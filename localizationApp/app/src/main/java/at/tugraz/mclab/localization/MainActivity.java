package at.tugraz.mclab.localization;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private static final int START_DELAY = 2000; // start delay in ms
    private static final int PERIOD = 1000; // scheduling period in ms
    private static final int NUMBER_OF_PARTICLES = 10000;

    private SensorManager mSensorManager;
    private TextView sensorTextView;
    private Sensor accelerationSensor;
    private Sensor magnetometerSensor;
    private MotionEstimator motionEstimator;
    private final Timer timerUIUpdate;
    private final TimerTask taskUIUpdate;
    private int lastMotionState;
    private ParticleFilter particleFilter;

    public MainActivity() {
        taskUIUpdate = new UIUpdateThread();
        timerUIUpdate = new Timer("timerUIUpdate");
        particleFilter = new ParticleFilter(NUMBER_OF_PARTICLES);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init sensor manager and get sensors
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        // init text view
        sensorTextView = findViewById(R.id.sensor_text);
        sensorTextView.setText("Waiting for data...");

        // schedule UI update thread
        timerUIUpdate.scheduleAtFixedRate(taskUIUpdate, START_DELAY, PERIOD);

        // create motion estimator
        File dataFile = new File(getExternalFilesDir(null), "sensorData.txt");
        motionEstimator = new MotionEstimator(dataFile, mSensorManager);

        // set last motion state to idle
        lastMotionState = MotionEstimator.IDLE;


        DrawParticlesView mDrawingView=new DrawParticlesView(this);
        ImageView mDrawingPad=(ImageView)findViewById(R.id.floorPlan);

        Paint paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeWidth(10);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        Bitmap bmp = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bmp);
        canvas.drawCircle(bmp.getWidth()/2,bmp.getHeight()/2, 20, paint);
        mDrawingPad.setImageBitmap(bmp);
    }


    @Override
    protected void onStart() {
        super.onStart();

        // register sensor listeners
        if (accelerationSensor != null) {
            mSensorManager.registerListener(this, accelerationSensor, SensorManager.SENSOR_DELAY_GAME);
        }
        if (accelerationSensor != null) {
            mSensorManager.registerListener(this, magnetometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
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
        } else if (sensorType == Sensor.TYPE_MAGNETIC_FIELD) {
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
            final int stepCount = (int) Math.round(motionEstimator.stepCount);
            final double orientationAngle = motionEstimator.orientationAngle;

            // when we switch from moving to idle, we update the particles !!
            if (lastMotionState == MotionEstimator.MOVING && motionState == MotionEstimator.IDLE) {
                particleFilter.moveParticles(stepCount, orientationAngle);
                particleFilter.eliminateParticles();
                particleFilter.normalizeParticleWeights();
                particleFilter.resampleParticles();
            }
            lastMotionState = motionState;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    switch (motionState) {
                        case MotionEstimator.IDLE:
                            sensorTextView.setText("Believe it or not, you are\n\n IDLE\n ( " + stepCount + " steps "
                                                           + "taken lately in direction " + orientationAngle + ")");
                            break;
                        case MotionEstimator.MOVING:
                            sensorTextView.setText("Believe it or not, you are\n\n MOVING\n (" + stepCount + "steps "
                                                           + "takenin direction " + orientationAngle + ")");
                            break;
                    }
                }
            });
        }
    }
}
