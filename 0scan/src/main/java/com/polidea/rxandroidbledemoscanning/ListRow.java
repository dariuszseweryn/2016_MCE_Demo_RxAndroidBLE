package com.polidea.rxandroidbledemoscanning;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ListRow extends LinearLayout {

    private TextView nameTextView;
    private TextView rssiTextView;
    private TextView lastSeenTextView;

    public ListRow(Context context) {
        super(context);
    }

    public ListRow(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ListRow(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        nameTextView = (TextView) findViewById(R.id.mac_name);
        rssiTextView = (TextView) findViewById(R.id.rssi);
        lastSeenTextView = (TextView) findViewById(R.id.last_seen);
    }

    public void setNameText(String text) {
        safeSet(nameTextView, text);
    }

    public void setRssiText(String text) {
        safeSet(rssiTextView, text);
    }

    public void setLastSeenText(String text) {
        safeSet(lastSeenTextView, text);
    }

    private void safeSet(TextView textView, String text) {
        if (textView != null) {
            textView.setText(text);
        }
    }
}
