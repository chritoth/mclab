package at.tugraz.mclab.sensors;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class KNN {
    private TrainRecord[] trainingSet;
    private final int K = 25;
    private final int NUM_CLASSES = 6;
    private final Metric metric = new EuclideanDistance();

    public KNN(InputStream trainingFile) {

        try {
            //read trainingSet and testingSet
            trainingSet = FileManager.readTrainFile(trainingFile);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int predict(double[] sample) {

        TrainRecord[] neighbors = findKNearestNeighbors(trainingSet, sample);

        int predictedLabel = classify(neighbors);

        //        int[] labelCounts = new int[NUM_CLASSES];
        //        for (int j = 0; j < K; j++)
        //            labelCounts[neighbors[j].classLabel]++;

        //        int predictedLabel = 0;
        //        int maxCount = -1;
        //        for (int j = 0; j < NUM_CLASSES; j++)
        //            if (labelCounts[j] > maxCount) {
        //                maxCount = labelCounts[j];
        //                predictedLabel = j;
        //            }

        return predictedLabel;
    }

    // Find K nearest neighbors of sample within trainingSet
    private TrainRecord[] findKNearestNeighbors(TrainRecord[] trainingSet, double[] sample) {

        TrainRecord[] neighbors = new TrainRecord[K];

        //initialization, put the first K trainRecords into the above arrayList
        int index;
        for (index = 0; index < K; index++) {
            trainingSet[index].distance = metric.getDistance(trainingSet[index].attributes, sample);
            neighbors[index] = trainingSet[index];
        }

        //go through the remaining records in the trainingSet to find K nearest neighbors
        for (index = K; index < trainingSet.length; index++) {
            trainingSet[index].distance = metric.getDistance(trainingSet[index].attributes, sample);

            //get the index of the neighbor with the largest distance to sample
            int maxIndex = 0;
            for (int i = 1; i < K; i++) {
                if (neighbors[i].distance > neighbors[maxIndex].distance)
                    maxIndex = i;
            }

            //add the current trainingSet[index] into neighbors if applicable
            if (neighbors[maxIndex].distance > trainingSet[index].distance)
                neighbors[maxIndex] = trainingSet[index];
        }

        return neighbors;
    }

    // Get the class label by using neighbors
    static int classify(TrainRecord[] neighbors) {
        //construct a HashMap to store <classLabel, weight>
        HashMap<Integer, Double> map = new HashMap<Integer, Double>();
        int num = neighbors.length;

        for (int index = 0; index < num; index++) {
            TrainRecord temp = neighbors[index];
            int key = temp.classLabel;

            //if this classLabel does not exist in the HashMap, put <key, 1/(temp.distance)> into
            // the HashMap
            if (!map.containsKey(key))
                map.put(key, 1 / temp.distance);

                //else, update the HashMap by adding the weight associating with that key
            else {
                double value = map.get(key);
                value += 1 / temp.distance;
                map.put(key, value);
            }
        }

        //Find the most likely label
        double maxSimilarity = 0;
        int returnLabel = -1;
        Set<Integer> labelSet = map.keySet();
        Iterator<Integer> it = labelSet.iterator();

        //go through the HashMap by using keys
        //and find the key with the highest weights
        while (it.hasNext()) {
            int label = it.next();
            double value = map.get(label);
            if (value > maxSimilarity) {
                maxSimilarity = value;
                returnLabel = label;
            }
        }

        return returnLabel;
    }

}

