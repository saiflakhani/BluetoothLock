package com.quicsolv.bluetoothlock;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.google.gson.Gson;
import com.quicsolv.bluetoothlock.api.AsyncResponse;
import com.quicsolv.bluetoothlock.api.CallAPI;
import com.quicsolv.bluetoothlock.api.TokenAPI;
import com.quicsolv.bluetoothlock.pojo.LockProperties;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SelectLockActivity extends AppCompatActivity implements AsyncResponse {


    String responseData = "";
    ArrayList<LockProperties> listOfLockProperties = new ArrayList<>();
    ArrayList<String> listOfLocks = new ArrayList<>();
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_lock);
        context = this;

        TokenAPI tokenAPI = new TokenAPI(context);
        tokenAPI.delegate = this;
        tokenAPI.execute();
    }


    @Override
    public void processFailed(String output) {
        runOnUiThread(new Runnable()
        {
            public void run()
            {
                new AlertDialog.Builder(context)
                        .setCancelable(false)
                        .setTitle("No connection")
                        .setMessage("Error connecting to the internet, please turn on mobile data or Wi-Fi and try again")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                CallAPI callAPI = new CallAPI(context);
                                callAPI.delegate = SelectLockActivity.this;
                                callAPI.execute();
                            }
                        })


                        // A null listener allows the button to dismiss the dialog and take no further action.
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });

    }

    @Override
    public void tokenApiFinish(String output){
        try {
            JSONObject tokenResponse = new JSONObject(output);
            JSONObject result = tokenResponse.getJSONObject("result");
            String userId = result.getString("userId");
            String token = result.getString("token");
            CallAPI callAPI = new CallAPI(context,userId,token);
            callAPI.delegate = this;
            callAPI.execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void tokenApiFailed(String output) {
        Log.d("FAILED",output);
    }


    @Override
    public void processFinish(String output) {
        ListView lockList = findViewById(R.id.lVLockList);
        listOfLocks.clear();
        listOfLockProperties.clear();
        try {
            Gson gson = new Gson();
            JSONObject object = new JSONObject(output);
            JSONArray resultArray = object.getJSONArray("result");
            for (int i=0;i<resultArray.length();i++)
            {
                JSONObject lock = (JSONObject)resultArray.get(i);
                LockProperties lockProperties = gson.fromJson(lock.toString(),LockProperties.class);
                listOfLockProperties.add(lockProperties);
                listOfLocks.add(lockProperties.getName());
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,  android.R.layout.simple_list_item_1, listOfLocks);
        lockList.setAdapter(adapter);
        lockList.setOnItemClickListener(itemClick);
    }

    private AdapterView.OnItemClickListener itemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            LockProperties lockProperty = listOfLockProperties.get(i);
            Intent intent = new Intent(SelectLockActivity.this,MainActivity.class);
            intent.putExtra("LockProperties",lockProperty);
            startActivity(intent);
        }
    };
}
