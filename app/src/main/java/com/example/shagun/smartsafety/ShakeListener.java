package com.example.shagun.smartsafety;


import android.content.Context;
import android.hardware.SensorListener;
import android.hardware.SensorManager;

public class ShakeListener implements SensorListener {
    private static final int FORCE_THRESHOLD = 10000;
    private static final int TIME_THRESHOLD = 75;
    private static final int SHAKE_TIMEOUT = 500;
    private static final int SHAKE_DURATION = 150;
    private static final int SHAKE_COUNT = 1;

    private SensorManager mSensorManager;
    private float mLastX=-1.0f, mLastY=-1.0f, mLastZ=-1.0f;
    private long mLastTime;
    private OnShakeListener mShakeListener;
    private Context mContext;
    private int mShakeCount = 0;
    private long mLastShake;
    private long mLastForce;

    public interface OnShakeListener {
        public void onShake();
    }

    public ShakeListener(Context context) {
        mContext = context;
        resume();
    }

    public void setOnShakeListener(OnShakeListener listener) {
        mShakeListener = listener;
    }

    public void resume() {
        mSensorManager = (SensorManager)mContext.getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager == null) {
            throw new UnsupportedOperationException("Sensors not supported");
        }
        boolean supported = mSensorManager.registerListener(this, SensorManager.SENSOR_ACCELEROMETER, SensorManager.SENSOR_DELAY_GAME);
        if (!supported) {
            mSensorManager.unregisterListener(this, SensorManager.SENSOR_ACCELEROMETER);
            throw new UnsupportedOperationException("Accelerometer not supported");
        }
    }

    public void pause() {
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this, SensorManager.SENSOR_ACCELEROMETER);
            mSensorManager = null;
        }
    }

    @Override
    public void onSensorChanged(int sensor, float[] values) {
        if(sensor != SensorManager.SENSOR_ACCELEROMETER) return;

        long now = System.currentTimeMillis();

        if( (now - mLastForce) > SHAKE_TIMEOUT) {
            mShakeCount = 0;
        }

        if ((now - mLastTime) > TIME_THRESHOLD) {
            long diff = now - mLastTime;
            float speed = Math.abs(values[SensorManager.DATA_X] + values[SensorManager.DATA_Y] + values[SensorManager.DATA_Z] - mLastX - mLastY - mLastZ) / diff * 10000;
            if (speed > FORCE_THRESHOLD) {
                if ((++mShakeCount >= SHAKE_COUNT) && (now - mLastShake > SHAKE_DURATION)) {
                    mLastShake = now;
                    mShakeCount = 0;
                    if (mShakeListener != null) {
                        mShakeListener.onShake();
                    }
                }
                mLastForce = now;
            }
            mLastTime = now;
            mLastX = values[SensorManager.DATA_X];
            mLastY = values[SensorManager.DATA_Y];
            mLastZ = values[SensorManager.DATA_Z];
        }

    }

    @Override
    public void onAccuracyChanged(int i, int i1) { }

}
