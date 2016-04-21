package com.polidea.rxandroidbledemonotifications;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class AccelerometerDataView extends View {

    private static final int READINGS_LENGTH = 50;
    private final Reading[] readings = new Reading[READINGS_LENGTH];

    private final Paint blackPaint = new Paint();
    private final Paint xPaint = createAxisPaint(Color.RED);
    private final Paint yPaint = createAxisPaint(Color.GREEN);
    private final Paint zPaint = createAxisPaint(Color.BLUE);

    private int startDisplayIndex = 0;
    private int addNextIndex = 0;

    public AccelerometerDataView(Context context) {
        this(context, null);
    }

    public AccelerometerDataView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AccelerometerDataView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        for (int i = 0; i < READINGS_LENGTH; i++) {
            readings[i] = new Reading();
        }
    }

    private Paint createAxisPaint(int color) {
        final Paint paint = new Paint();
        paint.setStrokeWidth(10);
        paint.setColor(color);
        return paint;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float widthStep = ((float) getWidth()) / READINGS_LENGTH;
        float halfHeight = getHeight() * 0.5f;
        canvas.save();
        canvas.translate(0, halfHeight);
        canvas.drawColor(Color.LTGRAY);
        canvas.drawLine(0, 0, getWidth(), 0, blackPaint);
        for (int i = 0; i < READINGS_LENGTH - 2; i++) {
            int lineStartIndex = (startDisplayIndex + i) % READINGS_LENGTH;
            int lineEndIndex = (lineStartIndex + 1) % READINGS_LENGTH;
            Reading startReading = readings[lineStartIndex];
            Reading endReading = readings[lineEndIndex];
            if (endReading.x == Float.MIN_VALUE) {
                break; // end reading was not yet used
            }
            float startX = i * widthStep;
            float endX = (i + 1) * widthStep;
            canvas.drawLine(startX, cut(startReading.x) * halfHeight, endX, cut(endReading.x) * halfHeight, xPaint);
            canvas.drawLine(startX, cut(startReading.y) * halfHeight, endX, cut(endReading.y) * halfHeight, yPaint);
            canvas.drawLine(startX, cut(startReading.z) * halfHeight, endX, cut(endReading.z) * halfHeight, zPaint);
        }
        canvas.restore();
    }

    private float cut(float value) {
        if (value > 1.0f) {
            return 1.0f;
        } else if (value < -1.0f) {
            return -1.0f;
        } else {
            return value;
        }
    }

    public void addReading(float x, float y, float z) {
        final Reading reading = readings[(addNextIndex++) % READINGS_LENGTH];
        reading.x = x;
        reading.y = y;
        reading.z = z;
        if (addNextIndex > READINGS_LENGTH) {
            startDisplayIndex++;
        }
        postInvalidate();
    }

    private static class Reading {

        float x = Float.MIN_VALUE;
        float y = Float.MIN_VALUE;
        float z = Float.MIN_VALUE;
    }
}
