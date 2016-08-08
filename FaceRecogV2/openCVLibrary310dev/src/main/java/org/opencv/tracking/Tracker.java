
//
// This file is auto-generated. Please don't modify it!
//
package org.opencv.tracking;

import java.lang.String;
import org.opencv.core.Algorithm;

// C++: class Tracker
//javadoc: Tracker
public class Tracker extends Algorithm {

    protected Tracker(long addr) { super(addr); }


    //
    // C++: static Ptr_Tracker create(String trackerType)
    //

    //javadoc: Tracker::create(trackerType)
    public static Tracker create(String trackerType)
    {
        
        Tracker retVal = new Tracker(create_0(trackerType));
        
        return retVal;
    }


    //
    // C++:  bool init(Mat image, Rect2d boundingBox)
    //

    // Unknown type 'Rect2d' (I), skipping the function


    //
    // C++:  bool update(Mat image, Rect2d& boundingBox)
    //

    // Unknown type 'Rect2d' (O), skipping the function


    @Override
    protected void finalize() throws Throwable {
        delete(nativeObj);
    }



    // C++: static Ptr_Tracker create(String trackerType)
    private static native long create_0(String trackerType);

    // native support for java finalize()
    private static native void delete(long nativeObj);

}
