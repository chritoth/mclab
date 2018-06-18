package at.tugraz.mclab.activitymonitoring;

//FileManager
// * ReadFile: read training files and test files
// * OutputFile: output predicted labels into a file

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Scanner;

public class FileManager {

    //read training files
    public static TrainRecord[] readTrainFile(InputStream fileName) throws IOException {
        //File file = new File(fileName);
        Scanner scanner = new Scanner(fileName);

        //read file
        int NumOfSamples = scanner.nextInt();
        int NumOfAttributes = scanner.nextInt();
        int LabelOrNot = scanner.nextInt();
        scanner.nextLine();

        assert LabelOrNot == 1 : "No classLabel";// ensure that C is present in this file

        //transform data from file into TrainRecord objects
        TrainRecord[] records = new TrainRecord[NumOfSamples];
        int index = 0;
        while (scanner.hasNext()) {
            scanner.nextInt();
            double[] attributes = new double[NumOfAttributes];
            int classLabel = -1;

            //Read a whole line for a TrainRecord
            for (int i = 0; i < NumOfAttributes; i++) {
                attributes[i] = Double.parseDouble(scanner.next());
            }

            //Read classLabel
            classLabel = (int) scanner.nextDouble();
            assert classLabel != -1 : "Reading class label is wrong!";

            records[index] = new TrainRecord(attributes, classLabel);
            index++;
        }

        return records;
    }
}
