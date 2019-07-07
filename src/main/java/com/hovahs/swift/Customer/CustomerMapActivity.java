package com.hovahs.swift.Customer;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hovahs.swift.HistoryActivity;
import com.hovahs.swift.Login.LauncherActivity;
import com.hovahs.swift.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.ceryle.radiorealbutton.RadioRealButtonGroup;

/**
 * Main Activity displayed to the customer
 */
public class CustomerMapActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,OnMapReadyCallback {

    int     AUTOCOMPLETE_REQUEST_CODE = 1,
            MAX_SEARCH_DISTANCE = 20;

    private GoogleMap mMap;
    Location mLastLocation;
    LocationRequest mLocationRequest;

    private FusedLocationProviderClient mFusedLocationClient;

    private Button  mRequest;

    private LatLng pickupLocation;

    private Boolean requestBol = false;

    private Marker destinationMarker;

    private SupportMapFragment mapFragment;

    private String destination, requestService;

    private LatLng destinationLatLng;

    private LinearLayout    mDriverInfo,
                            mRadioLayout;

    private ImageView mDriverProfileImage;

    private TextView    mDriverName,
                        mDriverPhone,
                        mDriverCar,
                        mRatingText;

    private RadioRealButtonGroup mRadioGroup;

    private RatingBar mRatingBar;

    DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_customer);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        drawer =  findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getUserData();

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        destinationLatLng = new LatLng(0.0,0.0);

        mDriverInfo = findViewById(R.id.driverInfo);
        mRadioLayout = findViewById(R.id.radioLayout);

        mDriverProfileImage = findViewById(R.id.driverProfileImage);

        mDriverName = findViewById(R.id.driverName);
        mDriverPhone = findViewById(R.id.driverPhone);
        mDriverCar = findViewById(R.id.driverCar);

        mRatingText = findViewById(R.id.ratingText);

        mRadioGroup = findViewById(R.id.radioRealButtonGroup);
        mRadioGroup.setPosition(0);

        mRequest = findViewById(R.id.request);

        mRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (requestBol){
                    endRide();


                }else{
                    if(mLastLocation == null){
                        Toast.makeText(CustomerMapActivity.this, "Can't get location", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if(destination == null){
                        Toast.makeText(CustomerMapActivity.this, "Please pick a destination", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int selectId = mRadioGroup.getPosition();


                   switch (selectId){
                       case 0:
                           requestService = "UberX";
                           break;
                       case 1:
                           requestService = "UberXl";
                           break;
                       case 2:
                           requestService = "UberBlack";
                           break;
                   }


                    requestBol = true;

                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRequest");

                    GeoFire geoFire = new GeoFire(ref);
                    geoFire.setLocation(userId, new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()), new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {
                        }
                    });

                    mMap.clear();
                    pickupLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    destinationMarker = mMap.addMarker(new MarkerOptions().position(destinationLatLng).title("Destination").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_pickup)));

                    mRequest.setText(R.string.getting_driver);

                    getClosestDriver();
                }
            }
        });








        ImageView mDrawerButton = findViewById(R.id.drawerButton);
        mDrawerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.openDrawer(Gravity.LEFT);
            }
        });

        LinearLayout mBringUpBottomLayout = findViewById(R.id.bringUpBottomLayout);
        mBringUpBottomLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mBottomSheetBehavior.getState()!= BottomSheetBehavior.STATE_EXPANDED)
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                else
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });



        initializeBottomLayout();
        initPlacesAutocomplete();
    }


    boolean previousRequestBol = true;
    View mBottomSheet;
    BottomSheetBehavior mBottomSheetBehavior;
    BottomSheetBehavior.BottomSheetCallback bottomSheetCallback;

    /**
     * Listener for the bottom popup. This will control
     * when it is shown and when it isn't according to the actions of the users
     * of pulling on it or just clicking on it.
     */
    private void initializeBottomLayout() {
        mBottomSheet =findViewById(R.id.bottomSheet);
        mBottomSheetBehavior = BottomSheetBehavior.from(mBottomSheet);
        mBottomSheetBehavior.setHideable(false);
        mBottomSheetBehavior.setPeekHeight(100);
        mBottomSheetBehavior.setBottomSheetCallback(bottomSheetCallback);

        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if(newState==BottomSheetBehavior.STATE_COLLAPSED && requestBol != previousRequestBol){
                    if(!requestBol){
                        mDriverInfo.setVisibility(View.GONE);
                        mRadioLayout.setVisibility(View.VISIBLE);
                        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                        previousRequestBol = requestBol;
                    }
                    else{
                        mDriverInfo.setVisibility(View.VISIBLE);
                        mRadioLayout.setVisibility(View.GONE);
                        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                        previousRequestBol = requestBol;
                    }
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
    }

    /**
     * Init Places according the updated google api and
     * listen for user inputs, when a user chooses a place change the values
     * of destination and destinationLatLng so that the user can call a driver
     */
    void initPlacesAutocomplete(){
        /**
         * Initialize Places. For simplicity, the API key is hard-coded. In a production
         * environment we recommend using a secure mechanism to manage API keys.
         */
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getResources().getString(R.string.google_maps_key));
        }


        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);


        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                destination = place.getName().toString();
                destinationLatLng = place.getLatLng();

                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                Log.i("Place_autocomplete_log", "Place: " + place.getName() + ", " + place.getId());
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i("Place_autocomplete_log", "An error occurred: " + status);
            }
        });


    }

    /**
     * Get the data of the driver that has been
     * attributed to the customer
     */
    private void getUserData() {
        DatabaseReference assignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        assignedCustomerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    NavigationView navigationView = findViewById(R.id.nav_view);
                    View header = navigationView.getHeaderView(0);
                    if (dataSnapshot.child("name").getValue() != null) {
                        TextView mUsername = header.findViewById(R.id.usernameDrawer);
                        mUsername.setText(dataSnapshot.child("name").getValue().toString());
                    }
                    if (dataSnapshot.child("profileImageUrl").getValue() != null) {
                        ImageView mProfileImage = header.findViewById(R.id.imageViewDrawer);
                        Glide.with(getApplication()).load(dataSnapshot.child("profileImageUrl").getValue().toString()).apply(RequestOptions.circleCropTransform()).into(mProfileImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private int radius = 1;
    private Boolean driverFound = false;
    private String driverFoundID;

    GeoQuery geoQuery;

    /**
     * Get Closest Rider by getting all the drivers available
     * within a radius of the customer current location.
     * radius starts with 1 km and goes up to MAX_SEARCH_DISTANCE
     * Where if no driver is found, and error is thrown saying no
     * driver is found.
     */
    private void getClosestDriver(){
        if(radius > MAX_SEARCH_DISTANCE){
            mRequest.setText(R.string.call_uber);
            Snackbar.make(findViewById(R.id.drawer_layout),R.string.no_driver_near_you, Snackbar.LENGTH_LONG).show();
            return;
        }
        DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference().child("driversAvailable");

        GeoFire geoFire = new GeoFire(driverLocation);
        geoQuery = geoFire.queryAtLocation(new GeoLocation(pickupLocation.latitude, pickupLocation.longitude), radius);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!driverFound && requestBol){
                    DatabaseReference mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(key);
                    mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                                Map<String, Object> driverMap = (Map<String, Object>) dataSnapshot.getValue();
                                if (driverFound){
                                    return;
                                }

                                if(driverMap.get("service").equals(requestService)){
                                    driverFound = true;
                                    driverFoundID = dataSnapshot.getKey();

                                    DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundID).child("customerRequest");
                                    String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                    HashMap map = new HashMap();
                                    map.put("service", requestService);
                                    map.put("customerRideId", customerId);
                                    map.put("destination", destination);
                                    map.put("destinationLat", destinationLatLng.latitude);
                                    map.put("destinationLng", destinationLatLng.longitude);
                                    driverRef.updateChildren(map);

                                    getDriverLocation();
                                    getDriverInfo();
                                    getHasRideEnded();
                                    mRequest.setText(R.string.looking_driver);
                                }
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
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
                if (!driverFound)
                {
                    radius++;
                    getClosestDriver();
                }
            }
            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }


    /**
     * Get's most updated driver location and it's always checking for movements.
     * Even though we used geofire to push the location of the driver we can use a normal
     * Listener to get it's location with no problem.
     * 0 -> Latitude
     * 1 -> Longitudde
     */
    private Marker mDriverMarker;
    private DatabaseReference driverLocationRef;
    private ValueEventListener driverLocationRefListener;
    private void getDriverLocation(){
        driverLocationRef = FirebaseDatabase.getInstance().getReference().child("driversWorking").child(driverFoundID).child("l");
        driverLocationRefListener = driverLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && requestBol){
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    if(map.get(0) != null){
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if(map.get(1) != null){
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    LatLng driverLatLng = new LatLng(locationLat,locationLng);
                    if(mDriverMarker != null){
                        mDriverMarker.remove();
                    }
                    Location loc1 = new Location("");
                    loc1.setLatitude(pickupLocation.latitude);
                    loc1.setLongitude(pickupLocation.longitude);

                    Location loc2 = new Location("");
                    loc2.setLatitude(driverLatLng.latitude);
                    loc2.setLongitude(driverLatLng.longitude);

                    float distance = loc1.distanceTo(loc2);

                    if (distance<100){
                        mRequest.setText(R.string.driver_here);
                    }else{
                        mRequest.setText(getString(R.string.driver_found) + ": " + String.valueOf(distance));
                    }



                    mDriverMarker = mMap.addMarker(new MarkerOptions().position(driverLatLng).title("your driver").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_car)));
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    /**
     * Get all the user information that we can get from the user's database.
     */
    private void getDriverInfo(){
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        
        DatabaseReference mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundID);
        mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    mDriverInfo.setVisibility(View.VISIBLE);
                    mRadioLayout.setVisibility(View.GONE);

                    if(dataSnapshot.child("name").getValue()!=null){
                        mDriverName.setText(getResources().getString(R.string.name) + ": " + dataSnapshot.child("name").getValue().toString());
                    }
                    if(dataSnapshot.child("phone").getValue()!=null){
                        mDriverPhone.setText(getResources().getString(R.string.phone) + ": " + dataSnapshot.child("phone").getValue().toString());
                    }
                    if(dataSnapshot.child("car").getValue()!=null){
                        mDriverCar.setText(getResources().getString(R.string.car) + ": " + dataSnapshot.child("car").getValue().toString());
                    }
                    if(dataSnapshot.child("profileImageUrl").getValue()!=null){
                        Glide.with(getApplication()).load(dataSnapshot.child("profileImageUrl").getValue().toString()).apply(RequestOptions.circleCropTransform()).into(mDriverProfileImage);
                    }

                    int ratingSum = 0;
                    float ratingsTotal = 0;
                    float ratingsAvg = 0;
                    for (DataSnapshot child : dataSnapshot.child("rating").getChildren()){
                        ratingSum = ratingSum + Integer.valueOf(child.getValue().toString());
                        ratingsTotal++;
                    }
                    if(ratingsTotal!= 0){
                        ratingsAvg = ratingSum/ratingsTotal;
                        mRatingText.setText(String.valueOf(ratingsAvg));
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    /**
     * Listen for the customerRequest Node to see if the driver ended it
     * in the mean time.
     */
    private DatabaseReference driveHasEndedRef;
    private ValueEventListener driveHasEndedRefListener;
    private void getHasRideEnded(){
        driveHasEndedRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundID).child("customerRequest").child("customerRideId");
        driveHasEndedRefListener = driveHasEndedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                }else{
                    endRide();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    /**
     * End Ride by removing all of the active listeners,
     * returning all of the values to the default state
     * and clearing the map from markers
     */
    private void endRide(){
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        requestBol = false;
        if(geoQuery != null)
            geoQuery.removeAllListeners();
        if (driverLocationRefListener != null)
            driverLocationRef.removeEventListener(driverLocationRefListener);
        if (driveHasEndedRefListener != null)
            driveHasEndedRef.removeEventListener(driveHasEndedRefListener);

        if (driverFoundID != null){
            DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundID).child("customerRequest");
            driverRef.removeValue();
            driverFoundID = null;

        }
        driverFound = false;
        radius = 1;
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRequest");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId, new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
            }
        });

        if(destinationMarker != null){
            destinationMarker.remove();
        }
        if (mDriverMarker != null){
            mDriverMarker.remove();
        }
        mMap.clear();
        mRequest.setText(getString(R.string.call_uber));

        mDriverInfo.setVisibility(View.GONE);
        mRadioLayout.setVisibility(View.VISIBLE);

        mDriverName.setText("");
        mDriverPhone.setText("");
        mDriverCar.setText(getString(R.string.destination));
        //mDriverProfileImage.setImageResource(R.mipmap.ic_default_user);
    }


    /**
     * Find and update user's location.
     * The update interval is set to 1000Ms and the accuracy is set to PRIORITY_HIGH_ACCURACY,
     * If you're having trouble with battery draining too fast then change these to lower values
     * @param googleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        boolean success = googleMap.setMapStyle(new MapStyleOptions(getResources()
                .getString(R.string.style_json)));


        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){

                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                mMap.setMyLocationEnabled(true);
            }else{
                checkLocationPermission();
            }
        }

    }

    LocationCallback mLocationCallback = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for(Location location : locationResult.getLocations()){
                if(getApplication()!=null){
                    mLastLocation = location;

                    LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());

                    //mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    //mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

                    if(!getDriversAroundStarted)
                        getDriversAround();

                }
            }
        }
    };


    /**
     * Get permissions for our app if they didn't previously exist.
     * requestCode -> the number assigned to the request that we've made.
     * Each request has it's own unique request code.
     */
    private void checkLocationPermission() {
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION) && ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                new android.app.AlertDialog.Builder(this)
                        .setTitle("give permission")
                        .setMessage("give permission message")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(CustomerMapActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                            }
                        })
                        .create()
                        .show();
            }
            else{
                ActivityCompat.requestPermissions(CustomerMapActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case 1:{
                if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        mMap.setMyLocationEnabled(true);
                    }
                } else{
                    Toast.makeText(getApplication(), "Please provide the permission", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }




    boolean getDriversAroundStarted = false;
    List<Marker> markerList = new ArrayList<Marker>();
    /**
     * Displays drivers around the user's current
     * location and updates them in real time.
     */
    private void getDriversAround(){
        if(mLastLocation==null){return;}
        getDriversAroundStarted = true;
        DatabaseReference driversLocation = FirebaseDatabase.getInstance().getReference().child(("driversAvailable"));


        GeoFire geoFire = new GeoFire(driversLocation);
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()), 10000);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                for(Marker markerIt : markerList){
                    if(markerIt.getTag() == null || key == null){continue;}
                    if(markerIt.getTag().equals(key))
                        return;
                }


                LatLng driverLocation = new LatLng(location.latitude, location.longitude);

                Marker mDriverMarker = mMap.addMarker(new MarkerOptions().position(driverLocation).title(key));
                mDriverMarker.setTag(key);

                markerList.add(mDriverMarker);

            }

            @Override
            public void onKeyExited(String key) {
                for(Marker markerIt : markerList) {
                    if(markerIt.getTag() == null || key == null){continue;}
                    if(markerIt.getTag().equals(key)){
                        markerIt.remove();
                        markerList.remove(markerIt);
                        return;
                    }

                }
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                for(Marker markerIt : markerList) {
                    if(markerIt.getTag() == null || key == null){continue;}
                    if(markerIt.getTag().equals(key)) {
                        markerIt.setPosition(new LatLng(location.latitude, location.longitude));
                    }
                }
            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }


    private void logOut(){
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(CustomerMapActivity.this, LauncherActivity.class);
        startActivity(intent);
        finish();
    }






    /**
     * Override the activity's onActivityResult(), check the request code, and
     * do something with the returned place data (in this example it's place name and place ID).
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if(mLastLocation == null){
                Snackbar.make(findViewById(R.id.drawer_layout),"First Activate GPS", Snackbar.LENGTH_LONG).show();
                return;
            }
            Place place = Autocomplete.getPlaceFromIntent(data);
            destination = place.getName();
            destinationLatLng = place.getLatLng();
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

            mMap.clear();
            pickupLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            destinationMarker = mMap.addMarker(new MarkerOptions().position(destinationLatLng).title("Destination").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_pickup)));

            Log.i("Place_autocomplete_log", "Place: " + place.getName() + ", " + place.getId());
            Log.i("PLACE_AUTOCOMPLETE", "Place: " + place.getName() + ", " + place.getId());
        } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
            // TODO: Handle the error.
            Status status = Autocomplete.getStatusFromIntent(data);
            Log.i("PLACE_AUTOCOMPLETE", status.getStatusMessage());
        } else if (resultCode == RESULT_CANCELED) {
            initPlacesAutocomplete();
        }
        initPlacesAutocomplete();

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.history) {
            Intent intent = new Intent(CustomerMapActivity.this, HistoryActivity.class);
            intent.putExtra("customerOrDriver", "Customers");
            startActivity(intent);
        } else if (id == R.id.settings) {
            Intent intent = new Intent(CustomerMapActivity.this, CustomerSettingsActivity.class);
            startActivity(intent);
        } else if (id == R.id.logout) {
            logOut();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
