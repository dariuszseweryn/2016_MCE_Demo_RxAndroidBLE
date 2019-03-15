package com.polidea.rxandroidbledemoscanning;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;
import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.scan.ScanSettings;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final UUID FILTERING_SERVICE_UUID = UUID.fromString("a59bfff0-6343-4053-a67c-357cc7f8f1a9");

    private RxBleClient rxBleClient;

    private Disposable scanDisposable;

    ListFragment listFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listFragment = (ListFragment) getSupportFragmentManager().findFragmentById(R.id.list_fragment);

        rxBleClient = RxBleClient.create(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        scanDisposable = rxBleClient.scanBleDevices(
                new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
        )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        rxBleScanResult -> listFragment.put(rxBleScanResult),
                        throwable -> Toast.makeText(this, throwable.toString(), Toast.LENGTH_LONG).show()
                );
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (scanDisposable != null) {
            scanDisposable.dispose();
            scanDisposable = null;
        }
    }
}
