package at.tugraz.mclab.sensors;

//basic metric interface

public interface Metric {
    double getDistance(Record s, Record e);
}
