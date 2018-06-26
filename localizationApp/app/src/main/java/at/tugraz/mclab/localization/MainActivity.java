package at.tugraz.mclab.localization;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private static final int START_DELAY = 2000; // start delay in ms
    private static final int PERIOD = 500; // scheduling period in ms
    private static final int NUMBER_OF_PARTICLES = 10000;

    private SensorManager mSensorManager;
    private TextView sensorTextView;
    private ImageView imageView;
    private Sensor accelerationSensor;
    private Sensor magnetometerSensor;
    private MotionEstimator motionEstimator;
    private final Timer timerMotionEstimation;
    private final MotionEstimationThread motionEstimationThread;
    private final ParticleUpdateThread particleUpdateThread;
    private int lastMotionState;
    private ParticleFilter particleFilter;
    private DrawParticlesView drawParticlesView;

    public MainActivity() {
        motionEstimationThread = new MotionEstimationThread();
        particleUpdateThread = new ParticleUpdateThread();
        timerMotionEstimation = new Timer("timerMotionEstimation");
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

        // init image view
        imageView = (ImageView) findViewById(R.id.floorPlanView);
        drawParticlesView = new DrawParticlesView(this);
        drawParticlesView.clearPanel(imageView);
        drawParticlesView.drawParticles(imageView, particleFilter.particles, new Position());
        //drawParticlesView.drawParticlesTest(imageView);

        // schedule UI update thread
        timerMotionEstimation.scheduleAtFixedRate(motionEstimationThread, START_DELAY, PERIOD);

        // start particle update thread
        particleUpdateThread.start();

        // create motion estimator
        File dataFile = new File(getExternalFilesDir(null), "sensorData.txt");
        motionEstimator = new MotionEstimator(dataFile, mSensorManager);

        // set last motion state to idle
        lastMotionState = MotionEstimator.IDLE;
    }

    @Override
    protected void onStart() {
        super.onStart();

        // register sensor listeners
        if (accelerationSensor != null) {
            mSensorManager.registerListener(this, accelerationSensor, SensorManager.SENSOR_DELAY_GAME);
        }
        if (accelerationSensor != null) {
            mSensorManager.registerListener(this, magnetometerSensor, SensorManager.SENSOR_DELAY_FASTEST);
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

    private class MotionEstimationThread extends TimerTask {

        @Override
        public void run() {

            System.out.println("Starting Motion Estimation update!");

            int motionState = motionEstimator.estimateMotion(PERIOD / 1000.0);
            double stepCount = motionEstimator.stepCount;
            double azimuth = motionEstimator.azimuth;

            // check if we stopped moving and possibly update GUI
            if (lastMotionState == MotionEstimator.MOVING && motionState == MotionEstimator.IDLE) {
                particleUpdateThread.updateParticles(stepCount, azimuth);
            }
            lastMotionState = motionState;
            updateTextView(motionState, stepCount, azimuth);

        }
    }

    private class ParticleUpdateThread implements Runnable {

        private Thread thread;
        private double stepCount;
        private double azimuth;

        public ParticleUpdateThread() {
        }

        public void start() {

            System.out.println("Starting Particle update!");
            if (thread == null) {
                thread = new Thread(this);
                thread.start();
            }
        }

        public void updateParticles(double stepCount, double azimuth) {
            System.out.println("Starting Particle update!");
            this.azimuth = azimuth;
            this.stepCount = stepCount;

            this.run();
        }

        @Override
        public void run() {

            System.out.println("Running Particle update!");

            // when we switch from moving to idle, we update the particles !!
            particleFilter.moveParticles(stepCount, azimuth);
            particleFilter.eliminateParticles();
            particleFilter.normalizeParticleWeights();
            particleFilter.resampleParticles();
            particleFilter.updateCurrentPosition(false);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    // draw the updated particles
                    drawParticlesView.clearPanel(imageView);
                    drawParticlesView.drawParticles(imageView, particleFilter.particles, particleFilter
                            .currentPosition);
                }
            });

        }
    }

    protected void updateTextView(final int motionState, final double stepCount, final double azimuth) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                System.out.println("Starting GUI update!");

                // update the text output..
                double direction = -azimuth - ParticleFilter.MAP_Y_HEADING_OFFSET;
                String text;
                switch (motionState) {
                    case MotionEstimator.IDLE:
                        text = "Believe it or not, you are\n\n IDLE\n ( " + stepCount + " steps " + "taken" + " " +
                                "lately " + "in direction " + direction + ")";
                        sensorTextView.setText(text);
                        break;
                    case MotionEstimator.MOVING:
                        text = "Believe it or not, you are\n\n MOVING\n (" + stepCount + "steps " + "taken" + " in "
                                + "direction " + direction + ")";
                        sensorTextView.setText(text);
                        break;
                }
            }
        });

    }

}
