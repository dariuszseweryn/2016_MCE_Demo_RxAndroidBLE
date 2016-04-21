package com.polidea.rxandroidbledemonotifications;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import com.polidea.rxandroidble.RxBleClient;
import com.polidea.rxandroidble.RxBleConnection;
import com.polidea.rxandroidble.RxBleDevice;
import com.polidea.rxandroidble.exceptions.BleDisconnectedException;
import com.polidea.rxandroidble.internal.RxBleLog;
import com.polidea.rxandroidble.internal.util.UUIDUtil;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.subscriptions.CompositeSubscription;

public class MainActivity extends AppCompatActivity {

    private RxBleClient rxBleClient;

    private DisplayFragment displayFragment;

    private static final String SENSOR_TAG_MAC_ADDRESS = "34:B1:F7:D5:04:01";

    private static final byte ENABLE_SENSOR_CODE = 1;

    private static final UUID accelerometerCharacteristicDataUuid = UUID.fromString("F000AA11-0451-4000-B000-000000000000");

    private static final UUID accelerometerCharacteristicConfigUuid = UUID.fromString("F000AA12-0451-4000-B000-000000000000");

    private CompositeSubscription compositeSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RxBleLog.setLogLevel(RxBleLog.VERBOSE);
        rxBleClient = RxBleClient.create(this);

        displayFragment = (DisplayFragment) getSupportFragmentManager().findFragmentById(R.id.display_fragment);
    }

    @Override
    protected void onResume() {
        super.onResume();
        compositeSubscription = new CompositeSubscription();

        final RxBleDevice bleDevice = rxBleClient.getBleDevice(SENSOR_TAG_MAC_ADDRESS);

        final Subscription stateSubscription = bleDevice
                .observeConnectionStateChanges()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rxBleConnectionState -> displayFragment.setStatus(rxBleConnectionState));
        compositeSubscription.add(stateSubscription);

        final Subscription dataSubscription = bleDevice
                .establishConnection(this, false)
                .flatMap(rxBleConnection ->
                        rxBleConnection.writeCharacteristic(accelerometerCharacteristicConfigUuid, new byte[]{ENABLE_SENSOR_CODE})
                                .flatMap(ignoredBytes -> rxBleConnection.setupNotification(accelerometerCharacteristicDataUuid))
                )
                .flatMap(observable -> observable)
                .map(bytes -> new AccelerometerData(bytes))
                .retryWhen(observable -> observable.delay(5, TimeUnit.SECONDS).filter(throwable -> throwable instanceof BleDisconnectedException))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        accelerometerData -> displayFragment.setAccValues(
                                accelerometerData.x,
                                accelerometerData.y,
                                accelerometerData.z
                        )//,
//                        throwable -> Toast.makeText(this, throwable.toString(), Toast.LENGTH_LONG).show()
                );

        compositeSubscription.add(dataSubscription);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (compositeSubscription != null) {
            compositeSubscription.unsubscribe();
            compositeSubscription = null;
        }
    }

    private static class AccelerometerData {

        float x;

        float y;

        float z;

        AccelerometerData(byte[] rawBytes) {
            x = ((int) rawBytes[0]) / 64.0f * 4;
            y = ((int) rawBytes[1]) / 64.0f * 4;
            z = ((int) rawBytes[2] * -1) / 64.0f * 4;
        }
    }
}
