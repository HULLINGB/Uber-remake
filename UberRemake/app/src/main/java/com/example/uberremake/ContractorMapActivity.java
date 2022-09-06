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
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
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

import java.util.List;
import java.util.Map;

public class ContractorMapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener, LocationListener {

    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;

    private Button mLogout, mCancel;
    private String customerID;

    private Boolean isLogginOut;
    private Boolean isCanceling;

    public LatLng customerLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contractor_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mLogout = (Button) findViewById(R.id.logout);
        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isLogginOut = true;
                disconnectFromCustomer();
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(ContractorMapActivity.this, MainActivity.class);
                startActivity(intent);
                //finish();
            }
        });

        getAssignedCustomer();
    }

    private void getAssignedCustomer(){
        String contractorID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference assignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Contractors").child(contractorID).child("customerID");
        assignedCustomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                    customerID = dataSnapshot.getValue().toString();
                    getAssignedCustomerMeetLocation();

                }else{
                    customerID = null;
                    if(pickupMarker != null){
                        pickupMarker.remove();
                    }
                    if(assignedMeetLocation != null){
                        assignedMeetLocation.removeEventListener(assignedMeetLocationRefListener);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    Marker pickupMarker;
    private DatabaseReference assignedMeetLocation;
    private ValueEventListener assignedMeetLocationRefListener;
    private Marker mCustomerMarker;
    private void getAssignedCustomerMeetLocation(){
        DatabaseReference assignedCustomerLocationRef = FirebaseDatabase.getInstance().getReference().child("Requests").child(customerID).child("l");
        assignedCustomerLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 1;
                    if(map.get(0) != null){
                        locationLat = Double.parseDouble(map.get(0).toString());

                    }
                    if(map.get(1) != null){
                        locationLng = Double.parseDouble(map.get(0).toString());

                    }

                    LatLng contractorLatLng = new LatLng(locationLat, locationLng);

                    mCustomerMarker = mMap.addMarker(new MarkerOptions().position(contractorLatLng).title("Customer Location").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher)));

                    //Distance between the Contractor and the customer
                    //Everything until the intent

                    Location loc1 = new Location("");
                    loc1.setLatitude(customerLocation.latitude);
                    loc1.setLongitude(customerLocation.longitude);

                    Location loc2 = new Location("");
                    loc2.setLatitude(contractorLatLng.latitude);
                    loc2.setLongitude(contractorLatLng.longitude);

                    double distance = loc1.distanceTo(loc2);
                    double speed = loc2.getSpeed();

                    double minutesUntilArrival = (speed/distance)*60;


                    Toast.makeText(ContractorMapActivity.this,"Customer Found" + minutesUntilArrival +"minutes until arrival", Toast.LENGTH_LONG).show();


                    if(minutesUntilArrival < 1){


                        Toast.makeText(ContractorMapActivity.this, "You have arrived", Toast.LENGTH_LONG).show();


                    }
                    if (minutesUntilArrival < .2){


                        Intent intent = new Intent(ContractorMapActivity.this, ContractorContractActivity.class);
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

        if(getApplicationContext()!=null){
            mLastLocation = location;
            LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());

            mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

            String user_id = FirebaseAuth.getInstance().getUid();

            DatabaseReference refAvailable = FirebaseDatabase.getInstance().getReference("DriverAvailable");
            DatabaseReference refWorking = FirebaseDatabase.getInstance().getReference("DriverWorking");

            GeoFire geoFireAvailable = new GeoFire(refAvailable);
            GeoFire geoFireWorking = new GeoFire(refWorking);


            switch (customerID){
                case "":
                    geoFireWorking.removeLocation(user_id);
                    geoFireAvailable.setLocation(user_id, new GeoLocation(location.getLatitude(), location.getLongitude()));
                    break;

                default:
                    geoFireAvailable.removeLocation(user_id);
                    geoFireWorking.setLocation(user_id, new GeoLocation(location.getLatitude(), location.getLongitude()));
                    break;





            }
        }


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

    private void disconnectFromCustomer(){

        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);
        String user_id = FirebaseAuth.getInstance().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Contractor Available");

        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(user_id);
    }

    @Override
    protected void onStop(){
        super.onStop();

        if(isLogginOut){
            disconnectFromCustomer();
        }




    }
}
