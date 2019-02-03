package com.example.workstation.basic;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;

public class AsyncRequest extends Thread
        /* Thread is used to make the request response cycle asynchronous to help preventing the UI from being Unresponsive*/
{
    final String TAG = AsyncRequest.class.getSimpleName();

    final String domain_name;
    BasicCookieStore store;
    /*Basic Cookie Store is Persistent Cookie Store implementation for Android used to store Cookie */
    String ResponseMsg = new String();  //Response message received from (Django)server
    int  ResponseCode;                  //Response code received from server (Code: (2xx for OK),(3xx for Redirects),(4xx for ClientError),(5xx for InternalSerer Error)
    String ResponseBody = new String(); //It is Data Received from Server(HTTP response or File response or JSON response)
    Map<String,List<String>> ResponseHeader;//Response Header Received from Server



    String Url = new String();             //Url to which to send request and response
    String RequestBody = new String();     //Request Body means Data to be sent to Server
    final String RequestType;              //Type of Request(GET,POST)

    AsyncRequest(String requestType,Context context)
    /* Context is accepted for CookieStore to initialize for the Application*/
    {
        RequestType = requestType;
        store=new BasicCookieStore(context);
        domain_name=context.getResources().getString(R.string.domain_name);
    }

    @TargetApi(Build.VERSION_CODES.N)
    @Override
    public void run()
    {
        try
        {
            URL url = new URL(Url);
            URI uri = new URI(Url);
            HttpURLConnection httpconn = (HttpURLConnection) url.openConnection();
            /*HttpURLConnection is the class which establish the connection between client and server and exchange data
            * using HTTP request response cycle.
            * url.openConnection() establishes the connection between client and server */
            httpconn.setInstanceFollowRedirects(false);
            /*Sets whether HTTP redirects (requests with response code 3xx) should be automatically followed by this HttpURLConnection
             instance*/
            HttpsURLConnection.setFollowRedirects(false);
            /*Sets whether HTTP redirects (requests with response code 3xx) should be automatically followed by this class*/
            httpconn.setRequestMethod(RequestType);//set Types of Request
            String S="";
            for(HttpCookie H:store.get(new URI(domain_name)))
                S+=H+"; ";
            httpconn.setRequestProperty("Cookie",S);
            /*retriving the cookie from cookie store and sending back to the server(session_id,csrf_token,etc)*/
            if(RequestType=="POST")
            {
                DataOutputStream output=new DataOutputStream(httpconn.getOutputStream());
                output.writeBytes(RequestBody);
                output.flush();
                output.close();
            }
            /* if the request is POST  then we send data to the server this using output stream received from connection*/

            boolean redirect = false;


            // normally, 3xx is redirect
            int status = httpconn.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {              //if request succeds then skip
                if (status == HttpURLConnection.HTTP_MOVED_TEMP
                        || status == HttpURLConnection.HTTP_MOVED_PERM
                        || status == HttpURLConnection.HTTP_SEE_OTHER)//if response code is 3xx then it is redirect request
                    redirect = true;                                  //set redirect to true
            }

            System.out.println("Response Code ... " + status);

            if(redirect) {
                // when response code 3xx then we receive redirect url in header field called "location"
                String newUrl = httpconn.getHeaderField("Location");

                // get the cookie if need, for login
                List<String> cookiesL =httpconn.getHeaderFields().get("set-cookie");
                Log.i(TAG, "run: "+httpconn.getHeaderFields());
                if(cookiesL != null)
                    for(String x:cookiesL)
                        store.add(new URI(domain_name),HttpCookie.parse(x).get(0));

                // open the new connnection again on url recived from location header
                url = new URL(domain_name+newUrl);
                uri = new URI(domain_name+newUrl);
                Log.i(TAG, "run: "+url);
                httpconn.disconnect();
                httpconn = (HttpURLConnection) url.openConnection();
                httpconn.setInstanceFollowRedirects(false);
                HttpURLConnection.setFollowRedirects(false);
                httpconn.setRequestMethod("GET"); //considered that redirect url will be GET request only
                S="";
                for(HttpCookie H:store.get(new URI(domain_name)))
                    S+=H+"; ";
                httpconn.setRequestProperty("Cookie",S);
                Log.i(TAG, "CookiesSession--: "+S);
                /*same as processed for first request*/

            }

            Log.i(TAG, "run: " + httpconn);

            this.ResponseMsg = httpconn.getResponseMessage(); //retriving  response message from httpconn object
            this.ResponseCode = httpconn.getResponseCode();//response code is retrived
            this.ResponseHeader = httpconn.getHeaderFields(); //getting header fields
            byte[] b = new byte[1024 * 1024]; // reserving the memory for responsebody
            int len;
            len = (new DataInputStream(httpconn.getInputStream())).read(b); //reads complete response body from httpconn object
            Log.i(TAG, "run: "+b.toString());
            this.ResponseBody = new String(b, 0, len); //stores in responsebody
            httpconn.disconnect();
        }
        catch(IOException e)
        {
            Log.e(TAG, "run: ",e );
        }
        catch (URISyntaxException e)
        {
            Log.e(TAG, "run: ",e );
        }

    }

    /*Getters and Setters*/

    void setUrl(String Url)
    {
        this.Url=Url;
    }


    void setRequestBody(String RequestBody)
    {
        this.RequestBody=RequestBody;
    }

    String getResponseMsg()
    {
        return ResponseMsg;
    }

    String getResponseBody()
    {
        return ResponseBody;
    }

    Map<String,List<String>> getResponseHeader()
    {
        return ResponseHeader;
    }
}
