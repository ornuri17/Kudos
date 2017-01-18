package com.example.or.kudos;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.cunoraz.gifview.library.GifView;
import com.facebook.Profile;
import com.facebook.login.LoginManager;


import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

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
                Toast.makeText(getBaseContext(),
                        "Location: " + m_GPSTracker.getLongitude() + "-" + m_GPSTracker.getLatitude(),
                        Toast.LENGTH_SHORT).show();
                Intent searchResultsActivity= new Intent(MainActivity.this,SearchResultsActivity.class);
                startActivity(searchResultsActivity);
                finish();
            }
        });
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
