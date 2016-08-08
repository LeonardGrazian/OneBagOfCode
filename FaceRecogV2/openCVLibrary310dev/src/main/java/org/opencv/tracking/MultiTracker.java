
//
// This file is auto-generated. Please don't modify it!
//
package org.opencv.tracking;

import java.lang.String;

// C++: class MultiTracker
//javadoc: MultiTracker
public class MultiTracker {

    protected final long nativeObj;
    protected MultiTracker(long addr) { nativeObj = addr; }


    //
    // C++:   MultiTracker(String trackerType = "")
    //

    //javadoc: MultiTracker::MultiTracker(trackerType)
    public   MultiTracker(String trackerType)
    {
        
        nativeObj = MultiTracker_0(trackerType);
        
        return;
    }

    //javadoc: MultiTracker::MultiTracker()
    public   MultiTracker()
    {
        
        nativeObj = MultiTracker_1();
        
        return;
    }


    //
    // C++:  bool add(Mat image, Rect2d boundingBox)
    //

    // Unknown type 'Rect2d' (I), skipping the function


    //
    // C++:  bool add(Mat image, vector_Rect2d boundingBox)
    //

    // Unknown type 'vector_Rect2d' (I), skipping the function


    //
    // C++:  bool add(String trackerType, Mat image, Rect2d boundingBox)
    //

    // Unknown type 'Rect2d' (I), skipping the function


    //
    // C++:  bool add(String trackerType, Mat image, vector_Rect2d boundingBox)
    //

    // Unknown type 'vector_Rect2d' (I), skipping the function


    //
    // C++:  bool update(Mat image, vector_Rect2d& boundingBox)
    //

    // Unknown type 'vector_Rect2d' (O), skipping the function


    @Override
    protected void finalize() throws Throwable {
        delete(nativeObj);
    }



    // C++:   MultiTracker(String trackerType = "")
    private static native long MultiTracker_0(String trackerType);
    private static native long MultiTracker_1();

    // native support for java finalize()
    private static native void delete(long nativeObj);

}
