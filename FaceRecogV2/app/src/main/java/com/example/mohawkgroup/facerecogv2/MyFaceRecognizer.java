package com.example.mohawkgroup.facerecogv2;

import android.content.Intent;

import java.util.LinkedList;
import java.util.List;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

// face recognition libs
import org.opencv.face.Face;
import org.opencv.face.FaceRecognizer;

/**
 * Created by Mohawk Group on 8/3/2016.
 */
public class MyFaceRecognizer {
    // constants
    private static int num_rows = 256;
    private static int num_cols = 256;
    private static int num_pixels = num_rows * num_cols;
    boolean isTrained = false; // is flipped to true when train is called

    private FaceRecognizer myRecog;
    List<Mat> trainingFaces;
    List<Integer> faceLabels; // each element is an index of a name in faceNames
    List<String> faceNames;

    public MyFaceRecognizer() {
        trainingFaces = new LinkedList<>();
        faceLabels = new LinkedList<>();
        faceNames = new LinkedList<>();

//        myRecog = Face.createEigenFaceRecognizer(); // TODO: add option to choose type of recognizer
//        myRecog = Face.createFisherFaceRecognizer();
        myRecog = Face.createLBPHFaceRecognizer();
    }

    public void addData(Mat faceData, String name) {
//        System.out.println(faceData.type());
        trainingFaces.add(resizeMat(faceData));
        if (!faceNames.contains(name)) {
            faceNames.add(name);
        }
        faceLabels.add(faceNames.indexOf(name));
    }

    public boolean train() {
        if (trainingFaces.size() > 1) {
//            Face.createEigenFaceRecognizer(trainingFaces, intListToMat(faceLabels));
            myRecog.train(trainingFaces, intListToMat(faceLabels));
            isTrained = true;
            return true;
        } else {
            // We do not have enough faces to train
            return false;
        }


//        intListToMat(faceLabels);
    }

    public String test(Mat testFace) {
        if (isTrained) {
            int winnerIndex = myRecog.predict_label(resizeMat(testFace)); // TODO: put in safegaurds to ensure we have enough training data
            return faceNames.get(winnerIndex);
        } else {
            return "Error! Model is not trained yet";
        }

//        return "test";
    }

    /*** STATIC METHODS ***/

    private static Mat resizeMat(Mat opencvMat) {
        Mat resizedFace = new Mat(num_rows, num_cols, CvType.CV_8UC1); //CV_64FC1);
        Imgproc.resize(opencvMat, resizedFace, new Size(num_rows, num_cols));
        return resizedFace;
    }

    private static Mat intListToMat(List<Integer> intList) {
        Mat intMat = new Mat(1, intList.size(), CvType.CV_32SC1);
        int counter = 0;
        for (int myInt : intList) {
            int[] myIntCastArray = { myInt };
            intMat.put(0, counter, myIntCastArray);

//            System.out.println("My int = " + myInt);
//            System.out.println("My float = " + myIntCastArray[0]);
//            float[] tempfloat = new float[1];
//            intMat.get(counter, 0, tempfloat);
//            System.out.println(tempfloat[0]);

            counter++;
        }

//        for (int i = 0; i < counter; i++) {
//            System.out.println(intMat.get(i, 0)[0]);
//        }

        return intMat;
    }

    private static int[] intListToArray(List<Integer> intList) {
        int num_ints = intList.size();
        int[] intArray = new int[num_ints];

        int counter = 0;
        for (int myInt : intList) {
            intArray[counter++] = myInt;
        }

        return intArray;
    }

    private static int getWinnerIndex(int[] nameIndices, double[] certainties) {
        double maxCertainty = -Double.MAX_VALUE;
        int winnerIndex = -1;
        for (int i = 0; i < certainties.length; i++) {
            double thisCertainty = certainties[i];
            if (thisCertainty > maxCertainty) {
                maxCertainty = thisCertainty;
                winnerIndex = i;
            }
        }
        return winnerIndex;
    }
}
