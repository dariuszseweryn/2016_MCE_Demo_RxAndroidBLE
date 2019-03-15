package com.polidea.rxandroidbledemonotifications;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;
import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.RxBleDevice;
import com.polidea.rxandroidble2.exceptions.BleDisconnectedException;
import com.polidea.rxandroidble2.internal.RxBleLog;
import com.polidea.rxandroidble2.scan.ScanFilter;
import com.polidea.rxandroidble2.scan.ScanSettings;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private RxBleClient rxBleClient;

    private DisplayFragment displayFragment;

    private static final byte ENABLE_SENSOR_CODE = 1;

    private static final UUID accelerometerCharacteristicDataUuid = UUID.fromString("F000AA11-0451-4000-B000-000000000000");

    private static final UUID accelerometerCharacteristicConfigUuid = UUID.fromString("F000AA12-0451-4000-B000-000000000000");

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

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

        final Disposable disposable1 =
                rxBleClient.scanBleDevices(
                        new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build(),
                        new ScanFilter.Builder().setDeviceName("SensorTag").build()
                )
                        .take(1)
                        .flatMap(scanResult -> {
                            RxBleDevice bleDevice = scanResult.getBleDevice();
                            final Disposable disposable = bleDevice.observeConnectionStateChanges()
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(rxBleConnectionState -> displayFragment.setStatus(rxBleConnectionState));
                            compositeDisposable.add(disposable);
                            return bleDevice.establishConnection(false);
                        })
                        .flatMap(rxBleConnection -> rxBleConnection
                                .writeCharacteristic(accelerometerCharacteristicConfigUuid, new byte[]{ENABLE_SENSOR_CODE})
                                .flatMapObservable(ignoredBytes -> rxBleConnection.setupNotification(accelerometerCharacteristicDataUuid))
                        )
                        .flatMap(observable -> observable)
                        .map(AccelerometerData::new)
                        .retryWhen(observable -> observable.delay(5, TimeUnit.SECONDS)
                                .filter(throwable -> throwable instanceof BleDisconnectedException))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                accelerometerData -> displayFragment.setAccValues(
                                        accelerometerData.x,
                                        accelerometerData.y,
                                        accelerometerData.z
                                ),
                                throwable -> Toast.makeText(this, throwable.toString(), Toast.LENGTH_LONG).show()
                        );
        compositeDisposable.add(disposable1);
    }

    @Override
    protected void onPause() {
        super.onPause();
        compositeDisposable.clear();
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
