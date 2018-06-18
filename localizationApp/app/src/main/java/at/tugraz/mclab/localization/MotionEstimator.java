package at.tugraz.mclab.localization;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;

public class MotionEstimator {

    public static final int IDLE = 0x0000;
    public static final int MOVING = 0x0001;
    public static final double MOVEMENT_POWER_THRESHOLD = 0.5;
    public static final double MAGNITUDE_THRESHOLD = 0.9;
    public static final double FREQUENCY_THRESHOLD = 0.5;

    private static int BUF_SIZE = 48;
    private static int ORIENTATION_BUF_SIZE = 5;
    private static int NDFT = 64;
    private final DFTAnalysis dftAnalysis = new DFTAnalysis(NDFT, BUF_SIZE);

    private final SensorManager mSensorManager;

    private ArrayList<Double> xbuffer = new ArrayList<Double>(BUF_SIZE);
    private ArrayList<Double> ybuffer = new ArrayList<Double>(BUF_SIZE);
    private ArrayList<Double> zbuffer = new ArrayList<Double>(BUF_SIZE);
    private final ReentrantLock bufferLock = new ReentrantLock();

    private ArrayList<Double> orientationBuffer = new ArrayList<Double>(ORIENTATION_BUF_SIZE);
    private final ReentrantLock orientationBufferLock = new ReentrantLock();

    private final float[] mRotationMatrix = new float[9];
    private final float[] mOrientationAngles = new float[3];

    private int state = IDLE;
    public double stepCount;
    public double orientationAngle;
    File dataFile;

