package com.example.or.kudos;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.facebook.Profile;

import org.json.JSONArray;
import org.json.JSONObject;

public class SendKudoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_kudo);

        GridView gridview = (GridView) findViewById(R.id.gridview);
        gridview.setAdapter(new ImageAdapter(this));

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int itemID, long id) {
                if (/*isNetworkAvailable()*/true) {
                    JSONObject requestObject = new JSONObject();

                    try {
                        Log.e("picked item", itemID + "");


                        requestObject.put("senderFacebookId", Profile.getCurrentProfile());
                        requestObject.put("receiverFacebookId", null/* Gets from searchresultactivity*/);
                        requestObject.put("itemId", itemID);

                        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, "www.google.com", requestObject,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        Intent mainActivity = new Intent(SendKudoActivity.this, MainActivity.class);
                                        startActivity(mainActivity);
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                            }
                        });

                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                //Toast.makeText(SendKudoActivity.this, "" + position,
                 //       Toast.LENGTH_SHORT).show();
            }
        });

    }
    public class ImageAdapter extends BaseAdapter {
        private Context mContext;

        public ImageAdapter(Context c) {
            mContext = c;
        }

        public int getCount() {
            return mThumbIds.length;
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                // if it's not recycled, initialize some attributes
                imageView = new ImageView(mContext);
                imageView.setLayoutParams(new GridView.LayoutParams(300, 300));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(2, 2, 2, 2);
            } else {
                imageView = (ImageView) convertView;
            }

            imageView.setImageResource(mThumbIds[position]);
            return imageView;
        }

        // references to our images
        private Integer[] mThumbIds = {
                R.drawable.cap, R.drawable.scarf,
                R.drawable.shirt2, R.drawable.hoodie,
                R.drawable.jacket, R.drawable.mitten,
                R.drawable.shorts1, R.drawable.skirt1,
                R.drawable.skirt2, R.drawable.trousers,
                R.drawable.underwear1, R.drawable.socks,
                R.drawable.sneaker
        };
    }
}

