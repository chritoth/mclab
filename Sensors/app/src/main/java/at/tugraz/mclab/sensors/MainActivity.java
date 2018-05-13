package at.tugraz.mclab.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.File;
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
    private ArrayDeque<Double> xbuffer;
    private ArrayDeque<Double> ybuffer;
    private ArrayDeque<Double> zbuffer;
    private final Timer timerUIUpdate = new Timer("timerUIUpdate");
    private final TimerTask taskUIUpdate = new UIUpdateThread();
    private final ReentrantLock bufferLock = new ReentrantLock();
    private final FeatureExtractor featureExtractor = new FeatureExtractor();

    File dataFile;

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
            // Todo : classification

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String bla = "asdf" + features[0] + "bla" + features[3];
                    sensorTextView.setText(bla);
                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // init sensor manager and get (list of all) sensors
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        linearAccelerationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        // init text view
        sensorTextView = (TextView) findViewById(R.id.sensor_text);
        sensorTextView.setText("Waiting for activity..");

        xbuffer = new ArrayDeque(BUF_SIZE);
        ybuffer = new ArrayDeque(BUF_SIZE);
        zbuffer = new ArrayDeque(BUF_SIZE);

        // init file for sensor data
        //        dataFile = new File(getExternalFilesDir(null), "sensorData.txt");
        //        try {
        //            dataFile.delete();
        //            dataFile.createNewFile();
        //            dataFile.setWritable(true, false);
        //        } catch (IOException e) {
        //            e.printStackTrace();
        //        }
        //
    }

    @Override
    protected void onStart() {
        super.onStart();

        // SENSOR_DELAY_NORMAL .... fs =  5,00 Hz
        // SENSOR_DELAY_UI ........ fs = 16,67 Hz
        // SENSOR_DELAY_GAME ...... fs = 50,00 Hz
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
                if (!xbuffer.isEmpty())
                    xbuffer.removeLast();
                xbuffer.addFirst((double) event.values[0]);
                if (!ybuffer.isEmpty())
                    ybuffer.removeLast();
                ybuffer.addFirst((double) event.values[1]);
                if (!zbuffer.isEmpty())
                    zbuffer.removeLast();
                zbuffer.addFirst((double) event.values[2]);

            } finally {
                bufferLock.unlock();
            }

        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        switch (id) {
            case R.id.action_list_sensors:
                break;
            case R.id.action_show_data:
                break;
            default:
        }

        return super.onOptionsItemSelected(item);
    }

}