    public MotionEstimator(File file, SensorManager sensorManager) {
        // init file for sensor data
        dataFile = file;
        try {
            dataFile.delete();
            dataFile.createNewFile();
            dataFile.setWritable(true, false);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mSensorManager = sensorManager;
    }

    public void addMeasurement(SensorEvent event) {

        // write the measurement data into buffers
        int sensorType = event.sensor.getType();
        if (sensorType == Sensor.TYPE_ACCELEROMETER) {
            bufferLock.lock();
            try {

                System.out.println("New buffer values are " + event.values[0] + " " + event.values[1] + " " + event
                        .values[2]);
                // remove first element (leftmost; oldest element..) if buffer is filled + add new
                // sample at the end of the list
                if (xbuffer.size() >= BUF_SIZE)
                    xbuffer.remove(0);
                xbuffer.add((double) event.values[0]);

                if (ybuffer.size() >= BUF_SIZE)
                    ybuffer.remove(0);
                ybuffer.add((double) event.values[1]);

                if (zbuffer.size() >= BUF_SIZE)
                    zbuffer.remove(0);
                zbuffer.add((double) event.values[2]);
            } finally {
                bufferLock.unlock();
            }
        } else if (sensorType == Sensor.TYPE_MAGNETIC_FIELD) {

            float[] mAccelerometerReading = new float[3];
            float[] mMagnetometerReading = new float[3];

            bufferLock.lock();
            try {
                mAccelerometerReading[0] = (float) xbuffer.get(xbuffer.size() - 1).doubleValue();
                mAccelerometerReading[1] = (float) ybuffer.get(ybuffer.size() - 1).doubleValue();
                mAccelerometerReading[2] = (float) zbuffer.get(zbuffer.size() - 1).doubleValue();
            } finally {
                bufferLock.unlock();
            }

            System.arraycopy(event.values, 0, mMagnetometerReading, 0, mMagnetometerReading.length);

            mSensorManager.getRotationMatrix(mRotationMatrix, null, mAccelerometerReading, mMagnetometerReading);

            mSensorManager.getOrientation(mRotationMatrix, mOrientationAngles);

            orientationBufferLock.lock();
            try {
                double orientation = mOrientationAngles[0] * 180 / Math.PI;
                if (orientationBuffer.size() >= ORIENTATION_BUF_SIZE)
                    orientationBuffer.remove(0);
                orientationBuffer.add(orientation);
                System.out.println("New orientation angle is " + orientation);

            } finally {
                orientationBufferLock.unlock();
            }

        }

        // write sensor data to log file
        //try (OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(dataFile, true)
        //        , "UTF-8")) {
        //    osw.write(event.values[0] + ";" + event.values[1] + ";" + event.values[2] + "\n");
        //    osw.flush();
        //} catch (FileNotFoundException e) {
        //    e.printStackTrace();
        //} catch (IOException e) {
        //    e.printStackTrace();
        //}

    }

    public void clearMeasurements() {

        // remove all measurements from the buffers..
        bufferLock.lock();
        try {
            xbuffer.clear();
            ybuffer.clear();
            zbuffer.clear();
        } finally {
            bufferLock.unlock();
        }

        orientationBufferLock.lock();
        try {
            orientationBuffer.clear();
        } finally {
            orientationBufferLock.unlock();
        }

    }

    private double mean(double[] signal) {

        // estimate mean
        double mean = 0;
        for (int i = 0; i < signal.length; i++) {
            mean += signal[i];
        }
        mean /= signal.length;
        return mean;
    }

    private double variance(double[] signal) {

        double mean = mean(signal);

        // estimate variance
        double variance = 0;
        for (int i = 0; i < signal.length; i++) {
            variance += Math.pow(signal[i] - mean, 2);
        }
        return variance;
    }

    private double power(double[] signal) {

        // estimate signal power
        double power = 0;
        for (int i = 0; i < signal.length; i++) {
            power += Math.pow(signal[i], 2);
        }
        return power;
    }

    private Motion detectMotion() {

        double[] signal;

        bufferLock.lock();
        try {
            // check if we have collected enough measurements..
            if (zbuffer.size() < BUF_SIZE) {
                return new Motion(false);
            }

            // convert buffer into double[]
            signal = new double[BUF_SIZE];
            for (int i = 0; i < BUF_SIZE; i++) {
                signal[i] = zbuffer.get(i);
            }

        } finally {
            bufferLock.unlock();
        }

        // remove mean from signal
        double zmean = mean(signal);
        for (int i = 0; i < BUF_SIZE; i++) {
            signal[i] = signal[i] - zmean;
        }

        // compute variance and check if there is significant motion
        double zVariance = power(signal);
        System.out.println("Z axis variance is " + zVariance);
        if (zVariance <= MOVEMENT_POWER_THRESHOLD)
            return new Motion(false);

        // compute dft
        //double[] zmagnitudes = dftAnalysis.dft(zbuffer.toArray(new Double[0]));
        double[] zmagnitudes = dftAnalysis.dft(signal);

        // find maximum magnitude and fundamental frequency of the motion (ignore mean)
        double max = -1;
        double fmax = 0;
        for (int idx = 0; idx < zmagnitudes.length; idx++) {
            if (zmagnitudes[idx] >= max) {
                max = zmagnitudes[idx];
                fmax = dftAnalysis.frequencies[idx];
            }
        }

        System.out.println("Found max magnitude/freq as " + max + "/" + fmax);

        // check if we have any walking pattern
        if (max < MAGNITUDE_THRESHOLD || fmax < FREQUENCY_THRESHOLD)
            return new Motion(false);

        // compute mean orientation angle
        double meanOrientation = 0;
        orientationBufferLock.lock();
        try {
            for (int i = 0; i < ORIENTATION_BUF_SIZE; i++)
                meanOrientation += orientationBuffer.get(i);
            meanOrientation /= ORIENTATION_BUF_SIZE;
        } finally {
            orientationBufferLock.unlock();
        }

        return new Motion(true, fmax, meanOrientation);

    }

    public int estimateMotion(double observationTime) {
        Motion motion = detectMotion();

        switch (state) {
            case IDLE:
                if (motion.isMoving) {
                    stepCount = 0;
                    state = MOVING;
                }
                break;
            case MOVING:
                if (motion.isMoving) {
                    stepCount += motion.frequency * observationTime;
                    orientationAngle = motion.angle;
                } else {
                    state = IDLE;
                }
                break;
        }
        return state;
    }
}
