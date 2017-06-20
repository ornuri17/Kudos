package com.example.or.kudos;

import android.app.AlertDialog;
import android.app.IntentService;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.style.BulletSpan;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
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
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.cunoraz.gifview.library.GifView;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.google.gson.JsonArray;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private RequestQueue queue;
    private GPSTracker m_GPSTracker;
    private Location m_Location;
    private Handler m_LocationHandler = new Handler();
    private static final int MY_PERMISSION_ACCESS_COURSE_LOCATION = 1;
    private Runnable m_LocationRunnable = new Runnable() {
        @Override
        public void run() {
            m_Location = m_GPSTracker.getLocation();
            m_LocationHandler.postDelayed(m_LocationRunnable, 3000);
        }
    };
    private TextView m_LogoutButton;
    private User m_User;
    private CircleImageView m_ProfilePicture;
    private Bitmap m_Bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        final ImageView imageView = (ImageView) findViewById(R.id.radarPhoto);
        imageView.setVisibility(View.VISIBLE);
        imageView.setImageResource(R.drawable.radar);


        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION)) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(getBaseContext());

                // Setting Dialog Title
                alertDialog.setTitle("GPS settings");

                // Setting Dialog Message
                alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

                // On pressing Settings button
                alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        getBaseContext().startActivity(intent);
                    }
                });

                // on pressing cancel button
                alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                // Showing Alert Message
                alertDialog.show();


            } else {

                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSION_ACCESS_COURSE_LOCATION);

            }
        }

        m_User = PrefUtils.getCurrentUser(MainActivity.this);
        m_ProfilePicture = (CircleImageView) findViewById(R.id.profilePicture);
        // fetching facebook's profile picture
        new AsyncTask<Void,Void,Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                URL imageURL = null;
                try {
                    Uri profilePicture = Profile.getCurrentProfile().getProfilePictureUri(400,400);
                    //imageURL = new URL("https://graph.facebook.com/" + Profile.getCurrentProfile().getId() + "/picture?type=large");
                    imageURL = new URL(profilePicture.toString());
                }
                catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                try {
                    m_Bitmap  = BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());

                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                m_ProfilePicture.setImageBitmap(m_Bitmap);
            }
        }.execute();


        m_LogoutButton = (TextView) findViewById(R.id.logoutButton);
        m_LogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PrefUtils.clearCurrentUser(MainActivity.this);


                // We can logout from facebook by calling following method
                LoginManager.getInstance().logOut();


                Intent loginActivity= new Intent(MainActivity.this,LoginActivity.class);
                startActivity(loginActivity);
                finish();
            }
        });

        m_GPSTracker = new GPSTracker(this);
        updateLocation();
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap
                Network network = new BasicNetwork(new HurlStack());
                queue = new RequestQueue(cache, network);
                queue.start();


                String url = "https://kudosapi.herokuapp.com/api/locationStatus/getUsersAroundMe";
                String facebookID = Profile.getCurrentProfile().getId(); //"Put My Facebook ID";  //  url?param=value&param2=value2
                double[] coordinates = new double[2];
                coordinates[0] = m_GPSTracker.getLongitude();
                coordinates[1] = m_GPSTracker.getLatitude();

                JSONArray coordinatesJson = new JSONArray();
                JSONObject getUsersAroundMe = new JSONObject();
                try {
                    coordinatesJson.put(0, coordinates[0]);
                    coordinatesJson.put(1, coordinates[1]);
                    getUsersAroundMe.put("facebookID", facebookID);
                    getUsersAroundMe.put("coordinates", coordinatesJson);
                    Log.d("KudosJsonArray", getUsersAroundMe.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                JsonObjectRequest req = new JsonObjectRequest (Request.Method.POST, url, getUsersAroundMe, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Bundle parsedResponse = new Bundle();
                        try {
                            JSONArray responseArray = response.getJSONArray("results");
                            //JSONArray responseArray = response.toJSONArray(names);
                            int numResults = responseArray.length();
                            String[] FBIDs = new String[numResults];
                            if (numResults == 0) {
                                Toast.makeText(getBaseContext(),
                        "Can't find users around you!",
                        Toast.LENGTH_SHORT).show();
                            } else {
                                for (int i = 0; i < responseArray.length(); i++) {
                                    FBIDs[i] = responseArray.getJSONObject(i).getString("_id");
                                }

                                parsedResponse.putStringArray("Responses", FBIDs);
                                parsedResponse.putInt("numberOfResults", numResults);
                                Intent searchResultsActivity = new Intent(MainActivity.this, SearchResultsActivity.class);
                                searchResultsActivity.putExtra("NearbyResults", parsedResponse);
                                startActivity(searchResultsActivity);
                            }
                        } catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("KUDOSERRORMASSAGE", error.toString());
                        // TODO Auto-generated method stub

                    }
                });
                queue.add(req);
            }
        });


    }
    private void updateLocation(){
        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap
        Network network = new BasicNetwork(new HurlStack());
        queue = new RequestQueue(cache, network);
        queue.start();
        JSONObject requestObject = new JSONObject();
        JSONArray coordinatesJson = new JSONArray();
        JSONObject location = new JSONObject();
        double[] coordinates = {m_GPSTracker.getLatitude(), m_GPSTracker.getLongitude()};
        try {
            coordinatesJson.put(0, coordinates[0]);
            coordinatesJson.put(1, coordinates[1]);
            location.put("type", "Point");
            location.put("coordinates", coordinatesJson);
            requestObject.put("facebookID", Profile.getCurrentProfile().getId());
            requestObject.put("loc", location);


        } catch (JSONException e) {
            e.printStackTrace();
        }
        String bla = requestObject.toString();
        int g = 6;
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, "https://kudosapi.herokuapp.com/api/locationStatus", requestObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        int a = 4;
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                int b = 2;
            }
        });
        queue.add(req);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case MY_PERMISSION_ACCESS_COURSE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {



                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }
}
