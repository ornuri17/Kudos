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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.cunoraz.gifview.library.GifView;
import com.facebook.Profile;
import com.facebook.login.LoginManager;


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
        setContentView(R.layout.activity_main);
        final GifView gifView = (GifView)findViewById(R.id.gif1);
        gifView.setVisibility(View.VISIBLE);
        gifView.setGifResource(R.drawable.radar);


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
        gifView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = "https://Google.com";
                String facebookID = "Put My Facebook ID";  //  url?param=value&param2=value2
                //Handler handler = new Handler();                //WHAT IS A HANDLER
                //ResultReceiver receiver = new ResultReceiver(handler);
                //Intent searchResultsActivity= new Intent(MainActivity.this,SearchResultsActivity.class);
                //searchResultsActivity.putExtra(VolleyService.RECEIVER_OBJECT, receiver);
                //startActivity(searchResultsActivity);
                Toast.makeText(getBaseContext(),
                        "Location: " + m_GPSTracker.getLongitude() + "-" + m_GPSTracker.getLatitude(),
                        Toast.LENGTH_SHORT).show();
                JsonObjectRequest req = new JsonObjectRequest (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Bundle parsedResponse = new Bundle();
                        int numResults = response.length();
                        //parsedResponse.putString("Responses", response.toString());
                        parsedResponse.putInt("numberOfResults", numResults);
                        Intent searchResultsActivity= new Intent(MainActivity.this,SearchResultsActivity.class);
                        searchResultsActivity.putExtra("NearbyResults", parsedResponse);
                        startActivity(searchResultsActivity);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub

                    }
                });
                //queue.add(req);
                //finish();
                Bundle parsedResponse = new Bundle();
                int numResults = 2;
                String[] res = new String[2];
                res[0] = "1052793741";
                res[1] = "1040083258";
                parsedResponse.putStringArray("responses", res);
                //parsedResponse.putString("Responses", "1052793741");
                parsedResponse.putInt("numberOfResults", numResults);
                Intent searchResultsActivity= new Intent(MainActivity.this,SearchResultsActivity.class);
                searchResultsActivity.putExtra("NearbyResults", parsedResponse);
                startActivity(searchResultsActivity);
            }
        });


    }
    /*
    private class MyResultReceiver extends ResultReceiver {
        MyResultReceiver(Handler handler) {
            super(handler);
        }
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            //final String result = (String)resultData.get(MyIntentService.EXTRA_PARAM_RESULT_STRING);
            //Log.i("MainActivity", "Result code from receiver " + resultCode);
            //Log.i("MainActivity", "Result that came back - " + result);
            //mTextResult.setText(result);
        }
    }
*/
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
