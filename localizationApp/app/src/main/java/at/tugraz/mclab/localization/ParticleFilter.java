package at.tugraz.mclab.localization;

import java.util.Random;

public class ParticleFilter {

    private static final double MAP_HEADING_OFFSET = -75.0; // map north orientation offset
    private static final double HEADING_STD_DEV = 10.0; // std deviation of the heading
    // uncertainty in degree
    private static final double STRIDE_UNCERTAINTY = 10.0; // uncertainty in % of the measured
    // stride
    private static final double STRIDE_MIN = 0.5; // minimum expected human stride length
    private static final double STRIDE_MAX = 1.2; // maximum expected human stride length

    private final int Ns; // number of particles
    public Particle[] particles;
    private FloorPlan floorPlan;

    public ParticleFilter(int Ns) {
        this.floorPlan = new FloorPlan();
        this.Ns = Ns;
        this.particles = new Particle[Ns];

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

                double x = room.getRoomCenter().getX() + room.getXLength() * rngPos.nextDouble();
                double y = room.getRoomCenter().getY() + room.getYLength() * rngPos.nextDouble();
                Position position = new Position(x, y);

                double stride = (STRIDE_MIN + STRIDE_MAX) / 2.0 + (STRIDE_MAX - STRIDE_MIN) *
                        rngStride.nextDouble();
                particles[particleIdx] = new Particle(position, stride, weight);
                particleIdx++;
            }

            if (particleIdx >= Ns)
                break;
        }
        assert particleIdx == Ns - 1;
    }

    public void moveParticles(int stepCount, double direction) {
        Random rngStride = new Random();
        Random rngHeading = new Random();

        for (Particle particle : particles) {

            double stride = stepCount * particle.getStrideLength();
            stride += 2.0 * STRIDE_UNCERTAINTY * stride * (rngStride.nextDouble() - 0.5);

            double heading = direction + HEADING_STD_DEV * rngHeading.nextGaussian() +
                    MAP_HEADING_OFFSET;
            heading = Math.toRadians(heading);

            // compute new position given the step count + heading
            double x = particle.getX() + stride + Math.cos(heading);
            double y = particle.getY() + stride + Math.sin(heading);

            particle.updateLastPosition();
            particle.setPosition(new Position(x, y));
        }
    }

    public void eliminateParticles() {

        // eliminate particles violating physical constraints given by the floor map
        for (Particle particle : particles) {
            for (Line wall : floorPlan.walls) {
                // check if the line of movement intersects any wall
                Line movementLine = new Line(particle.getLastPosition(), particle.getPosition());
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

            while (cdf_idx < (Ns - 1) && p_resample > cdf[cdf_idx])
                cdf_idx++;

            particles[i] = new Particle(particles[cdf_idx]);
            particles[i].setWeight(p_step);
        }
    }

}
