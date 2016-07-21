package com.example.mohawkgroup.openglpractice;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.mohawkgroup.openglpractice.DisplayActivity;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MODEL_NAME =
            "com.example.mohawkgroup.openglpractice.MODEL_NAME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    // called when any of our buttons are clicked
    public void displayModel(View view) {
        // create intent and start display activity
        Intent intent = new Intent(this, DisplayActivity.class);
        // put extra
        Button b = (Button) view;
        String model_name = b.getText().toString();
        intent.putExtra(EXTRA_MODEL_NAME, model_name);
        // start activity
        startActivity(intent);
    }
}


