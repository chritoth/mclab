package at.tugraz.mclab.activitymonitoring;

import java.util.Arrays;

public class FeatureExtractor {

    public static class AA_Filter {
        private double[] coeffs = {
                0.0004053458866948208, -0.0003546488346636961, -0.0010326454536157175,
                -0.0007239201021445352, 0.000985676833974638, 0.0026214754213113576,
                0.0017092888610681614, -0.002192761072533263, -0.005548279535175527,
                -0.003468434767531596, 0.00429339082433505, 0.010541132158361659,
                0.006426984842157257, -0.007797712227908163, -0.018859920594946736,
                -0.011389845919915847, 0.013773972013375402, 0.03346063496397457,
                0.020498439048544637, -0.02549353630646937, -0.0650051540159417,
                -0.04321655887292481, 0.06204003135928386, 0.21027666995510091, 0.3180503755355886,
                0.3180503755355886, 0.21027666995510091, 0.06204003135928386, -0.04321655887292481,
                -0.0650051540159417, -0.02549353630646937, 0.020498439048544637,
                0.03346063496397457, 0.013773972013375402, -0.011389845919915847,
                -0.018859920594946736, -0.007797712227908163, 0.006426984842157257,
                0.010541132158361659, 0.00429339082433505, -0.003468434767531596,
                -0.005548279535175527, -0.002192761072533263, 0.0017092888610681614,
                0.0026214754213113576, 0.000985676833974638, -0.0007239201021445352,
                -0.0010326454536157175, -0.0003546488346636961, 0.0004053458866948208};

        public static int NUM_TAPS = 48;

        public double[] filter(double[] input) {

            int conv_len = NUM_TAPS + input.length - 1;
            double[] conv = new double[conv_len];

            for (int n = 0; n < conv_len; n++) {
                conv[n] = 0;
                for (int k = 0; k < NUM_TAPS; k++) {
                    if ((n - k) >= 0 && (n - k) < input.length)
                        conv[n] += coeffs[k] * input[n - k];
                }
            }

            // truncate sequence (equals option 'same' in matlab, python..)
            int start = 0;
            if (conv_len % 2 == 0) // even
            {
                start = (conv_len - NUM_TAPS) / 2;
            } else {
                start = (conv_len - NUM_TAPS - 1) / 2;
            }

            double[] output = Arrays.copyOfRange(conv, start, start + NUM_TAPS);

            return output;
        }

    }

    private final AA_Filter aa_filter = new AA_Filter();
    public static int NDFT = 32;

    public FeatureExtractor() {
    }

    public double[] dftFeatures(double[] signal) {

        if (signal.length < 1)
            throw new RuntimeException("Input signal must not be empty!!");

        FFT fft = new FFT(NDFT);
        double[] window = fft.getWindow();

        // apply aa filter to the signal
        double[] sig = aa_filter.filter(signal);

        // downsample signal
        double[] downsampled = new double[sig.length / 3];
        int j = 0;
        for (int i = 0; i < sig.length; i++)
            if (i % 3 == 0)
                downsampled[j++] = sig[i];

        assert downsampled.length == 16;

        // apply window
        for (int i = 0; i < window.length; i++)
            downsampled[i] = downsampled[i] * window[i];

        // after aa filter + downsampling we have 16 samples --> zeropadding for DFT
        double[] re = Arrays.copyOf(downsampled, NDFT);
        double[] im = new double[NDFT];
        Arrays.fill(im, 0);

        // compute DFT
        fft.fft(re, im);
        double[] magnitude = new double[NDFT / 2];
        for (int i = 0; i < NDFT / 2; i++)
            magnitude[i] = Math.sqrt(re[i] * re[i] + im[i] * im[i]) * fft.scaling_1sided;

        return magnitude;
    }

    public double[] extractFeatureVectore(double[] x, double[] y, double[] z) {

        // after aa filter + downsampling we have 16 samples --> zeropadding for DFT
        double[] features = new double[3 * NDFT / 2];
        double[] x_features = dftFeatures(x);
        double[] y_features = dftFeatures(y);
        double[] z_features = dftFeatures(z);

        System.arraycopy(x_features, 0, features, 0, NDFT / 2);
        System.arraycopy(y_features, 0, features, NDFT / 2, NDFT / 2);
        System.arraycopy(z_features, 0, features, NDFT, NDFT / 2);

        return features;
    }

}
