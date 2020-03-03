package com.example.treeplanter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private static final int LOCATION_REQUEST_CODE = 101;
    private GoogleMap mMap;
    private FirebaseAuth mAuth;
    private static final LatLng masseyWoods = new LatLng(53.2539026, -6.3232363);
    private static final LatLng SYDNEY = new LatLng(-33.87365, 151.20689);
    private static final LatLng BRISBANE = new LatLng(-27.47093, 153.0235);


    private Marker mMassey;
    private Marker mSydney;
    private Marker mBrisbane;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.treemenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId()== R.id.logout){
            mAuth.signOut();
            finish();
            Intent intent = new Intent(this,MainActivity.class);
            startActivity(intent);

        } else if (item.getItemId()== R.id.purchase){
            Intent intent = new Intent(this,PurchaseActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
    // sign out if back button pressed
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mAuth.signOut();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (mMap != null)   {
            //Massey Woods Marker
            LatLng masseyWoods = new LatLng(53.2539026, -6.3232363);
            mMap.addMarker(new MarkerOptions()
                    .position(masseyWoods)
                    .title("Massey Woods"));
            // Add some markers to the map, and add a data object to each marker.
            mMassey = mMap.addMarker(new MarkerOptions()
                    .position(masseyWoods)
                    .title("Perth"));
            mMassey.setTag(0);

            mSydney = mMap.addMarker(new MarkerOptions()
                    .position(SYDNEY)
                    .title("Sydney"));
            mSydney.setTag(0);

            mBrisbane = mMap.addMarker(new MarkerOptions()
                    .position(BRISBANE)
                    .title("Brisbane"));
            mBrisbane.setTag(0);

            // Set a listener for marker click.
            mMap.setOnMarkerClickListener(this);

            // Center Map on Dublin
            LatLng dublin = new LatLng(53.3498, -6.2603);
            CameraUpdate point = CameraUpdateFactory.newLatLngZoom(dublin, 10f);
            mMap.moveCamera(point);
            mMap.animateCamera(point);
            // User's Location
            int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

            if (permission == PackageManager.PERMISSION_GRANTED)    {
                // enable my location function if permsion is accepted
                mMap.setMyLocationEnabled(true);
            }   else{
                // Move to default location
                mMap.moveCamera(point);
                mMap.animateCamera(point);
                // Request permission
                requestPermission(Manifest.permission.ACCESS_FINE_LOCATION,LOCATION_REQUEST_CODE);
            }
        }

    }
    //Request users permission to user their Location.
    //Reference from book.
    protected void requestPermission(String permissionType, int requestCode){
        ActivityCompat.requestPermissions(this, new String[]{permissionType},requestCode);

    }

    //If permisison accepted map is refreshed. If not a pop up message is displayed
    // Reference from book.
    public void requestPermissionsResult(int requestCode,
                                            String permissions[], int[] grantResults){
        switch (requestCode){
            case LOCATION_REQUEST_CODE: {
                // if permission is denied
                if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED)   {
                    Toast.makeText(this,
                            "Default location used", Toast.LENGTH_SHORT).show();

                } else {
                    SupportMapFragment mapFragment =
                            (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                    mapFragment.getMapAsync(this);
                    mMap.animateCamera( CameraUpdateFactory.zoomTo( 9f ) );
                }
            }
        }

    }

    /** Called when the user clicks a marker. */
    @Override
    public boolean onMarkerClick(final Marker marker) {

        // Retrieve the data from the marker.
        String treeLocation = (String) marker.getTitle();
        Toast.makeText(this, treeLocation, Toast.LENGTH_SHORT).show();
        // Show button to take user to next page

        // Check if a click count was set, then display the click count.
        /*
        if (clickCount != null) {
            clickCount = clickCount + 1;
            marker.setTag(clickCount);
            Toast.makeText(this,
                    marker.getTitle() +
                            " has been clicked " + clickCount + " times.",
                    Toast.LENGTH_SHORT).show();
        }
         */

        // Return false to indicate that we have not consumed the event and that we wish
        // for the default behavior to occur (which is for the camera to move such that the
        // marker is centered and for the marker's info window to open, if it has one).
        return false;
    }

}
