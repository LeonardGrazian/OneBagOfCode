package com.example.mohawkgroup.facerecogv2;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity
                            implements CameraBridgeViewBase.CvCameraViewListener2 {

    static {
        System.loadLibrary("opencv_java3");
    }

    // constants
    public static final String TAG = "OpenCVApp";
    public static final String NAME_EXTRA = "NameExtra";
    //    public static final String MAT_EXTRA = "MatrixExtra";
    private static final Scalar FACE_RECT_COLOR     = new Scalar(0, 255, 0, 255);
    public static final int        JAVA_DETECTOR       = 0;
    public static final int        NATIVE_DETECTOR     = 1;

    // for holding images (Mat's) and doing face detection
    private Mat selectedFace = null; // holds most recently selected face
    private Map<String, Integer> seenFaceNames = new HashMap<>();
    private Mat mRgba;
    private Mat mGray;
    private File mCascadeFile;
    private CascadeClassifier mJavaDetector;
//    private DetectionBasedTracker  mNativeDetector; // should not be used,  see mDetectorType

    // detector type
    private int mDetectorType = JAVA_DETECTOR; // let's see if we can do this with a java detector, then we can move on to a native detector
    private String[] mDetectorName; // set automatically in constructor

    private float mRelativeFaceSize   = 0.2f;
    private int mAbsoluteFaceSize   = 0;

    private MenuItem mItemFace50;
    private MenuItem mItemFace40;
    private MenuItem mItemFace30;
    private MenuItem mItemFace20;
    private MenuItem mItemType;

    // face recog
    private MyFaceRecognizer myFaceRecog;
    boolean isTraining = true;

    private CameraBridgeViewBase mOpenCvCameraView;

    public MainActivity() {
        mDetectorName = new String[2];
        mDetectorName[JAVA_DETECTOR] = "Java";
        mDetectorName[NATIVE_DETECTOR] = "Native (tracking)";

        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");

                    // Load native library after(!) OpenCV initialization
//                    System.loadLibrary("detection_based_tracker");

                    try {
                        // load cascade file from application resources
                        InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
                        FileOutputStream os = new FileOutputStream(mCascadeFile);

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();

                        mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                        if (mJavaDetector.empty()) {
                            Log.e(TAG, "Failed to load cascade classifier");
                            mJavaDetector = null;
                        } else
                            Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

//                        mNativeDetector = new DetectionBasedTracker(mCascadeFile.getAbsolutePath(), 0);

                        cascadeDir.delete();

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }

                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {



        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.HelloOpenCvView);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        myFaceRecog = new MyFaceRecognizer();

        final Button trainButton = (Button) findViewById(R.id.TrainButton);
        trainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // display the face we are logging
                // TODO: enable user to accept or reject this face
                if (selectedFace != null) {
                    // convert to bitmap
                    Bitmap bm = Bitmap.createBitmap(selectedFace.cols(),
                            selectedFace.rows(),
                            Bitmap.Config.ARGB_8888);
                    if ((selectedFace.dims() == 2)
                            && (selectedFace.rows() == bm.getHeight())
                            && (selectedFace.cols() == bm.getWidth())) {
                        Utils.matToBitmap(selectedFace, bm);
                    }

                    // display face
                    ImageView faceDisplay = (ImageView) findViewById(R.id.FaceDisplay);
                    faceDisplay.setImageBitmap(bm);

                    // update status message
                    String newMessage;
                    String name = ((EditText) findViewById(R.id.NameField)).getText().toString();
                    if (seenFaceNames.containsKey(name)) {
                        int face_occurrences = seenFaceNames.get(name) + 1;
                        seenFaceNames.put(name, face_occurrences);
                        newMessage = "We now have " + face_occurrences + " pictures of " + name;
                    } else {
                        seenFaceNames.put(name, 1);
                        newMessage = "This is the first time we've seen " + name;
                    }
                    TextView statusText = (TextView) findViewById(R.id.StatusUpdate);
                    statusText.setText(newMessage);

                    if (isTraining) {
                        myFaceRecog.addData(selectedFace, name);
                    }
                }


            }
        });

        final Button testButton = (Button) findViewById(R.id.TestButton);
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isTraining) {
                    // if training is successful we are done training
                    isTraining = !myFaceRecog.train();
                }

                // convert to bitmap
                Bitmap bm = Bitmap.createBitmap(selectedFace.cols(),
                        selectedFace.rows(),
                        Bitmap.Config.ARGB_8888);
                if ((selectedFace.dims() == 2)
                        && (selectedFace.rows() == bm.getHeight())
                        && (selectedFace.cols() == bm.getWidth())) {
                    Utils.matToBitmap(selectedFace, bm);
                }


                // display face
                ImageView faceDisplay = (ImageView) findViewById(R.id.FaceDisplay);
                faceDisplay.setImageBitmap(bm);

                TextView statusText = (TextView) findViewById(R.id.StatusUpdate);
                statusText.setText(myFaceRecog.test(selectedFace));

