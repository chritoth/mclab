package at.tugraz.mclab.localization;

public class Motion {

    public boolean isMoving;
    public double frequency;
    public double angle;

    public Motion() {
        isMoving = false;
        frequency = 0.0;
        angle = 0.0;
    }

    public Motion(boolean isMoving) {
        this.isMoving = isMoving;
        frequency = 0.0;
        angle = 0.0;
    }

    public Motion(boolean isMoving, double frequency, double angle) {
        this.isMoving = isMoving;
        this.frequency = frequency;
        this.angle = angle;
    }

}
