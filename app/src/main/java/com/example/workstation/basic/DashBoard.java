package com.example.workstation.basic;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DashBoard extends AppCompatActivity {

    boolean flag=true;
    String domain_name;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_board);

        this.domain_name =this.getResources().getString(R.string.domain_name);

        BitmapD B=new BitmapD();

        Intent DataIntent = getIntent(); //receiving the intent

        JSONArray obj = new JSONArray();
        try {
            obj = new JSONArray(DataIntent.getStringExtra("displayData"));
            //the received response from server was JSON serialized coverting it back to JSON Array
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            Log.i("Json", "SignIN: "+((JSONObject)((JSONObject)obj.get(0)).get("fields")).get("username"));
        } catch (JSONException e) {

            e.printStackTrace();
        }
        //selectiong differnt elements from UI(xml)
        TextView username = (TextView) findViewById(R.id.username);
        TextView firstname = (TextView) findViewById(R.id.firstname);
        TextView lastname = (TextView) findViewById(R.id.lastname);
        TextView email = (TextView) findViewById(R.id.email);
        ImageView I =(ImageView) findViewById(R.id.ProfilePic);

        try
        {
            //setting appropiate value from JSON Array
            username.setText("username : "+((JSONObject)((JSONObject)obj.get(0)).get("fields")).get("username"));
            lastname.setText(""+((JSONObject)((JSONObject)obj.get(0)).get("fields")).get("last_name"));
            firstname.setText(""+((JSONObject)((JSONObject)obj.get(0)).get("fields")).get("first_name"));
            email.setText("Email : "+((JSONObject)((JSONObject)obj.get(0)).get("fields")).get("email"));
            B.setUrl(domain_name+"/media/"+((JSONObject)((JSONObject)obj.get(1)).get("fields")).get("profile_pic"));
            //setting image url to BitmapD object which loads the image on other thread
            B.start();
            B.join();
            Bitmap bm=B.getBitmap();
            I.setImageBitmap(bm);

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbardb, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //triggers on clicking close icon  and finishes activity
    public void Close(MenuItem i)
    {
        this.finish();
    }

    //triggers on clicking logout icon and destroys the current session and finishes activity
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void LogOut(MenuItem i) throws InterruptedException {
        AsyncRequest P=new AsyncRequest("GET",this);
        P.setUrl(domain_name+this.getResources().getString(R.string.Logout)); //sends logout request
        P.start();
        P.join();
        this.finish();
    }

}
