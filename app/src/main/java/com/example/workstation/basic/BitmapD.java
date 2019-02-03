package com.example.workstation.basic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class BitmapD extends Thread {

    Bitmap B;
    String Url;

    public void setUrl(String Url)
    {
        this.Url=Url;
    } //Url from which to fetch image

    public void run()
    {
        try {
            Log.e("src",Url);
            URL url = new URL(Url); // converts string url to URL object
            HttpURLConnection connection = (HttpURLConnection) url.openConnection(); //establishes connection between client and server
            connection.setDoInput(true);
            connection.connect();//connection is established
            InputStream input = connection.getInputStream();//retriving input stream to retrive image data
            B= BitmapFactory.decodeStream(input);//convert input received to proper image format depending on header
            Log.e("Bitmap","returned");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Exception",e.getMessage());
        }
    }

    public Bitmap getBitmap()
    {
        return B;
    }//getter for fetching bitmap
}
