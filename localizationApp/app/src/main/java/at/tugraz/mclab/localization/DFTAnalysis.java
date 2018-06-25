package at.tugraz.mclab.localization;

import java.util.Arrays;

public class DFTAnalysis {

    private static final int M = 3; // downsampling factor
    private static final double fS = 50; // sampling frequency

    private final int NDFT; // DFT length -> has to be a power of 2 !!!
    private final int NSig; // input signal length
    private final int NSigDec; // decimated (input) signal length
    private final int NSpectrum; // one-sided spectrum length
    private final FFT fft;
    private double[] window;
    private double scaling1Sided;
    private double scaling2Sided;

    public final double[] frequencies;

    public DFTAnalysis(int NDFT, int NSig) {
        this.NSig = NSig;
        NSigDec = (int) Math.floor(this.NSig / M);
        generateWindow(NSigDec);
        this.NDFT = NDFT;
        NSpectrum = (NDFT / 2 + 1);
        fft = new FFT(NDFT);

        double[] frequencies = new double[NSpectrum];
        for (int i = 0; i < NSpectrum; i++) {
            frequencies[i] = i * (fS / NDFT / M);
            System.out.println(frequencies[i]);
        }

        this.frequencies = frequencies;
    }

    private void generateWindow(int num_taps) {
        // generate Hann window + scaling factors
        window = new double[num_taps];
        scaling2Sided = 0;
        for (int i = 0; i < num_taps; i++) {
            window[i] = 0.5 * (1.0 - Math.cos(2 * Math.PI * i / num_taps));
            scaling2Sided += window[i];
        }

        scaling2Sided = 1 / scaling2Sided;
        scaling1Sided = 2 * scaling2Sided;
    }

    private double[] downsample(double[] signal) {

        // apply anti aliasing filter to the signal
        double[] sig = AAFilter.filter(signal);

        // downsample signal
        double[] sigDec = new double[NSigDec];
        int j = 0;
        for (int i = 0; i < NSig; i++)
            if (i % M == 0)
                sigDec[j++] = sig[i];

        return sigDec;
    }

    public double[] dft(double[] signal) {

        if (signal.length != NSig)
            throw new RuntimeException("Input signal has invalid length!!");

        // downsample signal
        double[] sigDec = downsample(signal);

        // apply window
        for (int i = 0; i < NSigDec; i++)
            sigDec[i] = sigDec[i] * window[i];

        // prepare re/im parts of the signal for the FFT + apply zero padding (implicitly..)
        double[] re = Arrays.copyOf(sigDec, NDFT);
        double[] im = new double[NDFT];
        Arrays.fill(im, 0);

        // compute DFT
        fft.fft(re, im);
        double[] magnitude = new double[NSpectrum];
        //System.out.println("Scaling: " + scaling1Sided);
        for (int i = 0; i < NSpectrum; i++) {
            magnitude[i] = Math.sqrt(re[i] * re[i] + im[i] * im[i]) * scaling1Sided;
        }
        return magnitude;
    }
}
