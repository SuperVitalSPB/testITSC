package ru.supervital.test.itsc.service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import itsc.test.supervital.ru.R;
import ru.supervital.test.itsc.activity.MainActivity;
import ru.supervital.test.itsc.data.StepsDbHelper;

/**
 * Created by Vitaly Oantsa on 08.04.2017.
 */

public class StepsService extends Service {
    private static final String TAG = StepsService.class.getSimpleName();

    Map mCountSteps = new HashMap<String, Integer>();
    private StepsDbHelper mDbHelper = new StepsDbHelper(this);

    SensorManager mSensorManager;
    Sensor mSensorAccel;

    private float   mLimit = 15.0f;
    private float   mLastValues[] = new float[3*2];
    private float   mLastDirections[] = new float[3*2];
    private float   mLastExtremes[][] = { new float[3*2], new float[3*2] };
    private float   mLastDiff[] = new float[3*2];
    private int     mLastMatch = -1;

    @Override
    public void onCreate() {
        Log.d(TAG, "Service onCreate");
        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentText(getString(R.string.service_description));
        Notification notification;
        if (Build.VERSION.SDK_INT < 16)
            notification = builder.getNotification();
        else
            notification = builder.build();
        startForeground(777, notification);
        Intent hideIntent = new Intent(this, HideNotificationService.class);
        startService(hideIntent);


        super.onCreate();
        mCountSteps.put(mDbHelper.getTrustDate(null), mDbHelper.getCountStepsInDay(null));
    }

    void initPedometer(){
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensorAccel = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(listener, mSensorAccel, SensorManager.SENSOR_DELAY_FASTEST);
    }

    SensorEventListener listener = new SensorEventListener() {
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER)
                return;
            if (event.values[0]<=3 && event.values[1]<=3 && event.values[2]<=3)
                return;
            //Log.d(TAG, "new accelerometer value! " + sensor.getType());
            //Log.d(TAG, "accuracy: " + event.accuracy);
            //Log.d(TAG, "values: " + String.valueOf(event.values[0]) + " " + String.valueOf(event.values[1]) + " "+ String.valueOf(event.values[2]));

            synchronized (this) {
                float vSum = 0;
                int h = 480;
                float mYOffset = h * 0.5f;
//                final float fConst = - (h * 0.5f * (1.0f / (SensorManager.STANDARD_GRAVITY * 2)));
                final float fConst  = - (h * 0.5f * (1.0f / (SensorManager.MAGNETIC_FIELD_EARTH_MAX)));

                for (int i=0 ; i<3 ; i++) {
                    final float v = mYOffset + event.values[i] * fConst;

                    vSum += v;
                }
                int k = 0;
                float v = vSum / 3;

                float direction = (v > mLastValues[k] ? 1 : (v < mLastValues[k] ? -1 : 0));
                if (direction == - mLastDirections[k]) {
                    // Direction changed
                    int extType = (direction > 0 ? 0 : 1); // minumum or maximum?
                    mLastExtremes[extType][k] = mLastValues[k];
                    float diff = Math.abs(mLastExtremes[extType][k] - mLastExtremes[1 - extType][k]);

                    if (diff > mLimit) {

                        boolean isAlmostAsLargeAsPrevious = diff > (mLastDiff[k]*2/3);
                        boolean isPreviousLargeEnough = mLastDiff[k] > (diff/3);
                        boolean isNotContra = (mLastMatch != 1 - extType);

                        if (isAlmostAsLargeAsPrevious && isPreviousLargeEnough && isNotContra) {
                            Log.i(TAG, "step");
                            incSteps();
                            mLastMatch = extType;
                        }
                        else {
                            mLastMatch = -1;
                        }
                    }
                    mLastDiff[k] = diff;
                }
                mLastDirections[k] = direction;
                mLastValues[k] = v;
            }
        }
    };

    void incSteps(){
        int iCountSteps = (Integer) mCountSteps.get(mDbHelper.getTrustDate(null)) + 1;
        mCountSteps.put(mDbHelper.getTrustDate(null), iCountSteps);
        mDbHelper.setCountStepsInDay(null, iCountSteps);

        Intent intent = new Intent(MainActivity.BROADCAST_ACTION);
        intent.putExtra(MainActivity.PARAM_STATUS, MainActivity.STATUS_WORK)
                .putExtra(MainActivity.PARAM_VALUE, iCountSteps);
        sendBroadcast(intent);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service onStartCommand");
        //super.onStartCommand(intent, flags, startId);
        initPedometer();
        return START_STICKY;
    }

    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Service onDestroy");
        super.onDestroy();
        mDbHelper.close();
    }
}
