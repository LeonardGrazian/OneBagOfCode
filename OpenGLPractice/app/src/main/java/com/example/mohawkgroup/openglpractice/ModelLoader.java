package com.example.mohawkgroup.openglpractice;

import android.content.Context;
import android.content.res.Resources;
import android.provider.BaseColumns;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by Mohawk Group on 6/26/2016.
 */
public class ModelLoader {
    public static final int FILE_MODE = 0;
    public static final int CLOUD_MODE = 1;
    private Resources resources;
    private Scanner data_scanner;

    // file must be in text format
    // file_name does not include file extension
    // file must be located in raw directory
    public ModelLoader(Context context, String file_name, int mode) {
        String text_data = "";

        if (mode == FILE_MODE) {
            Log.i(DisplayActivity.TAG, "Loading " + file_name + " from local file");

            try {
                text_data = LoadFile(context, file_name);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (mode == CLOUD_MODE) {
            Log.i(DisplayActivity.TAG, "Loading " + file_name + " from cloud");

            // cloud constants
            String pageExtension = "stl"; // "hello" "db" "sci" ""
            // file_name is name of table where data is stored
            String serverURL = "https://benefique-livre-59642.herokuapp.com/"
                    + pageExtension + "?param1=" + file_name;
            String charset = "UTF-8";

            try {

                URL url = new URL(serverURL); //"http://localhost:5000/");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                // conn.connect; // io streams implicitly call this
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept-Charset", charset); // only necessary when we pass params

                // getResponseCode() returns 200 if connection is OK
                // returns 401 if connection is unauthorized
                // returns -1 if not code can be discerned (e.g. not a valid http)
                if (conn.getResponseCode() != 200) {
                    throw new RuntimeException("Failed : HTTP error code : "
                            + conn.getResponseCode());
                }

                BufferedReader br = new BufferedReader(new InputStreamReader(
                        (conn.getInputStream())));

                String temp;
//            System.out.println("Output from Server .... \n");
                while ((temp = br.readLine()) != null) { // output = br.readLine()
                    text_data = text_data + "\n" + temp;
//                System.out.println(output);
                }
                conn.disconnect();

            } catch (MalformedURLException e) {

                e.printStackTrace();

            } catch (IOException e) {

                e.printStackTrace();

            }
        }
        Log.i(DisplayActivity.TAG, "Loading complete");

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
        data_scanner = null;
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