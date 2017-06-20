package com.example.or.kudos;

import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.facebook.FacebookSdk;
import com.facebook.Profile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import de.hdodenhof.circleimageview.CircleImageView;

public class KudoResponse extends AppCompatActivity {
    private CircleImageView m_ProfilePicture;
    private Bitmap m_Bitmap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_kudo_response);
        clearNotification();


        m_ProfilePicture = (CircleImageView) findViewById(R.id.profilePicture);
        final Bundle userID = this.getIntent().getBundleExtra("IDBundle");
        new AsyncTask<Void,Void,Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                URL imageURL = null;
                try {
                    String otherUserID = userID.getString("ID");
                    Uri profilePicture = Uri.parse("https://graph.facebook.com/" + otherUserID + "/picture?type=large");//Profile.getCurrentProfile().getProfilePictureUri(400,400);
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


    }

    private void clearNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NotificationCreator.NOTIFICATION_ID);
    }

}
