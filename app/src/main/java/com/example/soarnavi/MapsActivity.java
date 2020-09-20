package com.example.soarnavi;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import androidx.core.view.GestureDetectorCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    BuildInGPSProvider GPS;

    GestureDetectorCompat mDetector;

    Float zoom = 12.0f ;

    Button altitudeIndicator, menuButton;

    private void refreshMap()
    {
        Location loc = GPS.getLoc();

        CameraPosition currentPlace = new CameraPosition.Builder()
                .target(new LatLng(loc.getLatitude() ,loc.getLongitude() ))
                .bearing(GPS.getAzimuth()).zoom(zoom).build();

        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(currentPlace));
    }

    void refreshPanels()
    {
        altitudeIndicator.setText("" + GPS.getAltitude());
    }

    private void refreshView()
    {
        refreshMap();
        refreshPanels();


    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_view);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mapFragment.getView().setClickable(false);

        altitudeIndicator = (Button) findViewById(R.id.altitudeIndicator);
        altitudeIndicator.setText("Sea level:\n0m");

        menuButton = (Button) findViewById(R.id.menuButton);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("MENU CLICK","------");
                DataParser dataParser = new DataParser( getBaseContext() );
                //getResources().openRawResource(R.raw.pobiednikaera);
            }
        });
    }

    public void onMenuButtonClick(View v)
    {
        Log.d("MENU CLICK","------");
        DataParser dataParser = new DataParser( getBaseContext() );
        //getResources().openRawResource(R.raw.pobiednikaera);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setAllGesturesEnabled(false);

        GPS = new BuildInGPSProvider(this.getBaseContext());

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        refreshView();
                    }
                });


            }
        }, 0, 1000);

    }

    protected static final String TAG = "SlidableActivity";
    private static final int ACTION_TYPE_DEFAULT = 0;
    private static final int ACTION_TYPE_UP = 1;
    private static final int ACTION_TYPE_RIGHT = 2;
    private static final int ACTION_TYPE_DOWN = 3;
    private static final int ACTION_TYPE_LEFT = 4;
    private static final int SLIDE_RANGE = 200;
    private float mTouchStartPointX;
    private float mTouchStartPointY;
    private int mActionType = ACTION_TYPE_DEFAULT;

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        int x = (int) event.getRawX();
        int y = (int) event.getRawY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouchStartPointX = event.getRawX();
                mTouchStartPointY = event.getRawY();
                mActionType = ACTION_TYPE_DEFAULT;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mTouchStartPointX - x > SLIDE_RANGE) {
                    mActionType = ACTION_TYPE_LEFT;
                } else if (x - mTouchStartPointX > SLIDE_RANGE) {
                    mActionType = ACTION_TYPE_RIGHT;
                } else if (mTouchStartPointY - y > SLIDE_RANGE) {
                    mActionType = ACTION_TYPE_UP;
                } else if (y - mTouchStartPointY > SLIDE_RANGE) {
                    mActionType = ACTION_TYPE_DOWN;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mActionType == ACTION_TYPE_UP) {
                    slideUp();
                } else if (mActionType == ACTION_TYPE_RIGHT) {
                    slideToRight();
                } else if (mActionType == ACTION_TYPE_DOWN) {
                    slideDown();
                } else if (mActionType == ACTION_TYPE_LEFT) {
                    slideToLeft();
                }
                break;
            default:
                break;
        }
        return false;
    }

    private void slideToLeft() {
    }

    private void slideDown() {
        Log.d("EVENT","SLIDE DOWN");
        zoom*=1.05f;
        refreshView();
    }

    private void slideToRight() {
    }

    private void slideUp() {
        Log.d("EVENT","SLIDE UP");
        zoom/=1.05f;
        refreshView();
    }



}