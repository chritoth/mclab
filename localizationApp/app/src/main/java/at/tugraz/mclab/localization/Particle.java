package at.tugraz.mclab.localization;

public class Particle {

    public static final double SMIN = 0.5; // minimum expected human stride length
    public static final double SMAX = 1.2; // maximum expected human stride length

    private double lastX; // previous x position coordinate
    private double lastY; // previous y position coordinate
    private double x; // x position coordinate
    private double y; // y position coordinate
    private double s; // stride length
    private double weight;

    public Particle() {
        x = 0.0;
        y = 0.0;
        lastX = 0.0;
        lastY = 0.0;
        s = 0.0;
    }

    public Particle(double x, double y, double s, double weight) {
        this.lastX = x;
        this.lastY = y;
        this.x = x;
        this.y = y;
        this.s = s;
        this.weight = weight;
    }

    public Particle(Particle particle) {
        this.lastX = particle.lastX;
        this.lastY = particle.lastY;
        this.x = particle.x;
        this.y = particle.y;
        this.s = particle.s;
        this.weight = particle.weight;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getS() {
        return s;
    }

    public void setS(double s) {
        this.s = s;
    }

    public double getLastX() {
        return lastX;
    }

    public void setLastX(double lastX) {
        this.lastX = lastX;
    }

    public double getLastY() {
        return lastY;
    }

    public void setLastY(double lastY) {
        this.lastY = lastY;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public void updateLastPosition() {
        lastX = x;
        lastY = y;
    }
}
