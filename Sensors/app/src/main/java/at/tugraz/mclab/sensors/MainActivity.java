package at.tugraz.mclab.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.hardware.SensorEventListener;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private String linearAccelerationText;
    private TextView sensorTextView;
    private Sensor linearAccelerationSensor;
    File dataFile;

    private knn KNN;

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

        // init file for sensor data
        dataFile = new File(getExternalFilesDir(null), "sensorData.txt");
        try {
            dataFile.delete();
            dataFile.createNewFile();
            dataFile.setWritable(true, false);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        if (linearAccelerationSensor != null) {
            mSensorManager.registerListener(this, linearAccelerationSensor, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        double x = 0, y = 0, z = 0;

        int sensorType = event.sensor.getType();
        switch (sensorType) {

            case Sensor.TYPE_LINEAR_ACCELERATION:
                x = event.values[0];
                y = event.values[1];
                z = event.values[2];
                //linearAccelerationText = "Linear Acceleration:\nx = " + x + "\ny = " + y + "\nz = " + z + "\n\n";
                linearAccelerationText = "";
                break;

            default:
        }

        sensorTextView.setText(linearAccelerationText);
        double [] dummy = {3,4};
        int activity = KNN.knn("TrainingData.txt\n",dummy,3);

        switch (activity){
            case 0:
                sensorTextView.setText("Believe it or not, you are jumping");
                break;
            case 1:
                sensorTextView.setText("Believe it or not, you are in idle state");
                break;
            case 2:
                sensorTextView.setText("Believe it or not, you are turning left");
                break;
            case 3:
                sensorTextView.setText("Believe it or not, you are turning right");
                break;
            case 4:
                sensorTextView.setText("Believe it or not, you are walking");
                break;
            case 5:
                sensorTextView.setText("Believe it or not, you are waving");
                break;

        }


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

}
