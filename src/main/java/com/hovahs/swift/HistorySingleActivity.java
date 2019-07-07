package com.hovahs.swift;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HistorySingleActivity extends AppCompatActivity implements OnMapReadyCallback, DirectionCallback {
    private String rideId, currentUserId, customerId, driverId, userDriverOrCustomer;

    private TextView rideLocation
                    ,rideDistance
                    ,rideDate
                    ,userName
                    ,userPhone;

    private ImageView userImage;

    private RatingBar mRatingBar;

    private Button mPay;

    private DatabaseReference historyRideInfoDb;

    private LatLng destinationLatLng, pickupLatLng;
    private String distance;
    private Double ridePrice;
    private Boolean customerPaid = false;

    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_single);

        Intent intent = new Intent(this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        startService(intent);

        polylines = new ArrayList<>();

        rideId = getIntent().getExtras().getString("rideId");

        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);


        rideLocation = findViewById(R.id.rideLocation);
        rideDistance = findViewById(R.id.rideDistance);
        rideDate = findViewById(R.id.rideDate);
        userName = findViewById(R.id.userName);
        userPhone = findViewById(R.id.userPhone);

        userImage = findViewById(R.id.userImage);

        mRatingBar = findViewById(R.id.ratingBar);

        mPay = findViewById(R.id.pay);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        historyRideInfoDb = FirebaseDatabase.getInstance().getReference().child("history").child(rideId);
        getRideInformation();

    }

    private void getRideInformation() {
        historyRideInfoDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for (DataSnapshot child:dataSnapshot.getChildren()){
                        if (child.getKey().equals("customer")){
                            customerId = child.getValue().toString();
                            if(!customerId.equals(currentUserId)){
                                userDriverOrCustomer = "Drivers";
                                getUserInformation("Customers", customerId);
                            }
                        }
                        if (child.getKey().equals("driver")){
                            driverId = child.getValue().toString();
                            if(!driverId.equals(currentUserId)){
                                userDriverOrCustomer = "Customers";
                                getUserInformation("Drivers", driverId);
                                displayCustomerRelatedObjects();
                            }
                        }
                        if (child.getKey().equals("timestamp")){
                            rideDate.setText(getDate(Long.valueOf(child.getValue().toString())));
                        }
                        if (child.getKey().equals("rating")){
                            mRatingBar.setRating(Integer.valueOf(child.getValue().toString()));

                        }
                        if (child.getKey().equals("customerPaid")){
                            customerPaid =true;
                        }
                        if (child.getKey().equals("distance")){
                            distance = child.getValue().toString();
                            rideDistance.setText(distance.substring(0, Math.min(distance.length(), 5)) + " km");
                            ridePrice = Double.valueOf(distance) * 0.5;

                        }
                        if (child.getKey().equals("destination")){
                            rideLocation.setText(child.getValue().toString());
                        }
                        if (child.getKey().equals("location")){
                            pickupLatLng = new LatLng(Double.valueOf(child.child("from").child("lat").getValue().toString()), Double.valueOf(child.child("from").child("lng").getValue().toString()));
                            destinationLatLng = new LatLng(Double.valueOf(child.child("to").child("lat").getValue().toString()), Double.valueOf(child.child("to").child("lng").getValue().toString()));
                            if(destinationLatLng != new LatLng(0,0)){
                                getRouteToMarker();
                            }
                        }
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void displayCustomerRelatedObjects() {
        mRatingBar.setVisibility(View.VISIBLE);

        mPay.setVisibility(View.VISIBLE);


        mRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                historyRideInfoDb.child("rating").setValue(rating);
                DatabaseReference mDriverRatingDb = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverId).child("rating");
                mDriverRatingDb.child(rideId).setValue(rating);
            }
        });
        if(customerPaid){
            mPay.setEnabled(false);
        }else{
            mPay.setEnabled(true);
        }
        mPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                payPalPayment();
            }
        });
    }

    private int PAYPAL_REQUEST_CODE = 1;
    private static PayPalConfiguration config = new PayPalConfiguration()
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
            .clientId(PayPalConfig.PAYPAL_CLIENT_ID);

    private void payPalPayment() {
        PayPalPayment payment = new PayPalPayment(new BigDecimal(ridePrice), "USD", "Uber Ride",
                PayPalPayment.PAYMENT_INTENT_SALE);

        Intent intent = new Intent(this, PaymentActivity.class);

        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payment);

        startActivityForResult(intent, PAYPAL_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PAYPAL_REQUEST_CODE){
            if(resultCode == Activity.RESULT_OK){
                PaymentConfirmation confirm = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if(confirm != null){
                    try{
                        JSONObject jsonObj = new JSONObject(confirm.toJSONObject().toString());

                        String paymentResponse = jsonObj.getJSONObject("response").getString("state");

                        if(paymentResponse.equals("approved")){
                            Toast.makeText(getApplicationContext(), "Payment successful", Toast.LENGTH_LONG).show();
                            historyRideInfoDb.child("customerPaid").setValue(true);
                            mPay.setEnabled(false);
                            mPay.setVisibility(View.GONE);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }else{
                Toast.makeText(getApplicationContext(), "Payment unsuccessful", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(this, PayPalService.class));
        super.onDestroy();
    }







    private void getUserInformation(String otherUserDriverOrCustomer, String otherUserId) {
        DatabaseReference mOtherUserDB = FirebaseDatabase.getInstance().getReference().child("Users").child(otherUserDriverOrCustomer).child(otherUserId);
        mOtherUserDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("name") != null){
                        userName.setText(map.get("name").toString());
                    }
                    if(map.get("phone") != null){
                        userPhone.setText(map.get("phone").toString());
                    }
                    if(map.get("profileImageUrl") != null){
                        Glide.with(getApplication()).load(map.get("profileImageUrl").toString()).apply(RequestOptions.circleCropTransform()).into(userImage);
                    }
                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private String getDate(Long time) {
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(time*1000);
        String date = DateFormat.format("MM-dd-yyyy hh:mm", cal).toString();
        return date;
    }
    private void getRouteToMarker() {
        String serverKey = getResources().getString(R.string.google_maps_key);
        if (pickupLatLng != null && destinationLatLng != null){
            GoogleDirection.withServerKey(serverKey)
                    .from(destinationLatLng)
                    .to(pickupLatLng)
                    .transportMode(TransportMode.DRIVING)
                    .execute(this);

            /*
            mMap.addMarker(new MarkerOptions().position(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())));
            mMap.addMarker(new MarkerOptions().position(pickupLatLng));
        */
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap=googleMap;
        boolean success = googleMap.setMapStyle(new MapStyleOptions(getResources()
                .getString(R.string.style_json)));

    }


    private List<Polyline> polylines;
    private void setCameraWithCoordinationBounds(Route route) {
        LatLng southwest = route.getBound().getSouthwestCoordination().getCoordination();
        LatLng northeast = route.getBound().getNortheastCoordination().getCoordination();
        LatLngBounds bounds = new LatLngBounds(southwest, northeast);
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
    }
    @Override
    public void onDirectionSuccess(Direction direction, String rawBody) {
        if (direction.isOK()) {
            Route route = direction.getRouteList().get(0);

            ArrayList<LatLng> directionPositionList = route.getLegList().get(0).getDirectionPoint();
            Polyline polyline = mMap.addPolyline(DirectionConverter.createPolyline(this, directionPositionList, 5, Color.RED));
            polylines.add(polyline);
            setCameraWithCoordinationBounds(route);

        }
    }

    @Override
    public void onDirectionFailure(Throwable t) {
    }


}
