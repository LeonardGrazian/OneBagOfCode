package com.example.mohawkgroup.openglpractice;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;

public class MainActivity extends AppCompatActivity {

    private GLSurfaceView myGLView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);

        myGLView = new MyGLSurfaceView(this);
        setContentView(myGLView);
    }

    @Override
    protected void onResume()
    {
        // The activity must call the GL surface view's onResume() on activity onResume().
        super.onResume();
        myGLView.onResume();
    }

    @Override
    protected void onPause()
    {
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
    private float myPreviousX;
    private float myPreviousY;

    public MyGLSurfaceView(Context c) {
        super(c);

        setEGLContextClientVersion(2); // creates OpenGL ES 2.0 context

        myRenderer = new MyRenderer();
        myRenderer.setContext(c);

//        setRenderMode(RENDERMODE_WHEN_DIRTY);

        setRenderer(myRenderer); // choose MyGLRenderer to execute graphics pipeline
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY); // render only if there's a state change
        requestRender(); // necessary?
    }

    // respond to touch event
    // currently only responds to finger dragging (causes model to rotate)
    // TODO: respond to other touch events (e.g. pinch to zoom)
    // TODO: major bug!!! hold finger down, tap with another finger, image disappears, why? rotation matrix must me set to 0's, but how and how to prevent this?
    // TODO: once above bug is fixed, use 2 fingers to rotate about z axis in obvious way
    @Override
    public boolean onTouchEvent(MotionEvent e) {

        float x = e.getX();
        float y = e.getY();

        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:

                float dx = x - myPreviousX;
                float dy = y - myPreviousY;

//                // reverse direction of rotation above the mid-line
//                if (y > getHeight() / 2) {
//                    dx = dx * -1 ;
//                }
//
//                // reverse direction of rotation to left of the mid-line
//                if (x < getWidth() / 2) {
//                    dy = dy * -1 ;
//                }

                myRenderer.updateOrientation(dx * TOUCH_SCALE_FACTOR, -dy * TOUCH_SCALE_FACTOR);

//                myRenderer.setYAngle(
//                        myRenderer.getYAngle() -
//                                (dx * TOUCH_SCALE_FACTOR));
//                myRenderer.setXAngle(
//                        myRenderer.getXAngle() -
//                                (dy * TOUCH_SCALE_FACTOR));

                requestRender();
        }

        myPreviousX = x;
        myPreviousY = y;
        return true;
    }
}
