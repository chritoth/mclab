package at.tugraz.mclab.navigation;

import android.hardware.SensorEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;

public class MotionEstimator {

    public static final int IDLE = 0x0000;
    public static final int MOVING = 0x0001;
    public static final double MOVEMENT_POWER_THRESHOLD = 0.3;

    private static int BUF_SIZE = 48;
    private static int NDFT = 64;
    private final DFTAnalysis dftAnalysis = new DFTAnalysis(NDFT, BUF_SIZE);

    private ArrayList<Double> xbuffer = new ArrayList<Double>(BUF_SIZE);
    private ArrayList<Double> ybuffer = new ArrayList<Double>(BUF_SIZE);
    private ArrayList<Double> zbuffer = new ArrayList<Double>(BUF_SIZE);
    private final ReentrantLock bufferLock = new ReentrantLock();

    public MotionEstimator() {
    }

    public void addMeasurement(SensorEvent event) {

        bufferLock.lock();
        try {

            System.out.println("New buffer values are " + event.values[0] + " " + event.values[1]
                                       + " " + event.values[2]);
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

    }

    public void clearMeasurements() {
        bufferLock.lock();
        try {
            // remove all measurements from the buffers..
            xbuffer.clear();
            ybuffer.clear();
            zbuffer.clear();
        } finally {
            bufferLock.unlock();
        }

    }

    private double computeSignalVariance() {

        double xmean = 0;
        double ymean = 0;
        double zmean = 0;

        // compute mean values for each axis
        for (int i = 0; i < xbuffer.size(); i++) {
            xmean += xbuffer.get(i);
            ymean += ybuffer.get(i);
            zmean += zbuffer.get(i);
        }
        xmean /= xbuffer.size();
        ymean /= ybuffer.size();
        zmean /= zbuffer.size();

        double variance = 0;
        for (int i = 0; i < xbuffer.size(); i++) {
            variance += Math.pow(xbuffer.get(i) - xmean, 2);
            variance += Math.pow(ybuffer.get(i) - ymean, 2);
            variance += Math.pow(zbuffer.get(i) - zmean, 2);
        }
        return variance / (xbuffer.size() + ybuffer.size() + zbuffer.size());
    }

    public int detectMotion() {

        double signalVariance;
        bufferLock.lock();
        try {

            // check if we have collected enough measurements..
            if (xbuffer.size() < BUF_SIZE) {
                return IDLE;
            }

            signalVariance = computeSignalVariance();
            System.out.println("Signal Variance is " + signalVariance);
        } finally {
            bufferLock.unlock();
        }

        if (signalVariance <= MOVEMENT_POWER_THRESHOLD)
            return IDLE;
        else
            return MOVING;
    }

}
