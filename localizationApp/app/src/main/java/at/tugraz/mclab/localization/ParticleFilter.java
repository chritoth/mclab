package at.tugraz.mclab.localization;

import java.util.Random;

public class ParticleFilter {

    private final int Ns; // number of particles
    private Particle[] particles;
    private static final double HEADING_STD_DEV = 10.0; // std deviation of the heading uncertainty in degree
    private static final double STRIDE_UNCERTAINTY = 10.0; // uncertainty in % of the measured stride

    public ParticleFilter(int Ns) {
        this.Ns = Ns;
    }

    private void init() {
        Random rngPos = new Random();
        Random rngStride = new Random();

        // generate initial samples
        for (Particle particle : particles) {
            // TODO: consider position constraints given the floor map...
            particle.setX(rngPos.nextDouble());
            particle.setY(rngPos.nextDouble());
            double stride = (Particle.SMIN + Particle.SMAX) / 2.0 + (Particle.SMAX - Particle.SMIN) * rngStride
                    .nextDouble();
            particle.setS(stride);
        }
    }

    public void moveParticles(int stepCount, double direction) {
        Random rngStride = new Random();
        Random rngHeading = new Random();

        for (Particle particle : particles) {
            particle.updateLastPosition();

            double stride = stepCount * particle.getS();
            stride += 2.0 * STRIDE_UNCERTAINTY * stride * (rngStride.nextDouble() - 0.5);

            double heading = direction + HEADING_STD_DEV * rngHeading.nextGaussian();

            particle.setX(particle.getX() + stride + Math.cos(heading));
            particle.setY(particle.getY() + stride + Math.sin(heading));
        }
    }

    public void eliminateParticles() {
        // TODO: eliminate particles violating physical constraints given by the floor map
        for (Particle particle : particles) {
            if (false) { //particles violate the constraints
                particle.setWeight(0.0); // set particle weight/probability to 0
            }
        }

    }

    private void normalizeParticleWeights() {
        double totalWeight = 0.0;

        // compute total weight
        for (Particle particle : particles) {
            totalWeight += particle.getWeight();
        }

        // normalize particle weights
        for (Particle particle : particles) {
            particle.setWeight(particle.getWeight() / totalWeight);
        }
    }

    public void resampleParticles() {

        // compute particle cdf
        double[] cdf = new double[Ns];
        cdf[0] = 0.0;
        for (int i = 1; i < Ns; i++) {
            cdf[i] = cdf[i - 1] + particles[i].getWeight();
        }

        Random rng = new Random();
        double p_step = 1.0 / Ns; // probablility step size for resampling (new sample weight)
        double p_resample = (rng.nextDouble() - 1) * p_step;
        int cdf_idx = 0;

        for (int i = 0; i < Ns; i++) {
            p_resample += p_step;

            while (p_resample > cdf[cdf_idx])
                cdf_idx++;

            particles[i] = new Particle(particles[cdf_idx]);
            particles[i].setWeight(p_step);
        }
    }

}

