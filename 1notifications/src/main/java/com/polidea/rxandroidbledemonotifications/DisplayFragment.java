package com.polidea.rxandroidbledemonotifications;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.polidea.rxandroidble2.RxBleConnection;

public class DisplayFragment extends Fragment {

    private TextView statusTextView;
    private AccelerometerDataView accelerometerDataView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_display, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        statusTextView = view.findViewById(R.id.status);
        accelerometerDataView = view.findViewById(R.id.accelerometer_view);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        statusTextView = null;
    }

    public void setStatus(RxBleConnection.RxBleConnectionState state) {
        safeSetText(statusTextView, getStatusString(state));
    }

    private String getStatusString(RxBleConnection.RxBleConnectionState state) {
        if (state == RxBleConnection.RxBleConnectionState.CONNECTING) {
            return "Connecting";
        } else if (state == RxBleConnection.RxBleConnectionState.CONNECTED) {
            return "Connected";
        } else if (state == RxBleConnection.RxBleConnectionState.DISCONNECTED) {
            return "Disconnected";
        } else {
            return "Disconnecting";
        }
    }

    public void setAccValues(float x, float y, float z) {
        accelerometerDataView.addReading(x, y, z);
    }

    private void safeSetText(TextView textView, String text) {
        if (textView != null) {
            textView.setText(text);
        }
    }
}
