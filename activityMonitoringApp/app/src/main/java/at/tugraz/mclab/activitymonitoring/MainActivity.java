package at.tugraz.mclab.activitymonitoring;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantLock;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static int BUF_SIZE = 48;
    private static int START_DELAY = 1500; // start delay in ms
    private static int PERIOD = 1000; // scheduling period in ms

    private SensorManager mSensorManager;
    private TextView sensorTextView;
    private Sensor linearAccelerationSensor;

    private KNN knnClassifier;

    private ArrayDeque<Double> xbuffer;
    private ArrayDeque<Double> ybuffer;
    private ArrayDeque<Double> zbuffer;
    private final Timer timerUIUpdate = new Timer("timerUIUpdate");
    private final TimerTask taskUIUpdate = new UIUpdateThread();
    private final ReentrantLock bufferLock = new ReentrantLock();
    private final FeatureExtractor featureExtractor = new FeatureExtractor();

    private class UIUpdateThread extends TimerTask {
        @Override
        public void run() {

            double[] x = new double[BUF_SIZE], y = new double[BUF_SIZE], z = new double[BUF_SIZE];
            bufferLock.lock();
            try {
                int i = 0;
                for (Iterator<Double> it = xbuffer.iterator(); it.hasNext(); ) {
                    x[i++] = it.next();
                }
                i = 0;
                for (Iterator<Double> it = ybuffer.iterator(); it.hasNext(); ) {
                    y[i++] = it.next();
                }
                i = 0;
                for (Iterator<Double> it = zbuffer.iterator(); it.hasNext(); ) {
                    z[i++] = it.next();
                }
            } finally {
                bufferLock.unlock();
            }

            final double[] features = featureExtractor.extractFeatureVectore(x, y, z);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int activity = knnClassifier.predict(features);

                    switch (activity) {
                        case 0:
                            sensorTextView.setText("Believe it or not, you are\n\n JUMPING");
                            break;
                        case 1:
                            sensorTextView.setText("Believe it or not, you are\n\n IDLE");
                            break;
                        case 2:
                            sensorTextView.setText("Believe it or not, you are\n\n WALKING");
                            break;
                        case 3:
                            sensorTextView.setText("Believe it or not, you are\n\n WAVING");
                            break;
                    }
                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init sensor manager and get (list of all) sensors
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        linearAccelerationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        // init text view
        sensorTextView = (TextView) findViewById(R.id.sensor_text);
        sensorTextView.setText("Waiting for data");

        // circular buffers
        xbuffer = new ArrayDeque(BUF_SIZE);
        ybuffer = new ArrayDeque(BUF_SIZE);
        zbuffer = new ArrayDeque(BUF_SIZE);

        InputStream trainingData = getResources().openRawResource(R.raw.trainingdata);
        knnClassifier = new KNN(trainingData);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (linearAccelerationSensor != null) {
            mSensorManager.registerListener(this, linearAccelerationSensor, SensorManager
                    .SENSOR_DELAY_GAME);
        }

        timerUIUpdate.scheduleAtFixedRate(taskUIUpdate, START_DELAY, PERIOD);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        // write the measurement data into buffers
        int sensorType = event.sensor.getType();
        if (sensorType == Sensor.TYPE_LINEAR_ACCELERATION) {

            bufferLock.lock();
            try {
                if (xbuffer.size() >= BUF_SIZE)
                    xbuffer.removeLast();
                xbuffer.addFirst((double) event.values[0]);

                ybuffer.addFirst((double) event.values[1]);
                if (ybuffer.size() >= BUF_SIZE)
                    ybuffer.removeLast();

                zbuffer.addFirst((double) event.values[2]);
                if (zbuffer.size() >= BUF_SIZE)
                    zbuffer.removeLast();
            } finally {
                bufferLock.unlock();
            }

        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }
}
