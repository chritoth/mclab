package at.tugraz.mclab.sensors;

//Basic Record class
public class Record {
    double[] attributes;
    int classLabel;
    Record(){}
    Record(double[] attributes, int classLabel){
        this.attributes = attributes;
        this.classLabel = classLabel;
    }

}
