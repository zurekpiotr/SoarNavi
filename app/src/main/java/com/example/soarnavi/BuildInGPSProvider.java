package com.example.soarnavi;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;


class BuildInGPSProvider {

    private final Context mContext;
    Sensor magneticFieldSensor, accelerometerSensor;
    private SensorManager mSensorManager;
    private float azimuth, pitch, roll;
    Location location;
    double elevation;

    RequestQueue reqQueue;

    List<Float> azimuthList;


    void initGPS()
    {
        LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new MyLocationListener();
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
//            mContext.requestPermissionLauncher.launch(
//                    Manifest.permission.ACCESS_COARSE_LOCATION);


            Log.e("GPSERROR", "PERMISSION");
            //return;
        }
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
    }

    void initSensors()
    {
        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        Log.e("SENSOR" , "" + mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) );
        Log.e("SENSOR" , "" + mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) );

        MySensorListener sensorListener = new MySensorListener();

        magneticFieldSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mSensorManager.registerListener(sensorListener, magneticFieldSensor,
                SensorManager.SENSOR_DELAY_NORMAL);

        accelerometerSensor =  mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(sensorListener, accelerometerSensor,
                SensorManager.SENSOR_DELAY_NORMAL);

        azimuthList = new LinkedList<>();

    }

    void requestElevation(Location loc)
    {
        Log.e("JSON" , loc.toString() );
        String latlonStr = ""+loc.getLatitude()+","+loc.getLongitude();
        String url = "https://maps.googleapis.com/maps/api/elevation/json?locations=";
        String ulrApiKey = /*39.7391536,-104.9847034*/"&key=AIzaSyCTHbpm4iu9UiC1dXdafdp5RK1vc6-NLVA";

        url = url + latlonStr + ulrApiKey;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e("JSON" , response.toString() );
                        try {
                            elevation = response.getJSONArray("results").getJSONObject(0).getDouble("elevation");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {


                    }
                });

        reqQueue.add(jsonObjectRequest);





        //https://maps.googleapis.com/maps/api/elevation/json?locations=39.7391536,-104.9847034&key=AIzaSyCTHbpm4iu9UiC1dXdafdp5RK1vc6-NLVA
    }


    BuildInGPSProvider(Context context) {
        this.mContext = context;

        Cache cache = new DiskBasedCache(context.getCacheDir(), 1024 * 1024); // 1MB cap

// Set up the network to use HttpURLConnection as the HTTP client.
        Network network = new BasicNetwork(new HurlStack());


        reqQueue = new RequestQueue(cache, network);

        reqQueue.start();

        initGPS();
        initSensors();


    }

    Location getLoc()
    {
        if( location == null)
        {
            return new Location("dummyprovider");
        }
        return location;
    }

    public float getAzimuth() {
        return (float) ((azimuth+Math.PI)*360.0f/(2.0f * Math.PI)) + 270.0f ;
    }

    public double getAltitude()
    {
        if( location == null)
        {
            return 0;
        }
        return location.getAltitude();
    }





    private class MyLocationListener implements LocationListener {



        @Override
        public void onLocationChanged(Location loc) {

            requestElevation(loc);
            location = loc;
            String longitude = "Longitude: " + loc.getLongitude();
            String TAG = "GPS";
            Log.d(TAG, longitude);
            String latitude = "Latitude: " + loc.getLatitude();
            Log.d(TAG, latitude);

        }
    }

    private class MySensorListener implements SensorListener, SensorEventListener {
        float[] mGravity;
        float[] mGeomagnetic;

        @Override
        public void onSensorChanged(SensorEvent event) {


            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
                mGravity = event.values;
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
                mGeomagnetic = event.values;
            if (mGravity != null && mGeomagnetic != null) {
                float R[] = new float[9];
                float I[] = new float[9];
                boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
                if (success) {
                    float orientationData[] = new float[3];
                    SensorManager.getOrientation(R, orientationData);
                    azimuth = orientationData[0];
                    azimuthList.add(azimuth);
                    while(azimuthList.size() > 10)
                    {
                        azimuthList.remove(0);
                    }
                    azimuth=0f;
                    for(Float f: azimuthList)
                        azimuth += f;
                    azimuth/=(float)azimuthList.size();

                    //Log.d("AZIMUTH" , "" + azimuth);
                    pitch = orientationData[1];
                    roll = orientationData[2];
                    // now how to use previous 3 values to calculate orientation
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            //Log.e("SENSOR" , sensor.toString() + " " + accuracy);
        }

        @Override
        public void onSensorChanged(int sensor, float[] values) {

        }

        @Override
        public void onAccuracyChanged(int sensor, int accuracy) {

        }
    }
}
