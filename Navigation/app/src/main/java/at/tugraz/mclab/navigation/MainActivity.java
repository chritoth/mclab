package at.tugraz.mclab.navigation;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private static final int START_DELAY = 2000; // start delay in ms
    private static final int PERIOD = 1000; // scheduling period in ms

    private SensorManager mSensorManager;
    private TextView sensorTextView;
    private Sensor accelerationSensor;
    private MotionEstimator motionEstimator = new MotionEstimator();
    private final Timer timerUIUpdate = new Timer("timerUIUpdate");
    private final TimerTask taskUIUpdate = new UIUpdateThread();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init sensor manager and get sensors
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // init text view
        sensorTextView = (TextView) findViewById(R.id.sensor_text);
        sensorTextView.setText("Waiting for data...");

        // schedule UI update thread
        timerUIUpdate.scheduleAtFixedRate(taskUIUpdate, START_DELAY, PERIOD);
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
            final int motionState = motionEstimator.detectMotion();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    switch (motionState) {
                        case MotionEstimator.IDLE:
                            sensorTextView.setText("Believe it or not, you are\n\n IDLE");
                            break;
                        case MotionEstimator.MOVING:
                            sensorTextView.setText("Believe it or not, you are\n\n MOVING");
                            break;
                    }
                }
            });
        }
    }
}
