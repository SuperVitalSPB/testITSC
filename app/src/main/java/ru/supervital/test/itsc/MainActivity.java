package ru.supervital.test.itsc;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.supervital.test.itsc.data.*;
import itsc.test.supervital.ru.R;

/**
 * Created by Vitaly Oantsa on 07.04.2017.
 */

public class MainActivity extends AppCompatActivity {
    final String TAG = MainActivity.class.getSimpleName();

    final String COUNT_STEPS = "COUNT_STEPS";

    Integer mCountSteps = 0;

    private StepsDbHelper mDbHelper = new StepsDbHelper(this);

    @BindView(R.id.lblPpd)
    TextView lblPpd;
    @BindView(R.id.lblCount)
    TextView lblCount;
    @BindView(R.id.lblSteps)
    TextView lblSteps;

    SensorManager mSensorManager;
    Sensor mSensorAccel;

    private float   mLimit = 15.0f;
    private float   mLastValues[] = new float[3*2];
    private float   mLastDirections[] = new float[3*2];
    private float   mLastExtremes[][] = { new float[3*2], new float[3*2] };
    private float   mLastDiff[] = new float[3*2];
    private int     mLastMatch = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mCountSteps = (savedInstanceState != null)?
                            savedInstanceState.getInt(COUNT_STEPS) :
                                mDbHelper.getCountStepsInDay(null);
        lblCount.setText(mCountSteps.toString());
        initPedometer();
    }

    void initPedometer(){
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensorAccel = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        setSensorAccelSupport(getSensorAccelSupport());
    }

    public void setSensorAccelSupport(Boolean aValue) {
        int supS = (aValue)? View.VISIBLE : View.INVISIBLE;
        lblPpd.setVisibility(supS);
        supS = (aValue)? R.string.clSteps : R.string.clNotAvailable;
        lblSteps.setText(supS);
    }

    public Boolean getSensorAccelSupport() {
        return mSensorAccel != null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!getSensorAccelSupport())
                return;
        mSensorManager.registerListener(listener, mSensorAccel, SensorManager.SENSOR_DELAY_FASTEST);
    }

    void incSteps(){
        mCountSteps++;
        setCountedSteps(mCountSteps);
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

    public void setCountedSteps(Integer iCountSteps) {
        this.mCountSteps = iCountSteps;
        mDbHelper.setCountStepsInDay(null, iCountSteps);
        lblCount.setText(iCountSteps.toString());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(COUNT_STEPS, mCountSteps);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (getSensorAccelSupport())
            mSensorManager.unregisterListener(listener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDbHelper.close();
    }
}
