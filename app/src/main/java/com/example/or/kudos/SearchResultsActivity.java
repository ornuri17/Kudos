package com.example.or.kudos;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.azoft.carousellayoutmanager.CarouselLayoutManager;
import com.azoft.carousellayoutmanager.CarouselZoomPostLayoutListener;
import com.azoft.carousellayoutmanager.CenterScrollListener;
import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.widget.ProfilePictureView;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.R.attr.bitmap;

public class SearchResultsActivity extends AppCompatActivity {

    public static int INVALID_POSITION = -1;
    private static Uri[] m_ImageUri;
    private static int m_NumberOfResults;
    private static String[] m_FacebookIDs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_search_results);
        Bundle resultsBundle = this.getIntent().getBundleExtra("NearbyResults");
        parseResults(resultsBundle);
        final HorizontalAdaptar adapter = new HorizontalAdaptar(this);
        RecyclerView rh = (RecyclerView) findViewById(R.id.list_horizontal);
        initRecyclerView(rh, new CarouselLayoutManager(CarouselLayoutManager.HORIZONTAL, false), adapter);

    }

    private void parseResults (Bundle resultsBundle){
        m_NumberOfResults = resultsBundle.getInt("numberOfResults");
        m_ImageUri = new Uri[m_NumberOfResults];
        m_FacebookIDs = resultsBundle.getStringArray("Responses");
        String currentUserId;
        for (int i = 0; i < m_NumberOfResults; i++){
            currentUserId = m_FacebookIDs[i];
            Uri temp = Uri.parse("https://graph.facebook.com/" + currentUserId + "/picture?type=large");
            m_ImageUri[i] = temp;
        }
    }

    private void initRecyclerView(final RecyclerView recyclerView, final CarouselLayoutManager layoutManager, final HorizontalAdaptar adapter) {
        // enable zoom effect. this line can be customized
        layoutManager.setPostLayoutListener(new CarouselZoomPostLayoutListener());

        recyclerView.setLayoutManager(layoutManager);
        // we expect only fixed sized item for now
        recyclerView.setHasFixedSize(true);
        // sample adapter with random data
        recyclerView.setAdapter(adapter);
        // enable center post scrolling
        recyclerView.addOnScrollListener(new CenterScrollListener());

        layoutManager.addOnItemSelectionListener(new CarouselLayoutManager.OnCenterItemSelectionListener() {

            @Override
            public void onCenterItemChanged(final int adapterPosition) {
                if (INVALID_POSITION != adapterPosition) {
                    final int value = adapter.mPosition[adapterPosition];
                    adapter.mPosition[adapterPosition] = (value % m_NumberOfResults) + (value / m_NumberOfResults + 1) * m_NumberOfResults;
                    adapter.notifyItemChanged(adapterPosition);
                }
            }
        });
    }

    private final class HorizontalAdaptar extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @SuppressWarnings("UnsecureRandomNumberGeneration")
        private final Random mRandom = new Random();
        private final int[] mColors;
        private final int[] mPosition;
        private Context context;



        private int mItemsCount = m_NumberOfResults;
        LayoutInflater inflater;

        HorizontalAdaptar(Context context) {
            this.context=context;

            this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mColors = new int[m_NumberOfResults];
            mPosition = new int[m_NumberOfResults];

            for (int i = 0; m_NumberOfResults > i; ++i) {
                //noinspection MagicNumber
                mColors[i] = Color.argb(255, mRandom.nextInt(256), mRandom.nextInt(256), mRandom.nextInt(256));
                mPosition[i] = i;

            }

        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.item_view, null) ;
            RecyclerView.ViewHolder holder = new RowNewsViewHolder(view);
            return holder;

        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            final CircleImageView circleImageView = ((RowNewsViewHolder) holder).pp;
            circleImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle pickedUserID = new Bundle();
                    pickedUserID.putString("facebookID", m_FacebookIDs[position]);
                    Intent sendKudoActivity = new Intent(SearchResultsActivity.this, SendKudoActivity.class);
                    sendKudoActivity.putExtra("pickedUserFBID", pickedUserID);
                    startActivity(sendKudoActivity);
                    finish();
                }
            });
            new AsyncTask<Void,Void,Bitmap>(){
                @Override
                protected Bitmap doInBackground(Void... params) {
                    URL imageURL = null;
                    Bitmap bitmap = null;
                    try {
                        Uri profilePicture = m_ImageUri[position]; //Profile.getCurrentProfile().getProfilePictureUri(400,400);
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
                    /*Bundle FBparams = new Bundle();
                    FBparams.putString("fields", "id,picture.type(large)");
                    new GraphRequest(AccessToken.getCurrentAccessToken(), "me", FBparams, HttpMethod.GET,
                            new GraphRequest.Callback() {
                                @Override
                                public void onCompleted(GraphResponse response) {
                                    if (response != null) {
                                        try {
                                            JSONObject data = response.getJSONObject();
                                            if (data.has("picture")) {
                                                String profilePicUrl = data.getJSONObject("picture").getJSONObject("data").getString("url");
                                                URL profilePicture = new URL (profilePicUrl);
                                                Bitmap profilePic = BitmapFactory.decodeStream(profilePicture.openConnection().getInputStream());
                                                circleImageView.setImageBitmap(profilePic);
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }).executeAsync();
*/
                    return bitmap;
                }

                @Override
                protected void onPostExecute(Bitmap i_Bitmap) {
                    super.onPostExecute(i_Bitmap);
                    circleImageView.setImageBitmap(i_Bitmap);
                }
            }.execute();
        }

        @Override
        public int getItemCount() {
            return mItemsCount;
        }
    }

    public static class RowNewsViewHolder extends RecyclerView.ViewHolder {
        CircleImageView pp;

        public RowNewsViewHolder(View itemView) {
            super(itemView);
            pp = (CircleImageView)itemView.findViewById(R.id.circleProfilePicture);
            pp.invalidate();
        }
    }


}
