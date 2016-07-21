package com.example.mohawkgroup.openglpractice;

import android.content.Context;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Mohawk Group on 7/20/2016.
 */
public class DisplayActivity extends AppCompatActivity {
    public static final String TAG = "TimeTracker";
    private GLSurfaceView myGLView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        String model_name = intent.getStringExtra(MainActivity.EXTRA_MODEL_NAME);

        myGLView = new MyGLSurfaceView(this, model_name);
        setContentView(myGLView);
    }

    @Override
    protected void onResume() {
        // The activity must call the GL surface view's onResume() on activity onResume().
        super.onResume();
        myGLView.onResume();
    }

    @Override
    protected void onPause() {
        // The activity must call the GL surface view's onPause() on activity onPause().
        super.onPause();
        myGLView.onPause();
    }
}



/* Creates instance of myRenderer to render images to teh GLSurfaceView
 * Listens for and responds to touch events
 */
class MyGLSurfaceView extends GLSurfaceView {
    private final MyRenderer myRenderer;

    private final float TOUCH_SCALE_FACTOR = 90.0f / 320;
    private float myPreviousX_0;
    private float myPreviousY_0;
    private float myPreviousX_1;
    private float myPreviousY_1;

    public MyGLSurfaceView(Context c, String model_name) {
        super(c);

        setEGLContextClientVersion(2); // creates OpenGL ES 2.0 context

        myRenderer = new MyRenderer(model_name);
        myRenderer.setContext(c);

        setRenderer(myRenderer); // choose MyGLRenderer to execute graphics pipeline
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY); // render only if there's a state change
        requestRender(); // necessary?
    }

    // respond to touch event
    // currently only responds to finger dragging (causes model to rotate)
    // TODO: respond to other touch events (e.g. pinch to zoom)
    // TODO: major bug!!! Some multitaps cause image to disappear. RESOLVED: we were passing dx=dy=0
    // TODO: once above bug is fixed, use 2 fingers to rotate about z axis in obvious way
    @Override
    public boolean onTouchEvent(MotionEvent e) {
//        Log.i(DisplayActivity.TAG, String.valueOf(e.getAction()));

        float x_0 = e.getX(0);
        float y_0 = e.getY(0);

        int pointer_part = e.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK;
        int number_of_pointers = (pointer_part >> 2) + 1;
        int action_part =  e.getAction() & MotionEvent.ACTION_MASK;

        if (action_part == MotionEvent.ACTION_MOVE) {
            float dx = x_0 - myPreviousX_0;
            float dy = y_0 - myPreviousY_0;
            if (number_of_pointers == 1) {


                if ((Math.abs(dx) > 0.01) || (Math.abs(dy) > 0.01)) { // arbitrary cutoff to avoid passing zerovals
                    myRenderer.updateXYOrientation(dx * TOUCH_SCALE_FACTOR, -dy * TOUCH_SCALE_FACTOR);
                }

                requestRender();
            } else if (number_of_pointers == 2) {
                Log.i(DisplayActivity.TAG, "I'm being called!");
                float x_1 = e.getX(1);
                float y_1 = e.getY(1);

                float dx_1 = x_1 - myPreviousX_1;
                float dy_1 = y_1 - myPreviousY_1;

                // update orientation about z axis

                myPreviousX_1 = x_1;
                myPreviousY_1 = y_1;
            }
        }


        myPreviousX_0 = x_0;
        myPreviousY_0 = y_0;
        return true;
    }
}

