package com.example.uberremake;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

public class CustomerMapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener, LocationListener {

    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;

    private FusedLocationProviderClient mFusedLocationClient;

    public Button mLogout, mRequest, mRequest2, mBegin, mSettings;
    public LatLng pickupLocation;

    private int radius;

    public Boolean requestActive;

    private Marker meetLocationMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_map);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mLogout = (Button) findViewById(R.id.logout);
        mRequest = (Button) findViewById(R.id.request);
        //mRequest2 = (Button) findViewById(R.id.request2);
        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(CustomerMapActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });



        mRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(requestActive){
                    geoQuery.removeAllListeners();
                    contractorLocationRef.removeEventListener(contractorLocationRefListener);

                    String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("CustomerRequest");
                    GeoFire geoFire = new GeoFire(ref);
                    geoFire.removeLocation(userID);

                    if(contractorFoundId != null){
                        DatabaseReference contractorRef = FirebaseDatabase.getInstance().getReference().child("Users").child("ContractorId").child(contractorFoundId);
                        contractorRef.setValue(true);
                        contractorFoundId=null;


                    }
                    contractorFound=false;
                    radius = 1;
                    if(meetLocationMarker != null){
                        meetLocationMarker.remove();
                        mRequest.setText("Find Contractor");
                    }
                }else{
                    String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("CustomerRequest");
                    GeoFire geoFire = new GeoFire(ref);
                    geoFire.setLocation(user_id, new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
                    pickupLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());

                    meetLocationMarker = mMap.addMarker(new MarkerOptions().position(pickupLocation).title("Meet your driver here").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_avatar)));

                    getClosestContractor();

                }



            }
        });
/**
 mRequest2.setOnClickListener(new View.OnClickListener() {
@Override
public void onClick(View v) {
//empty and ready for the user to choose a driver




}
});

 **/
        mSettings.findViewById(R.id.settings);
        mSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CustomerMapActivity.this, CustomerSettingsActivity.class);
                startActivity(intent);

            }
        });

    }

    private Boolean contractorFound = false;
    private String contractorFoundId;
    GeoQuery geoQuery;

    private void getClosestContractor(){
        DatabaseReference contractorLocation = FirebaseDatabase.getInstance().getReference().child("ContractorAvailable");

        GeoFire geoFire = new GeoFire(contractorLocation);

        geoQuery = geoFire.queryAtLocation(new GeoLocation(pickupLocation.latitude, pickupLocation.longitude), radius);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if(!contractorFound){
                    contractorFound = true;
                    contractorFoundId = key;

                    DatabaseReference contractorReference = FirebaseDatabase.getInstance().getReference().child("Users").child("Contractors").child(contractorFoundId);
                    String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    HashMap map = new HashMap();
                    map.put("CustomerId", customerId);
                    contractorReference.updateChildren(map);

                    getContractorLocation();
                    mRequest.setText("Looking for contractor");



                }

            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if(!contractorFound){

                    radius++;
                    getClosestContractor();

                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }
    private Marker mContractorMarker;
    private DatabaseReference contractorLocationRef;
    private ValueEventListener contractorLocationRefListener;
    private void getContractorLocation(){
        contractorLocationRef = FirebaseDatabase.getInstance().getReference().child("ContractorWorking").child(contractorFoundId).child("l");
        contractorLocationRefListener = contractorLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 1;
                    mRequest.setText("Contractor Found");
                    if(map.get(0) != null){
                        locationLat = Double.parseDouble(map.get(0).toString());

                    }
                    if(map.get(1) != null){
                        locationLng = Double.parseDouble(map.get(0).toString());

                    }

                    LatLng contractorLatLng = new LatLng(locationLat, locationLng);
                    if(mContractorMarker != null){
                        mContractorMarker.remove();
                    }
                    /**
                     mContractorMarker = mMap.addMarker(new MarkerOptions().position(contractorLatLng).title("Your contractor is on the way"));
                     **/
                    Location loc1 = new Location("");
                    loc1.setLatitude(pickupLocation.latitude);
                    loc1.setLongitude(pickupLocation.longitude);

                    Location loc2 = new Location("");
                    loc2.setLatitude(contractorLatLng.latitude);
                    loc2.setLongitude(contractorLatLng.longitude);

                    //Between the Contractor and the customer
                    //Everything until the toast
                    double distance = loc1.distanceTo(loc2);
                    double speed = loc2.getSpeed();

                    double minutesUntilArrival = (speed/distance)*60;


                    mRequest.setText("Contractor Found" + minutesUntilArrival +"minutes until arrival");



                    if(minutesUntilArrival < 1){


                        Toast.makeText(CustomerMapActivity.this, "Contractor has arrived", Toast.LENGTH_LONG).show();

                    }
                    if(minutesUntilArrival < .1){


                        Intent intent = new Intent(CustomerMapActivity.this, CustomerContractActivity.class);
                        startActivity(intent);


                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            return;
        }
        buildGoogleApiClient();
        //mMap.setMyLocationEnabled(true);
    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }


    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));



    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){

            return;
        }
        //LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);


    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onStop(){
        super.onStop();
        FirebaseAuth.getInstance().signOut();


    }
}