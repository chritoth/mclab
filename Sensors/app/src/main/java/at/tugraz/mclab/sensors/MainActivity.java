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
    private String accelerometerText;
    private String gyroscopeText;
    private String gravitySensorText;
    private String linearAccelerationText;
    private TextView sensorTextView;
    private Sensor accelerometer;
    private Sensor gyroscope;
    private Sensor gravitySensor;
    private Sensor linearAccelerationSensor;
    File dataFile;

    private knn KNN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init sensor manager and get (list of all) sensors
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        gravitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
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

        // SENSOR_DELAY_NORMAL .... fs =  5,00 Hz
        // SENSOR_DELAY_UI ........ fs = 16,67 Hz
        // SENSOR_DELAY_GAME ...... fs = 50,00 Hz
        // if (accelerometer != null) {
        //     mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        // }
        // if (gyroscope != null) {
        //     mSensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        // }
        // if (gravitySensor != null) {
        //     mSensorManager.registerListener(this, gravitySensor, SensorManager.SENSOR_DELAY_NORMAL);
        // }
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
            case Sensor.TYPE_ACCELEROMETER:
                x = event.values[0];
                y = event.values[1];
                z = event.values[2];
                accelerometerText = "Accelerometer:\nx = " + x + "\ny = " + y + "\nz " + "= " + "" + z + "\n\n";
                break;

            case Sensor.TYPE_GYROSCOPE:
                x = event.values[0];
                y = event.values[1];
                z = event.values[2];
                gyroscopeText = "Gyroscope:\nx = " + x + "\ny = " + y + "\nz = " + z + "\n\n";
                break;

            case Sensor.TYPE_GRAVITY:
                x = event.values[0];
                y = event.values[1];
                z = event.values[2];
                gravitySensorText = "Gravity Sensor:\nx = " + x + "\ny = " + y + "\nz = " + z + "\n\n";
                break;

            case Sensor.TYPE_LINEAR_ACCELERATION:
                x = event.values[0];
                y = event.values[1];
                z = event.values[2];
                linearAccelerationText = "Linear Acceleration:\nx = " + x + "\ny = " + y + "\nz = " + z + "\n\n";
                break;

            default:
        }

        sensorTextView.setText(linearAccelerationText);

        // write sensor data to log file
        try (OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(dataFile, true), "UTF-8")) {
            osw.write(x + ";" + y + ";" + z + "\n");
            osw.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

       // KNN.knn("classification\\jumpingLinAcc_train.txt\n","classification\\jumpingLinAcc_test.txt",3);


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