//                if (selectedFace != null) {
//                    int num_rows = 100;
//                    int num_cols = 64;
//                    Mat resizedFace = new Mat(num_rows, num_cols, CvType.CV_32SC1);
//                    Imgproc.resize(selectedFace, resizedFace, new Size(num_rows, num_cols));
//
//                    // convert to bitmap
//                    Bitmap bm = Bitmap.createBitmap(num_rows,
//                            num_cols,
//                            Bitmap.Config.ARGB_8888);
////                    System.out.println(resizedFace.dims());
////                    System.out.println(resizedFace.rows());
////                    System.out.println(resizedFace.cols());
////                    System.out.println(bm.getHeight());
////                    System.out.println(bm.getWidth());
//                    Utils.matToBitmap(resizedFace, bm);
//
////                    // display face
//                    ImageView faceDisplay = (ImageView) findViewById(R.id.FaceDisplay);
//                    faceDisplay.setImageBitmap(bm);
//                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
//        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, this, mLoaderCallback);
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat();
        mGray = new Mat();
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
        mGray.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
//        return inputFrame.rgba();

        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();

        if (mAbsoluteFaceSize == 0) {
            int height = mGray.rows();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }
//            mNativeDetector.setMinFaceSize(mAbsoluteFaceSize);
        }

        MatOfRect faces = new MatOfRect();

        if (mDetectorType == JAVA_DETECTOR) {
            if (mJavaDetector != null)
                mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2, 2, // TODO: wassup with all these params?
                        new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
        }
//        else if (mDetectorType == NATIVE_DETECTOR) {
//            if (mNativeDetector != null)
//                mNativeDetector.detect(mGray, faces);
//        }
        else {
            Log.e(TAG, "Detection method is not selected!");
        }

        Rect[] facesArray = faces.toArray();
        int number_of_detected_faces = facesArray.length;
        if (number_of_detected_faces == 1) {
            // only one face detected
            selectedFace = new Mat(mGray, facesArray[0]); // crop image to only include face
        }

        for (int i = 0; i < number_of_detected_faces; i++) {
            Imgproc.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);
        }

        return mRgba;

//        Size newSize = new Size(mGray.cols(), mGray.rows());
//        Mat resizedGray = new Mat(newSize, CvType.CV_64FC1); //CvType.CV_8UC1); //CvType.CV_32SC1);
//        Imgproc.resize(mGray, resizedGray, newSize);
//        return resizedGray;

//        Mat temp = mGray.clone();
////        mGray.copyTo(temp); // redundant
//        return temp;

//        return mGray.reshape()
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "called onCreateOptionsMenu");
        mItemFace50 = menu.add("Face size 50%");
        mItemFace40 = menu.add("Face size 40%");
        mItemFace30 = menu.add("Face size 30%");
        mItemFace20 = menu.add("Face size 20%");
        mItemType   = menu.add(mDetectorName[mDetectorType]);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
        if (item == mItemFace50)
            setMinFaceSize(0.5f);
        else if (item == mItemFace40)
            setMinFaceSize(0.4f);
        else if (item == mItemFace30)
            setMinFaceSize(0.3f);
        else if (item == mItemFace20)
            setMinFaceSize(0.2f);
        else if (item == mItemType) {
            int tmpDetectorType = (mDetectorType + 1) % mDetectorName.length;
            item.setTitle(mDetectorName[tmpDetectorType]);
            setDetectorType(tmpDetectorType);
        }
        return true;
    }

    private void setMinFaceSize(float faceSize) {
        mRelativeFaceSize = faceSize;
        mAbsoluteFaceSize = 0;
    }

    private void setDetectorType(int type) {
        if (mDetectorType != type) {
            mDetectorType = type;

//            if (type == NATIVE_DETECTOR) {
//                Log.i(TAG, "Detection Based Tracker enabled");
//                mNativeDetector.start();
//            } else {
//                Log.i(TAG, "Cascade detector enabled");
//                mNativeDetector.stop();
//            }
        }
    }
}
