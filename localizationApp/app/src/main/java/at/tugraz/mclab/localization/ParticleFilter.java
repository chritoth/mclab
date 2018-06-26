package at.tugraz.mclab.localization;

import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

public class ParticleFilter {

    public static final double MAP_Y_HEADING_OFFSET = -63.0; // map north orientation offset for y
    // axis (0° is y axis)
    private static final double HEADING_STD_DEV = 25.0; // std deviation of the heading
    // uncertainty in degree
    private static final double STRIDE_UNCERTAINTY = 0.10; // uncertainty in % of the measured
    // stride
    private static final double STRIDE_MIN = 0.5; // minimum expected human stride length
    private static final double STRIDE_MAX = 1.2; // maximum expected human stride length

    private final int Ns; // number of particles
    public Particle[] particles;
    private FloorPlan floorPlan;
    public Position currentPosition;

    public ParticleFilter(int Ns) {
        this.floorPlan = new FloorPlan();
        this.Ns = Ns;
        this.particles = new Particle[Ns];
        currentPosition = new Position();

        generateInitialParticles();
    }

    private void generateInitialParticles() {
        Random rngPos = new Random();
        Random rngStride = new Random();

        // particle weight: initially we have equally probable particles
        double weight = 1.0 / Ns;

        int particleIdx = 0;
        int numberOfParticles = 0;
        for (Room room : floorPlan.rooms) {

            // compute the aliquot number of particles for this room (w.r.t. the total area)
            numberOfParticles += (int) Math.round(Ns * room.area / floorPlan.totalArea);

            // generate uniformly distributed particles all over the room
            while (particleIdx < numberOfParticles && particleIdx < Ns) {

                double x = room.getRoomCenter().getX() + room.getXLength() * (rngPos.nextDouble() - 0.5);
                double y = room.getRoomCenter().getY() + room.getYLength() * (rngPos.nextDouble() - 0.5);
                Position position = new Position(x, y);

                double stride = (STRIDE_MIN + STRIDE_MAX) / 2.0 + (STRIDE_MAX - STRIDE_MIN) * (rngStride.nextDouble()
                        - 0.5);
                particles[particleIdx] = new Particle(position, stride, weight);
                particleIdx++;
            }

            if (particleIdx >= Ns)
                break;
        }
        assert particleIdx == Ns - 1;
    }

    public void moveParticles(double stepCount, double azimuth) {
        Random rngStride = new Random();
        Random rngHeading = new Random();

        for (Particle particle : particles) {

            double stride = stepCount * particle.getStrideLength();
            stride += 2.0 * STRIDE_UNCERTAINTY * stride * (rngStride.nextDouble() - 0.5);

            // we have to take the negative azimuth to compensate for map and world coordinate
            // system dispute
            double heading = -azimuth + HEADING_STD_DEV * rngHeading.nextGaussian() - MAP_Y_HEADING_OFFSET;
            heading = Math.toRadians(heading);

            // compute new position given the step count + heading
            double x = particle.getX() + stride * Math.sin(heading);
            double y = particle.getY() + stride * Math.cos(heading);

            particle.updateLastPosition();
            particle.setPosition(new Position(x, y));
        }
    }

    public void eliminateParticles() {

        // eliminate particles violating physical constraints given by the floor map
        for (Particle particle : particles) {
            Line movementLine = new Line(particle.getLastPosition(), particle.getPosition());

            for (Line wall : floorPlan.walls) {
                // check if the line of movement intersects any wall
                if (movementLine.intersects(wall)) {
                    particle.setWeight(0.0); // set particle weight/probability to 0
                }
            }
        }

    }

    public void normalizeParticleWeights() {
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
        Particle[] resampledParticles = new Particle[Ns];

        // compute particle cdf
        double[] cdf = new double[Ns];
        cdf[0] = 0.0;
        for (int i = 1; i < Ns; i++) {
            cdf[i] = cdf[i - 1] + particles[i].getWeight();
        }

        Random rng = new Random();
        double p_step = 1.0 / Ns; // probability step size for resampling (new sample weight)
        double p_resample = (rng.nextDouble() - 1) * p_step;
        int cdf_idx = 0;

        for (int i = 0; i < Ns; i++) {
            p_resample += p_step;

            while (cdf_idx < (Ns - 1) && (p_resample > cdf[cdf_idx] || particles[cdf_idx].getWeight() == 0.0)) {
                cdf_idx++;
            }

            // if the resample particle weight is 0.0 (should only occur for the last part of the
            // cdf) then we take a
            // particle with non-zero weight..
            if (particles[cdf_idx].getWeight() == 0.0)
                resampledParticles[i] = new Particle(resampledParticles[i - 1]);
            else
                resampledParticles[i] = new Particle(particles[cdf_idx]);

            resampledParticles[i].setWeight(p_step);
        }

        particles = resampledParticles;
    }

    public void updateCurrentPosition(boolean mapEstimate) {

        Position position = new Position();

        if (mapEstimate) {
            double max_weight = -1.0;
            for (Particle particle : particles) {
                if (particle.getWeight() > max_weight) {
                    max_weight = particle.getWeight();
                    position = new Position(particle.getPosition());
                }
            }
        } else {
            // take median of all particle positions
            double[] x = new double[Ns];
            double[] y = new double[Ns];
            for (int i = 0; i < Ns; i++) {
                x[i] = particles[i].getX();
                y[i] = particles[i].getY();
            }

            Arrays.sort(x);
            Arrays.sort(y);

            position = new Position(x[Ns / 2], y[Ns / 2]);
        }

        currentPosition = position;
    }

}

