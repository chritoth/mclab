package at.tugraz.mclab.localization;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private static final int START_DELAY = 2000; // start delay in ms
    private static final int PERIOD = 1000; // scheduling period in ms

    private SensorManager mSensorManager;
    private TextView sensorTextView;
    private Sensor accelerationSensor;
    private MotionEstimator motionEstimator;
    private final Timer timerUIUpdate;
    private final TimerTask taskUIUpdate = new UIUpdateThread();

    public MainActivity() {
        timerUIUpdate = new Timer("timerUIUpdate");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init sensor manager and get sensors
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // init text view
        sensorTextView = findViewById(R.id.sensor_text);
        sensorTextView.setText("Waiting for data...");

        // schedule UI update thread
        timerUIUpdate.scheduleAtFixedRate(taskUIUpdate, START_DELAY, PERIOD);

        // create motion estimator
        File dataFile = new File(getExternalFilesDir(null), "sensorData.txt");
        motionEstimator = new MotionEstimator(dataFile);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // register sensor listeners
        if (accelerationSensor != null) {
            mSensorManager.registerListener(this, accelerationSensor, SensorManager
                    .SENSOR_DELAY_GAME);
        }

        // clear buffers to make sure we throw away old measurements..
        motionEstimator.clearMeasurements();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // unregister sensor listeners..
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // write the measurement data into buffers
        int sensorType = event.sensor.getType();
        if (sensorType == Sensor.TYPE_ACCELEROMETER) {
            motionEstimator.addMeasurement(event);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    private class UIUpdateThread extends TimerTask {
        @Override
        public void run() {
            final int motionState = motionEstimator.estimateMotion(PERIOD / 1000.0);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    switch (motionState) {
                        case MotionEstimator.IDLE:
                            sensorTextView.setText("Believe it or not, you are\n\n IDLE\n ( " +
                                                           Math.round(motionEstimator.stepCount)
                                                           + " steps taken lately)");
                            break;
                        case MotionEstimator.MOVING:
                            sensorTextView.setText("Believe it or not, you are\n\n MOVING\n (" +
                                                           motionEstimator.stepCount + "steps " +
                                                           "taken)");
                            break;
                    }
                }
            });
        }
    }
}
