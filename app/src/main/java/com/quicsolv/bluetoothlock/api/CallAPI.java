package com.quicsolv.bluetoothlock.api;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CallAPI extends AsyncTask<String, String, String> {

    private final OkHttpClient client = new OkHttpClient();
    private Request request;
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    private static final String JSON_DATA_TO_POST = "{\"userId\": 232202}";
    private static final String URL_TO_POST = "http://app.nokelock.com:8080/newNokelock/lock/getLockList";
    private static final String AUTH_TOKEN = "ff11dfc338d644c98d361cf029388af8";
    private String responseFinal = "";
    public AsyncResponse delegate = null;
    Context context;
    private ProgressDialog dialog;

        public CallAPI(Context context){
            //set context variables if required
            this.context = context;
            dialog = new ProgressDialog(context);
            dialog.show();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.setMessage("Retrieving the list of locks, please wait..");
        }

         @Override
         protected String doInBackground(String... params) {
             RequestBody body = RequestBody.create(JSON, JSON_DATA_TO_POST);

             request = new Request.Builder()
                     .url(URL_TO_POST)
                     .addHeader("clientType","Android")
                     .addHeader("token",AUTH_TOKEN)
                     .addHeader("language","en-US")
                     .addHeader("appVersion","1.0.9")
                     .addHeader("phoneModel","Nexus 5")
                     .addHeader("User-Agent","okhttp/3.11.0")
                     .addHeader("osVersion","7.1.2")
                     .post(body)
                     .build();

             try (Response response = client.newCall(request).execute()) {
                 if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                 Headers responseHeaders = response.headers();
                 for (int i = 0; i < responseHeaders.size(); i++) {
                     System.out.println(responseHeaders.name(i) + ": " + responseHeaders.value(i));
                 }
                 responseFinal = response.body().string();
                 System.out.println(responseFinal);
                 //responseFinal = response.body().string();
                 return responseFinal;
             }catch (IOException e)
             {
                 e.printStackTrace();
                 dialog.dismiss();
                 //Toast.makeText(context,"Error connecting to the Internet",Toast.LENGTH_SHORT).show();
                 delegate.processFailed("Failed");
             }
             return responseFinal;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            dialog.dismiss();
            delegate.processFinish(result);
        }
    }