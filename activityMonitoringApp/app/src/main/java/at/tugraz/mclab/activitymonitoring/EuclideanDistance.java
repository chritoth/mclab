package at.tugraz.mclab.activitymonitoring;

//This class implements Metric interface and is used to calculate EuclideanDistance
public class EuclideanDistance implements Metric {

    @Override
    //L2
    public double getDistance(double[] x, double[] y) {
        assert x.length == y.length : "vector dimensions do not match!";

        double sum2 = 0;
        for(int i = 0; i < x.length; i ++){
            sum2 += Math.pow(x[i] - y[i], 2);
        }

        return Math.sqrt(sum2);
    }

}
