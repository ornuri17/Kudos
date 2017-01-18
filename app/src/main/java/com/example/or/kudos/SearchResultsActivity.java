package com.example.or.kudos;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.azoft.carousellayoutmanager.CarouselLayoutManager;
import com.azoft.carousellayoutmanager.CarouselZoomPostLayoutListener;
import com.azoft.carousellayoutmanager.CenterScrollListener;
import com.facebook.FacebookSdk;
import com.facebook.Profile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchResultsActivity extends AppCompatActivity {

    public static int INVALID_POSITION = -1;
    private static Uri[] m_ImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_search_results);
        m_ImageUri = new Uri[]
                {
                    Profile.getCurrentProfile().getProfilePictureUri(200,200),
                    Profile.getCurrentProfile().getProfilePictureUri(200,200),
                    Profile.getCurrentProfile().getProfilePictureUri(200,200),
                    Profile.getCurrentProfile().getProfilePictureUri(200,200),
                    Profile.getCurrentProfile().getProfilePictureUri(200,200),
                    Profile.getCurrentProfile().getProfilePictureUri(200,200),
                    Profile.getCurrentProfile().getProfilePictureUri(200,200),
                    Profile.getCurrentProfile().getProfilePictureUri(200,200),
                    Profile.getCurrentProfile().getProfilePictureUri(200,200),
                    Profile.getCurrentProfile().getProfilePictureUri(200,200)
        };
        final HorizontalAdaptar adapter = new HorizontalAdaptar(this);
        RecyclerView rh = (RecyclerView) findViewById(R.id.list_horizontal);
        initRecyclerView(rh, new CarouselLayoutManager(CarouselLayoutManager.HORIZONTAL, false), adapter);
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
                    adapter.mPosition[adapterPosition] = (value % 10) + (value / 10 + 1) * 10;
                    adapter.notifyItemChanged(adapterPosition);
                }
            }
        });
    }

    private static final class HorizontalAdaptar extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @SuppressWarnings("UnsecureRandomNumberGeneration")
        private final Random mRandom = new Random();
        private final int[] mColors;
        private final int[] mPosition;
        private Context context;



        private int mItemsCount = 10;
        LayoutInflater inflater;

        HorizontalAdaptar(Context context) {
            this.context=context;

            this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mColors = new int[10];
            mPosition = new int[10];

            for (int i = 0; 10 > i; ++i) {
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
            new AsyncTask<Void,Void,Bitmap>(){
                @Override
                protected Bitmap doInBackground(Void... params) {
                    URL imageURL = null;
                    Bitmap bitmap = null;
                    try {
                        Uri profilePicture = Profile.getCurrentProfile().getProfilePictureUri(400,400);
                        imageURL = new URL(profilePicture.toString());
                    }
                    catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                    try {
                        bitmap  = BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());

                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
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
