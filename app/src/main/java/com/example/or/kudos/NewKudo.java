package com.example.or.kudos;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
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

import org.json.JSONObject;

public class NewKudo extends AppCompatActivity {

    private RequestQueue queue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_new_kudo);
        clearNotification();

        final String receiverID = this.getIntent().getBundleExtra("IDBundle").getString("ID");
        final String senderID = Profile.getCurrentProfile().getId();
        findViewById(R.id.SendBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://kudosapi.herokuapp.com/api/complimentsResponses";

                Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap
                Network network = new BasicNetwork(new HurlStack());
                queue = new RequestQueue(cache, network);
                queue.start();
                JSONObject requestObject = new JSONObject();

                try {

                    requestObject.put("facebookIDOfSender", senderID);
                    requestObject.put("facebookIDOfReceiver", receiverID); //pickedUserFBID.getString("facebookID"));
                    EditText brand = (EditText)findViewById(R.id.BrandTB);
                    requestObject.put("brand", brand.getText().toString());
                    Switch recentlyPurchased = (Switch)findViewById(R.id.RPSwitch);
                    requestObject.put("recentlyPurchased", recentlyPurchased.isChecked());
                    Switch abroad = (Switch)findViewById(R.id.AbroadSwitch);
                    requestObject.put("gotItAbroad", abroad.isChecked());
                    EditText price = (EditText)findViewById(R.id.PriceTB);
                    requestObject.put("price", price.getText().toString());
                    requestObject.put("received", false);

                    JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, url, requestObject,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    Toast.makeText(getBaseContext(), "Response has been sent!", Toast.LENGTH_SHORT).show();
                                    Intent mainActivity = new Intent(NewKudo.this, MainActivity.class);
                                    startActivity(mainActivity);
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(getBaseContext(), "Response was not sent!", Toast.LENGTH_SHORT).show();
                            Intent mainActivity = new Intent(NewKudo.this, MainActivity.class);
                            startActivity(mainActivity);
                        }
                    });
                    queue.add(req);

                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });

    }
    private void clearNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NotificationCreator.NOTIFICATION_ID);
    }
}
