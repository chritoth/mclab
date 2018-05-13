package at.tugraz.mclab.sensors;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class knn
{
    private  TrainRecord[] trainingSet;
    private  int K;
    private Metric metric;

    public  knn(InputStream trainingFile, int k){
        K=k;
        metric = new EuclideanDistance();

        try {
            //read trainingSet and testingSet
            trainingSet =  FileManager.readTrainFile(trainingFile);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int calculateKnn(double[] testSample, int K)
    {
        TestRecord[] testingSet = convertToTestRecord(testSample);
        // make sure the input arguments are legal
        if(K <= 0){
            System.out.println("K should be larger than 0!");
            return -1;
        }

        try {

            //test those TestRecords one by one
            int numOfTestingRecord = testingSet.length;
            for(int i = 0; i < numOfTestingRecord; i ++){
                TrainRecord[] neighbors = findKNearestNeighbors(trainingSet, testingSet[i], K, metric);
                int classLabel = classify(neighbors);
                testingSet[i].predictedLabel = classLabel; //assign the predicted label to TestRecord
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return testingSet[0].predictedLabel;
    }

    // Find K nearest neighbors of testRecord within trainingSet
    static TrainRecord[] findKNearestNeighbors(TrainRecord[] trainingSet, TestRecord testRecord,int K, Metric metric){
        int NumOfTrainingSet = trainingSet.length;
        assert K <= NumOfTrainingSet : "K is lager than the length of trainingSet!";

        //Update KNN: take the case when testRecord has multiple neighbors with the same distance into consideration
        //Solution: Update the size of container holding the neighbors
        TrainRecord[] neighbors = new TrainRecord[K];

        //initialization, put the first K trainRecords into the above arrayList
        int index;
        for(index = 0; index < K; index++){
            trainingSet[index].distance = metric.getDistance(trainingSet[index], testRecord);
            neighbors[index] = trainingSet[index];
        }

        //go through the remaining records in the trainingSet to find K nearest neighbors
        for(index = K; index < NumOfTrainingSet; index ++){
            trainingSet[index].distance = metric.getDistance(trainingSet[index], testRecord);

            //get the index of the neighbor with the largest distance to testRecord
            int maxIndex = 0;
            for(int i = 1; i < K; i ++){
                if(neighbors[i].distance > neighbors[maxIndex].distance)
                    maxIndex = i;
            }

            //add the current trainingSet[index] into neighbors if applicable
            if(neighbors[maxIndex].distance > trainingSet[index].distance)
                neighbors[maxIndex] = trainingSet[index];
        }

        return neighbors;
    }

    //convert samples array to the TestRecord array format with just one entry
    static TestRecord[] convertToTestRecord (double[] sample)
    {
        int NumOfSamples = 1;
        TestRecord[] records = new TestRecord[NumOfSamples];
        records[0] = new TestRecord();
        records[0].attributes = sample.clone();
        records[0].classLabel = -1;
        return records;
    }

    // Get the class label by using neighbors
    static int classify(TrainRecord[] neighbors){
        //construct a HashMap to store <classLabel, weight>
        HashMap<Integer, Double> map = new HashMap<Integer, Double>();
        int num = neighbors.length;

        for(int index = 0;index < num; index ++){
            TrainRecord temp = neighbors[index];
            int key = temp.classLabel;

            //if this classLabel does not exist in the HashMap, put <key, 1/(temp.distance)> into the HashMap
            if(!map.containsKey(key))
                map.put(key, 1 / temp.distance);

                //else, update the HashMap by adding the weight associating with that key
            else{
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
        while(it.hasNext()){
            int label = it.next();
            double value = map.get(label);
            if(value > maxSimilarity){
                maxSimilarity = value;
                returnLabel = label;
            }
        }

        return returnLabel;
    }

}

