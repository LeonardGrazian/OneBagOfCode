package com.example.mohawkgroup.openglpractice;

import android.content.Context;
import android.content.res.Resources;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

/**
 * Created by Mohawk Group on 6/26/2016.
 */
public class ModelLoader {
    private Resources resources;
    private Scanner data_scanner;

    // file must be in text format
    // file_name does not include file extension
    // file must be located in raw directory
    public ModelLoader(Context context, String file_name) {
        String text_data = null;
        try {
            text_data = LoadFile(context, file_name);
        } catch (IOException e) {
            e.printStackTrace();
        }

        data_scanner = new Scanner(text_data);
    }

    // normal should have 3 entries, vertices should be have 3x3 entries
    public boolean getNextFacet(double[] normal, double[][] vertices) {
        // data for each facet is stored in stl as follows:
//        facet normal 0 1 0
//        outer loop
//        vertex -17 -15 0
//        vertex -6.999 -15 10.001
//        vertex -6.999 -15 0
//        endloop
//        endfacet
        // we know the order of numerical data, so we skip text and just load sets of 12 numbers
        double[] ordered_facet_data = new double[12];
        int counter = 0;
        while(data_scanner.hasNext()) {
            if (data_scanner.hasNextDouble()) {
                ordered_facet_data[counter] = data_scanner.nextDouble();
                counter++;
            } else {
                data_scanner.next();
            }
            if (counter >= 12) {
                for (int i = 0; i < 3; i++) {
                    normal[i] = ordered_facet_data[i];
                }
                for (int i = 3; i < 12; i++) {
                    int j = i - 3;
                    vertices[j / 3][j % 3] = ordered_facet_data[i];
                }
                return true;
            }
        }
        return false;
    }

    //load file from apps res/raw folder
    public String LoadFile(Context context, String file_name) throws IOException {
        resources = context.getResources();
        //Create a InputStream to read the file into
        InputStream iS;

        //get the resource id from the file name
        int rID = resources.getIdentifier(file_name, "raw", context.getPackageName());
        //get the file as a stream
        iS = resources.openRawResource(rID);


        //create a buffer that has the same size as the InputStream
        byte[] buffer = new byte[iS.available()];
        //read the text file as a stream, into the buffer
        iS.read(buffer);
        //create a output stream to write the buffer into
        ByteArrayOutputStream oS = new ByteArrayOutputStream();
        //write this buffer to the output stream
        oS.write(buffer);
        //Close the Input and Output streams
        oS.close();
        iS.close();

        //return the output stream as a String
        return oS.toString();
    }
}
