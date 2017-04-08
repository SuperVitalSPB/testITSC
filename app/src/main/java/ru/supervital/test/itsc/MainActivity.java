package ru.supervital.test.itsc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import itsc.test.supervital.ru.R;
import ru.supervital.test.itsc.data.StepsDbHelper;
import ru.supervital.test.itsc.service.StepsService;

/**
 * Created by Vitaly Oantsa on 07.04.2017.
 */

public class MainActivity extends AppCompatActivity {
    final String TAG = MainActivity.class.getSimpleName();

    public final static String BROADCAST_ACTION = "ru.supervital.test.itsc.service.pedometr";

    final String COUNT_STEPS = "COUNT_STEPS";

    public final static String PARAM_STATUS = "status";
    public final static String PARAM_VALUE = "param_value";
    public final static int STATUS_START = 100;
    public final static int STATUS_WORK = 200;
    public final static int STATUS_FINISH = 300;

    BroadcastReceiver br;
    Integer mCountSteps = 0;

    @BindView(R.id.lblPpd)
    TextView lblPpd;
    @BindView(R.id.lblCount)
    TextView lblCount;
    @BindView(R.id.lblSteps)
    TextView lblSteps;

    SensorManager mSensorManager;
    Sensor mSensorAccel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        lblCount.setText("");
        restoreInstace(savedInstanceState);
        initPedometer();
    }

    void initPedometer(){
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensorAccel = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        boolean bSupporSens = getSensorAccelSupport();
        setSensorAccelSupport(bSupporSens);
        if (bSupporSens) {
            startService(new Intent(this, StepsService.class));
            InitBroadCast();
        }
    }

    void restoreInstace(Bundle savedInstanceState){
        if (savedInstanceState != null) {
            mCountSteps = savedInstanceState.getInt(COUNT_STEPS);
        } else {
            StepsDbHelper mDbHelper = new StepsDbHelper(this);
            mCountSteps = mDbHelper.getCountStepsInDay(null);
            mDbHelper.close();
        }
        lblCount.setText(mCountSteps.toString());
    }

    public Boolean getSensorAccelSupport() {
        return mSensorAccel != null;
    }

    public void setSensorAccelSupport(Boolean aValue) {
        int supS = (aValue)? View.VISIBLE : View.INVISIBLE;
        lblPpd.setVisibility(supS);
        supS = (aValue)? R.string.clSteps : R.string.clNotAvailable;
        lblSteps.setText(supS);
    }

    private void InitBroadCast(){
        br = new BroadcastReceiver() {

            public void onReceive(Context context, Intent intent) {
                int status = intent.getIntExtra(PARAM_STATUS, 0);
                switch (status){
                    case STATUS_START:
                        Log.d(TAG, "STATUS_START");
                        break;
                    case STATUS_FINISH:
                        Log.d(TAG, "STATUS_FINISH");
                        break;
                    case STATUS_WORK:
                        mCountSteps = intent.getIntExtra(PARAM_VALUE, 0);
                        Log.d(TAG, "STATUS_WORK: " + mCountSteps);
                        setCountedSteps(mCountSteps);
                        break;
                }
            }
        };
        // создаем фильтр для BroadcastReceiver
        // регистрируем (включаем) BroadcastReceiver
        registerReceiver(br, new IntentFilter(BROADCAST_ACTION));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(COUNT_STEPS, mCountSteps);
    }

    public void setCountedSteps(Integer iCountSteps) {
        lblCount.setText(iCountSteps.toString());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(br);
    }

}
