package com.example.or.kudos;

/**
* Created by Roy Oren on 14/06/2017.
*/
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NotificationCreator extends Service {

    private User m_User;
    private RequestQueue queue;
    public static final int REQUEST_CODE_START_PLAYING = 5;
    public static final int NOTIFICATION_ID = 15;

    public NotificationCreator() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        Intent i = new Intent(this, MainActivity.class);
//        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(i);



        FacebookSdk.sdkInitialize(getApplicationContext());
        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap
        Network network = new BasicNetwork(new HurlStack());
        queue = new RequestQueue(cache, network);
        queue.start();
        m_User = PrefUtils.getCurrentUser(NotificationCreator.this);

        String url = "https://kudosapi.herokuapp.com/api/compliments/getComplimentsByFacebookID";
        JSONObject requestObject = new JSONObject();
        try {
            requestObject.put("facebookID", Profile.getCurrentProfile().getId());

            JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, url, requestObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            JSONArray complimentResponses = null;
                            try {
                                complimentResponses = response.getJSONArray("results");
                                if (complimentResponses.length() != 0){
                                    String ID = "";
                                    try {
                                        String updateURL = "https://kudosapi.herokuapp.com/api/compliments/" + response.getString("_id");
                                        JSONObject updateJsonObj = new JSONObject();
                                        updateJsonObj.put("received", true);
                                        ID = response.getString("_id");
                                        JsonObjectRequest req = new JsonObjectRequest(Request.Method.PUT, updateURL, updateJsonObj,
                                                new Response.Listener<JSONObject>() {
                                                    @Override
                                                    public void onResponse(JSONObject response) {

                                                    }
                                                }, new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {

                                            }
                                        });
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    Intent newKudoActivity = new Intent(NotificationCreator.this, NewKudo.class);
                                    showNotification("You Got a KUDO!", "Click here to respond", newKudoActivity, ID);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    int b = 7;
                }
            });
            queue.add(req);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        url = "https://kudosapi.herokuapp.com/api/complimentsResponses/getComplimentsResponsesByFacebookID";
        try {
            requestObject.put("facebookID", Profile.getCurrentProfile().getId());

            JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, url, requestObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                JSONArray complimentResponses = response.getJSONArray("results");
                                if (complimentResponses.length() != 0){
                                    String ID = "";
                                    try {
                                        String updateURL = "https://kudosapi.herokuapp.com/api/complimentsResponses/" + complimentResponses.getJSONObject(0).getString("_id");
                                        JSONObject updateJsonObj = new JSONObject();
                                        updateJsonObj.put("received", true);
                                        ID = complimentResponses.getJSONObject(0).getString("_id");
                                        JsonObjectRequest req = new JsonObjectRequest(Request.Method.PUT, updateURL, updateJsonObj,
                                                new Response.Listener<JSONObject>() {
                                                    @Override
                                                    public void onResponse(JSONObject response) {

                                                    }
                                                }, new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {

                                            }
                                        });
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    Intent kudoResponseActivity = new Intent(NotificationCreator.this, KudoResponse.class);
                                    showNotification("Incoming", "Click here to view desired item", kudoResponseActivity, ID);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                }
            });
            queue.add(req);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // TODO: check if a new player has joined

        return super.onStartCommand(intent, flags, startId);
    }

    private void showNotification(String title, String text, Intent intent, String ID) {
        //final Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle bundle = new Bundle();
        bundle.putString("ID", ID);
        intent.putExtra("IDBundle", bundle);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, REQUEST_CODE_START_PLAYING, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.logobackground)          //:Todo ICON!!!
                        .setContentTitle(title)
                        .setContentText(text)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
