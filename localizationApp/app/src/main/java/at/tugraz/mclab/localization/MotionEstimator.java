package at.tugraz.mclab.localization;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.locks.ReentrantLock;

public class MotionEstimator {

    public static final int IDLE = 0x0000;
    public static final int MOVING = 0x0001;
    public static final double MOVEMENT_POWER_THRESHOLD = 0.5;
    public static final double MAGNITUDE_THRESHOLD = 0.9;
    public static final double FREQUENCY_THRESHOLD = 0.5;

    private static int ACC_BUF_SIZE = 48;
    private static int AZIMUTH_BUF_SIZE = 501;
    private static int NDFT = 64;
    private final DFTAnalysis dftAnalysis = new DFTAnalysis(NDFT, ACC_BUF_SIZE);

    private ArrayList<Double> xAccBuffer = new ArrayList<Double>(ACC_BUF_SIZE);
    private ArrayList<Double> yAccBuffer = new ArrayList<Double>(ACC_BUF_SIZE);
    private ArrayList<Double> zAccBuffer = new ArrayList<Double>(ACC_BUF_SIZE);
    private final ReentrantLock accBufferLock = new ReentrantLock();

    private ArrayList<Double> azimuthBuffer = new ArrayList<Double>(AZIMUTH_BUF_SIZE);
    private final ReentrantLock azimuthBufferLock = new ReentrantLock();

    private ArrayList<Double> azimuthDuringMotion = new ArrayList<Double>();

    private int state = IDLE;
    public double stepCount;
    public double azimuth;

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
    }

    public void addMeasurement(SensorEvent event) {

        int sensorType = event.sensor.getType();
        if (sensorType == Sensor.TYPE_ACCELEROMETER) {
            accBufferLock.lock();
            try {

                //System.out.println("New accelerometer data: " + event.values[0] + " " + event
                // .values[1] + " " + event
                //        .values[2]);

                // remove first element (leftmost; oldest element..) if buffer is filled + add new
                // sample at the end of the list
                if (xAccBuffer.size() >= ACC_BUF_SIZE)
                    xAccBuffer.remove(0);
                xAccBuffer.add((double) event.values[0]);

                if (yAccBuffer.size() >= ACC_BUF_SIZE)
                    yAccBuffer.remove(0);
                yAccBuffer.add((double) event.values[1]);

                if (zAccBuffer.size() >= ACC_BUF_SIZE)
                    zAccBuffer.remove(0);
                zAccBuffer.add((double) event.values[2]);

            } finally {
                accBufferLock.unlock();
            }

        } else if (sensorType == Sensor.TYPE_MAGNETIC_FIELD) {

            float[] mAccelerometerReading = new float[3];
            float[] mMagnetometerReading = new float[3];
            float[] mRotationMatrix = new float[9];
            float[] mOrientationAngles = new float[3];

            accBufferLock.lock();
            try {
                // if we have no accelerometer readings yet we just quit..
                if (xAccBuffer.isEmpty())
                    return;

                // get last accelerometer readings
                mAccelerometerReading[0] = (float) xAccBuffer.get(xAccBuffer.size() - 1).doubleValue();
                mAccelerometerReading[1] = (float) yAccBuffer.get(yAccBuffer.size() - 1).doubleValue();
                mAccelerometerReading[2] = (float) zAccBuffer.get(zAccBuffer.size() - 1).doubleValue();

            } finally {
                accBufferLock.unlock();
            }

            azimuthBufferLock.lock();
            try {
                // get last magnetometer readings
                System.arraycopy(event.values, 0, mMagnetometerReading, 0, mMagnetometerReading.length);

                // compute the device orientation w.r.t. magnetic north (results lie in
                // mOrientationAngles..)
                SensorManager.getRotationMatrix(mRotationMatrix, null, mAccelerometerReading, mMagnetometerReading);
                SensorManager.getOrientation(mRotationMatrix, mOrientationAngles);
                double mAzimuth = (Math.toDegrees(mOrientationAngles[0]) + 360) % 360;

                // write the measurement data into buffers
                if (azimuthBuffer.size() >= AZIMUTH_BUF_SIZE)
                    azimuthBuffer.remove(0);
                azimuthBuffer.add(mAzimuth);
                System.out.println("New azimuth is " + mAzimuth);

            } finally {
                azimuthBufferLock.unlock();
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
        accBufferLock.lock();
        try {
            xAccBuffer.clear();
            yAccBuffer.clear();
            zAccBuffer.clear();
        } finally {
            accBufferLock.unlock();
        }

        azimuthBufferLock.lock();
        try {
            azimuthBuffer.clear();
        } finally {
            azimuthBufferLock.unlock();
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

        accBufferLock.lock();
        try {
            // check if we have collected enough measurements..
            if (zAccBuffer.size() < ACC_BUF_SIZE) {
                return new Motion(false);
            }

            // convert buffer into double[]
            signal = new double[ACC_BUF_SIZE];
            for (int i = 0; i < ACC_BUF_SIZE; i++) {
                signal[i] = zAccBuffer.get(i);
            }

        } finally {
            accBufferLock.unlock();
        }

        // remove mean from signal
        double zmean = mean(signal);
        for (int i = 0; i < ACC_BUF_SIZE; i++) {
            signal[i] = signal[i] - zmean;
        }

        // compute variance and check if there is significant motion
        double zVariance = power(signal);
        System.out.println("Z axis variance is " + zVariance);
        if (zVariance <= MOVEMENT_POWER_THRESHOLD)
            return new Motion(false);

        // compute dft
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
        azimuthBufferLock.lock();
        try {
            // compute mean azimuth
            //            double meanAzimuth = 0;
            //            for (int i = 0; i < azimuthBuffer.size(); i++)
            //                meanAzimuth += azimuthBuffer.get(i);
            //            meanAzimuth /= azimuthBuffer.size();
            //            return new Motion(true, fmax, meanAzimuth);

            // compute median azimuth
            ArrayList<Double> tmpBuffer = (ArrayList<Double>) azimuthBuffer.clone();
            Collections.sort(tmpBuffer);
            double medianAzimuth = tmpBuffer.get(azimuthBuffer.size() / 2);
            return new Motion(true, fmax, medianAzimuth);

        } finally {
            azimuthBufferLock.unlock();
        }

        // if we go here st went wrong!!
        //return new Motion(false);

    }

    public int estimateMotion(double observationTime) {
        Motion motion = detectMotion();

        switch (state) {
            case IDLE:
                if (motion.isMoving) {
                    stepCount = 0.0;// motion.frequency * observationTime * 0.5;
                    azimuth = motion.angle;
                    azimuthDuringMotion.add(motion.angle);
                    state = MOVING;
                }
                break;
            case MOVING:
                if (motion.isMoving) {
                    stepCount += motion.frequency * observationTime;
                    azimuth = motion.angle;
                    azimuthDuringMotion.add(motion.angle);
                } else {
                    state = IDLE;

                    // correct step count
                    stepCount = 0.5 + stepCount * 0.85;

                    // take median azimuth from all measurements recorded during motion
                    Collections.sort(azimuthDuringMotion);
                    azimuth = azimuthDuringMotion.get(azimuthDuringMotion.size() / 2);

                    // clear buffer
                    azimuthDuringMotion.clear();
                }
                break;
        }
        return state;
    }
}
