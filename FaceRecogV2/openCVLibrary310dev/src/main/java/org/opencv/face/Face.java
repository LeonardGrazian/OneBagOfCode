
//
// This file is auto-generated. Please don't modify it!
//
package org.opencv.face;


import org.opencv.core.Mat;
import org.opencv.utils.Converters;

import java.util.List;

public class Face {

    //
    // C++:  Ptr_BasicFaceRecognizer createEigenFaceRecognizer(int num_components = 0, double threshold = DBL_MAX)
    //

    //javadoc: createEigenFaceRecognizer(num_components, threshold)
    public static BasicFaceRecognizer createEigenFaceRecognizer(int num_components, double threshold)
    {
        
        BasicFaceRecognizer retVal = new BasicFaceRecognizer(createEigenFaceRecognizer_0(num_components, threshold));
        
        return retVal;
    }

    //javadoc: createEigenFaceRecognizer()
    public static BasicFaceRecognizer createEigenFaceRecognizer()
    {
        long tempPtr = createEigenFaceRecognizer_1();
        System.out.println("address in java = " + tempPtr);


        BasicFaceRecognizer retVal = new BasicFaceRecognizer(tempPtr);
        
        return retVal;
    }

    // lenny special
    public static void createEigenFaceRecognizer(List<Mat> trainingFaces, Mat labels) {

        Mat src_mat = Converters.vector_Mat_to_Mat(trainingFaces);
//        createEigenFaceRecognizer_2(src_mat.nativeObj, labels.nativeObj);

    }


    //
    // C++:  Ptr_BasicFaceRecognizer createFisherFaceRecognizer(int num_components = 0, double threshold = DBL_MAX)
    //

    //javadoc: createFisherFaceRecognizer(num_components, threshold)
    public static BasicFaceRecognizer createFisherFaceRecognizer(int num_components, double threshold)
    {
        
        BasicFaceRecognizer retVal = new BasicFaceRecognizer(createFisherFaceRecognizer_0(num_components, threshold));
        
        return retVal;
    }

    //javadoc: createFisherFaceRecognizer()
    public static BasicFaceRecognizer createFisherFaceRecognizer()
    {
        
        BasicFaceRecognizer retVal = new BasicFaceRecognizer(createFisherFaceRecognizer_1());
        
        return retVal;
    }


    //
    // C++:  Ptr_LBPHFaceRecognizer createLBPHFaceRecognizer(int radius = 1, int neighbors = 8, int grid_x = 8, int grid_y = 8, double threshold = DBL_MAX)
    //

    //javadoc: createLBPHFaceRecognizer(radius, neighbors, grid_x, grid_y, threshold)
    public static LBPHFaceRecognizer createLBPHFaceRecognizer(int radius, int neighbors, int grid_x, int grid_y, double threshold)
    {
        
        LBPHFaceRecognizer retVal = new LBPHFaceRecognizer(createLBPHFaceRecognizer_0(radius, neighbors, grid_x, grid_y, threshold));
        
        return retVal;
    }

    //javadoc: createLBPHFaceRecognizer()
    public static LBPHFaceRecognizer createLBPHFaceRecognizer()
    {
        
        LBPHFaceRecognizer retVal = new LBPHFaceRecognizer(createLBPHFaceRecognizer_1());
        
        return retVal;
    }




    // C++:  Ptr_BasicFaceRecognizer createEigenFaceRecognizer(int num_components = 0, double threshold = DBL_MAX)
    private static native long createEigenFaceRecognizer_0(int num_components, double threshold);
    private static native long createEigenFaceRecognizer_1();

    // C++:  Ptr_BasicFaceRecognizer createFisherFaceRecognizer(int num_components = 0, double threshold = DBL_MAX)
    private static native long createFisherFaceRecognizer_0(int num_components, double threshold);
    private static native long createFisherFaceRecognizer_1();

    // C++:  Ptr_LBPHFaceRecognizer createLBPHFaceRecognizer(int radius = 1, int neighbors = 8, int grid_x = 8, int grid_y = 8, double threshold = DBL_MAX)
    private static native long createLBPHFaceRecognizer_0(int radius, int neighbors, int grid_x, int grid_y, double threshold);
    private static native long createLBPHFaceRecognizer_1();

}
