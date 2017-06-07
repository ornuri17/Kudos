package com.example.or.kudos;

import android.app.IntentService;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

/**
 * Created by Roy Oren on 24/05/2017.
 */

public class VolleyService extends IntentService{
    public final static String RECEIVER_OBJECT = "ReceiverObject";
    public final static String GET_NEARBY_FROM_SERVER = "QueryServer";
    public final static String POSTSERVER = "PostServer";
    public final static String ID = "id";
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public VolleyService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (action.equals(GET_NEARBY_FROM_SERVER)) {
                final String id = intent.getStringExtra(ID);
                final ResultReceiver receiver = intent.getParcelableExtra(RECEIVER_OBJECT);
                getNearbyFromServer(id, "Google.com", receiver);
                //handleActionFoo(param1, param2);
            } else if (action.equals(POSTSERVER)) {

            }
        }
    }

    public void getNearbyFromServer (String id,String url, ResultReceiver receiver){
        final Bundle bundle = new Bundle();
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        //mTxtDisplay.setText("Response: " + response.toString());
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub

                    }
                });
        bundle.putString("0", "Result");
        receiver.send(0, bundle);
    }



}
