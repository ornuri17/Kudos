package com.example.or.kudos;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;
import android.telephony.TelephonyManager;
import android.content.Context;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.facebook.Profile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static java.security.AccessController.getContext;

/**
 * Created by Or on 11/01/2017.
 */

public class GPSTracker extends Service implements LocationListener {
    private final Context m_Context;

    // Flag for GPS status
    boolean m_IsGPSEnabled = false;
    // Flag for network status
    boolean m_IsNetworkEnabled = false;
    // Flag for getting location
    boolean m_CanGetLocation = false;
    Location m_Location;
    double m_Latitude;
    double m_Longitude;

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters
    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 30 * 1; // 30 seconds

    // Declaring a Location Manager
    protected LocationManager m_LocationManager;

    public GPSTracker(Context context) {
        m_Context = context;
        getLocation();
    }


    public Location getLocation() {

        try {
            m_LocationManager = (LocationManager) m_Context
                    .getSystemService(LOCATION_SERVICE);

            // getting GPS status
            m_IsGPSEnabled = m_LocationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            m_IsNetworkEnabled = m_LocationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!m_IsGPSEnabled && !m_IsNetworkEnabled) {
                // no network provider is enabled
            }
            else {
                m_CanGetLocation = true;
                // First get location from Network Provider
                if (m_IsNetworkEnabled) {
                    m_LocationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    Log.d("Network", "Network");
                    if (m_LocationManager != null) {
                        m_Location = m_LocationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (m_Location != null) {
                            m_Latitude = m_Location.getLatitude();
                            m_Longitude = m_Location.getLongitude();
                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (m_IsGPSEnabled && m_Location == null) {
                    m_LocationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    Log.d("GPS Enabled", "GPS Enabled");
                    if (m_LocationManager != null) {
                        m_Location = m_LocationManager
                                .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (m_Location != null) {
                            m_Latitude = m_Location.getLatitude();
                            m_Longitude = m_Location.getLongitude();
                        }
                    }
                }
            }



        } catch (SecurityException e) {
            e.printStackTrace();
        }

        return m_Location;
    }

    /**
     * Stop using GPS listener
     * Calling this function will stop using GPS in your app
     * */
    public void stopUsingGPS(){
        try
        {
            if(m_LocationManager != null){
                m_LocationManager.removeUpdates(GPSTracker.this);
            }
        }
        catch (SecurityException e){
            e.printStackTrace();
        }
    }

    /**
     * Function to get latitude
     * */
    public double getLatitude(){
        if(m_Location != null){
            m_Latitude = m_Location.getLatitude();
        }

        // return latitude
        return m_Latitude;
    }

    /**
     * Function to get longitude
     * */
    public double getLongitude(){
        if(m_Location != null){
            m_Longitude = m_Location.getLongitude();
        }

        // return longitude
        return m_Longitude;
    }

    /**
     * Function to check GPS/wifi enabled
     * @return boolean
     * */
    public boolean canGetLocation() {
        return m_CanGetLocation;
    }

    /**
     * Function to show settings alert dialog
     * On pressing Settings button will lauch Settings Options
     * */
    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(m_Context);

        // Setting Dialog Title
        alertDialog.setTitle("GPS settings");

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                m_Context.startActivity(intent);
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
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {

        //m_Location = getLocation();

        Log.e("Google", "Location Changed");

        if (location == null)
            return;

        if (/*isNetworkAvailable()*/true) {
            JSONObject requestObject = new JSONObject();

            try {
                //TelephonyManager telephonyManager;

                //telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                //String deviceId = telephonyManager.getDeviceId();
                Log.e("latitude", location.getLatitude() + "");
                Log.e("longitude", location.getLongitude() + "");


                double[] coordinates = {location.getLatitude(), location.getLongitude()};
                requestObject.put("coordinates", coordinates);
                requestObject.put("facebookID", Profile.getCurrentProfile().getId());

                JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, "https://kudosapi.herokuapp.com/api/locationStatus", requestObject,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {

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
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}

