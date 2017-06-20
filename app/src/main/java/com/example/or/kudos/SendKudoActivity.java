package com.example.or.kudos;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class SendKudoActivity extends AppCompatActivity {
    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        //this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_send_kudo);

        GridView gridview = (GridView) findViewById(R.id.gridview);
        gridview.setAdapter(new ImageAdapter(this));
        final Bundle pickedUserFBID = this.getIntent().getBundleExtra("pickedUserFBID");
        final ImageView imageView = (ImageView) findViewById(R.id.sendKudoPP);


        new AsyncTask<Void,Void,Bitmap>(){
            @Override
            protected Bitmap doInBackground(Void... params) {
                URL imageURL = null;
                Bitmap bitmap = null;
                try {
                    Uri profilePicture = Uri.parse("https://graph.facebook.com/" + pickedUserFBID.getString("facebookID") + "/picture?type=large");
                    imageURL = new URL(profilePicture.toString());
                }
                catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                try {
                    bitmap  = BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return bitmap;
            }

            @Override
            protected void onPostExecute(Bitmap i_Bitmap) {
                super.onPostExecute(i_Bitmap);
                imageView.setImageBitmap(i_Bitmap);
            }
        }.execute();

        final String url = "https://kudosapi.herokuapp.com/api/compliments";
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int itemID, long id) {
                if (/*isNetworkAvailable()*/true) {
                    Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap
                    Network network = new BasicNetwork(new HurlStack());
                    queue = new RequestQueue(cache, network);
                    queue.start();
                    JSONObject requestObject = new JSONObject();

                    try {
                        Log.e("picked item", "" + itemID);


                        requestObject.put("facebookIDOfSender", Profile.getCurrentProfile().getId());
                        String FBID = pickedUserFBID.getString("facebookID");
                        requestObject.put("facebookIDOfReceiver", FBID); //pickedUserFBID.getString("facebookID"));
                        requestObject.put("itemID", itemID);
                        requestObject.put("received", false);

                        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, url, requestObject,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        Toast.makeText(getBaseContext(), "Kudo has been sent!", Toast.LENGTH_SHORT).show();
                                        Intent mainActivity = new Intent(SendKudoActivity.this, MainActivity.class);
                                        startActivity(mainActivity);
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(getBaseContext(), "Kudo was not sent!", Toast.LENGTH_SHORT).show();
                                Intent mainActivity = new Intent(SendKudoActivity.this, MainActivity.class);
                                startActivity(mainActivity);
                            }
                        });
                        queue.add(req);

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

