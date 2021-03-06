package com.example.jarambadriver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.google.firebase.database.ValueEventListener;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class HomeActivity extends FragmentActivity implements OnMapReadyCallback {

    //maps properti
    private GoogleMap mMap;
    Location curentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;
    private static final int REQUEST_CODE = 101;
    LocationRequest locationRequest;

    Marker userLocationMarker;
    Circle userLocationAccuracy;



    String nama, key;
    String trayek, id_trip, id_bus, chKey, endtime_hist;





    long START_TIME_IN_MILLIS;
    private TextView vCounter;
    private CountDownTimer mCountDownTimer;
    private boolean mTimerRunning;
    private long mTimeLeftInMills = START_TIME_IN_MILLIS;

    long hasil;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        Intent i = getIntent();
        nama = i.getStringExtra("nama");
        key = i.getStringExtra("key");
        trayek = i.getStringExtra("trayek");
        id_trip = i.getStringExtra("id_trip");
        id_bus = i.getStringExtra("id_bus");

        chKey = i.getStringExtra("chKey");

      //  Toast.makeText(this, id_bus + "\n" + key, Toast.LENGTH_SHORT).show();

        vCounter = findViewById(R.id.txt_count);

        if(trayek==null){
            trayek = "Belum memilih trayek";
        }

       // Toast.makeText(HomeActivity.this, trayek + "\n" + id_trip + "\n" + nama + "\n" + id_bus, Toast.LENGTH_LONG).show();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fetchLastLocation();


        ChipNavigationBar bottomNavigationView =  findViewById(R.id.chipNavigationBar);
        bottomNavigationView.setItemSelected(R.id.nav_home,true);
        bottomNavigationView.setOnItemSelectedListener(new ChipNavigationBar.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int i) {
                switch (i) {
                    case R.id.history:
                        Intent intent3 = new Intent(HomeActivity.this, HistoryDriver.class);
                        intent3.putExtra("nama", nama);
                        intent3.putExtra("trayek",trayek);
                        intent3.putExtra("key", key);
                        intent3.putExtra("id_bus",id_bus);
                        intent3.putExtra("id_trip", id_trip);
                        intent3.putExtra("chKey", chKey);
                        startActivity(intent3);
                        finish();
                        break;
                    case R.id.trip:
                        Intent intent = new Intent(HomeActivity.this, Trip_start.class);
                        intent.putExtra("nama", nama);
                        intent.putExtra("id_trip", id_trip);
                        intent.putExtra("key",key);
                        intent.putExtra("trayek", trayek);
                        intent.putExtra("id_bus", id_bus);
                        intent.putExtra("chKey", chKey);
                        startActivity(intent);
                        finish();
                        break;
                    case R.id.profile:
                        Intent intent2 = new Intent(HomeActivity.this, ProfileDriverActivity.class);
                        intent2.putExtra("nama", nama);
                        intent2.putExtra("id_trip", id_trip);
                        intent2.putExtra("key",key);
                        intent2.putExtra("trayek",trayek);
                        intent2.putExtra("id_bus", id_bus);
                        intent2.putExtra("chKey", chKey);
                        startActivity(intent2);
                        finish();
                        break;
                }
            }
        });

        //set berapa detik sekali akan update ke database
        locationRequest = new LocationRequest();
        locationRequest.setInterval(15000);
        locationRequest.setFastestInterval(15000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("jam");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String jam = snapshot.child("selesai").getValue(String.class);

                String x = jam.substring(0,2);
                String y = jam.substring(3);

                long  jam_selesai = Integer.parseInt(x) * 3600 * 1000;
                long  menit_selesai = Integer.parseInt(y) * 60 * 1000;

                long hasil = jam_selesai+menit_selesai;

                //Toast.makeText(HomeActivity.this, x + " + "+ y + "\n" + hasil, Toast.LENGTH_LONG).show();

                Calendar rightNow = Calendar.getInstance();
                int currentHour = rightNow.get(Calendar.HOUR_OF_DAY);
                int currentMinute = rightNow.get(Calendar.MINUTE);
                int currentSec = rightNow.get(Calendar.SECOND);

                long currTime = (currentHour*3600*1000) + (currentMinute*60*1000) + (currentSec*1000);
                START_TIME_IN_MILLIS = hasil - currTime;
                mTimeLeftInMills = START_TIME_IN_MILLIS;

                if (currTime <= hasil){
                    startTimer();

                    updateCountDownText();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }

    public void startTimer(){
        mCountDownTimer = new CountDownTimer(mTimeLeftInMills, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimeLeftInMills = millisUntilFinished;
                updateCountDownText();
            }

            @Override
            public void onFinish() {

                AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                builder.setTitle("Terimakasih " + nama);
                builder.setMessage("Waktu mengemudi kamu sudah habis, sistem akan melakukan Logout otomatis");
                builder.setPositiveButton("Selesai", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        //auto hentikan trip
                        //null kan trayek
                        DatabaseReference driverLocationRef = FirebaseDatabase.getInstance().getReference("Driver Location");
                        HashMap<String, Object> driverLocRef = new HashMap<>();
                        driverLocRef.put("trayek", null);
                        driverLocationRef.child(key).updateChildren(driverLocRef);

                        //ubah status bus menjadi tidak aktif

                        if(id_bus != null) {
                            DatabaseReference statusBusRef = FirebaseDatabase.getInstance().getReference("bus");
                            HashMap<String, Object> statusBus = new HashMap<>();
                            statusBus.put("status","Bus tidak aktif");

                            //akan NPE ketika tidak melaksanakan trip
                            statusBusRef.child(id_bus).updateChildren(statusBus);
                        }

                        if(id_trip != null){
                            //selesaikan history_trip_dashboard
                            //getCurrent time clock
                            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+8:00"));
                            Date currentLocalTime = cal.getTime();
                            @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("HH:mm a");
                            String localTime = dateFormat.format(currentLocalTime);

                            DatabaseReference historyTripDashboardRef = FirebaseDatabase.getInstance().getReference("history_trip_dashboard");
                            HashMap<String, Object> historyTripDriver = new HashMap<>();
                            historyTripDriver.put("end_time", localTime);
                            historyTripDriver.put("status","tidak aktif");
                            historyTripDashboardRef.child(id_trip).updateChildren(historyTripDriver);

                            //Mengirim data ke DB history driver
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference myRef = database.getReference();
                            HashMap<String, Object> Etime = new HashMap<>();
                            Etime.put("end_time", localTime);
                            Etime.put("status", "done");
                            myRef.child("Mobile_Apps").child("Driver").child(key).child("History_Trip_Driver").child(chKey).updateChildren(Etime);
                        }

                        startActivity(new Intent(HomeActivity.this, LoginPage.class));
                        finish();
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();

//                startActivity(new Intent(HomeActivity.this, LoginPage.class));
//                finish();
            }
        }.start();
        mTimerRunning = true;
    }

    private void updateCountDownText(){
        int hours = (int) (mTimeLeftInMills / 1000) / 3600;
        int minutes = (int) ((mTimeLeftInMills / 1000)  % 3600) / 60;
        int seconds = (int) (mTimeLeftInMills / 1000) % 60;

        String timeLeftFormatted;
        if(hours > 0) {
            timeLeftFormatted = String.format(Locale.getDefault(),"%18d:%02d:%02d", hours, minutes, seconds);

        } else {
            timeLeftFormatted = String.format(Locale.getDefault(),"%02d:%02d", minutes, seconds);

        }

        vCounter.setText(timeLeftFormatted);
    }


    //get latLng / location callback
    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {


                super.onLocationResult(locationResult);
                Log.d("LOCATION_RESULT", String.valueOf(locationResult.getLastLocation()));

                if(mMap != null){
                    setUserLocationMarker(locationResult.getLastLocation());


                }
        }
    };

    //set auto update marker
    private void setUserLocationMarker(Location location){


        final LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        if(userLocationMarker == null){
            //create new marker
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_bus));
            markerOptions.rotation(location.getBearing());
            markerOptions.anchor((float) 0.5, (float) 0.5);
            markerOptions.title("Trayek : "+ trayek);
            userLocationMarker = mMap.addMarker(markerOptions);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));

        }else {
            //use the prev creatd marker
            userLocationMarker.setPosition(latLng);
            userLocationMarker.setRotation(location.getBearing());
            userLocationMarker.setTitle("Trayek : " + trayek);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
        }

        if(userLocationAccuracy == null) {
            CircleOptions circleOptions = new CircleOptions();
            circleOptions.center(latLng);
            circleOptions.strokeWidth(4);
            circleOptions.strokeColor(Color.argb(255, 0,0,255));
            circleOptions.fillColor(Color.argb(32, 0,0,255));
            circleOptions.radius(location.getAccuracy());
            userLocationAccuracy = mMap.addCircle(circleOptions);
        } else {
            userLocationAccuracy.setCenter(latLng);
            userLocationAccuracy.setRadius(location.getAccuracy());
        }




        //update database tiap 15000 ms, kalau udah start trip tapi
        if(id_bus!=null && key!=null && id_trip!=null){
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Driver Location");
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("key", key);
            hashMap.put("latitude", location.getLatitude());
            hashMap.put("longtitude" , location.getLongitude());
            hashMap.put("trayek", trayek);
            ref.child(key).updateChildren(hashMap);

            DatabaseReference refBus = FirebaseDatabase.getInstance().getReference("bus");
            HashMap<String, Object> hmBus = new HashMap<>();
            hmBus.put("location", location.getLatitude() + ", " + location.getLongitude());
            refBus.child(id_bus).updateChildren(hmBus);

            DatabaseReference refHistory = FirebaseDatabase.getInstance().getReference("history_trip_dashboard");
            HashMap<String, Object> hmHistory = new HashMap<>();
            hmHistory.put("gps", location.getLatitude() + ", " + location.getLongitude());
            refHistory.child(id_trip).updateChildren(hmHistory);
        }


        DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference("Driver Location");
        driverRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {


                double lat = (double) snapshot.child("latitude").getValue();
                double lon = (double) snapshot.child("longtitude").getValue();
                String keys = (String)snapshot.child("key").getValue();
                String trayexxx = (String)snapshot.child("trayek").getValue();


                LatLng newLocation = new LatLng(
                        lat,
                        lon
                );

                if(trayexxx==null){
                    trayexxx = "Belum memilih trayek";
                }



                    //marker nya tidak mau terhapus
                     mMap.addMarker(new MarkerOptions()
                            .position(newLocation)
                            .title("Trayek: " + trayexxx)
                             .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_bus))
                            .snippet(keys));



            }


            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        mMap.clear();


    }


    private void startLocationUpdates(){
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void stopLocationUpdates(){
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            startLocationUpdates();
        } else {
            //req permission
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopLocationUpdates();
    }

    //ngambil current location
    private void fetchLastLocation(){
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            return;
        }
                Task<Location> task = fusedLocationProviderClient.getLastLocation();
                task.addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if(location!=null){
                            curentLocation = location;
                            SupportMapFragment supportMapFragment = (SupportMapFragment)
                                    getSupportFragmentManager().findFragmentById(R.id.map);
                            supportMapFragment.getMapAsync(HomeActivity.this);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(HomeActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
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
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

//        // Add a marker in Sydney and move the camera
//        LatLng itb = new LatLng(-6.891469, 107.610756);
//        mMap.addMarker(new MarkerOptions().position(itb).title("Institut Teknologi Bandung"));
//        mMap.setMyLocationEnabled(true);
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(itb , 14.0f));

        LatLng latLng = new LatLng(
                curentLocation.getLatitude(),
                curentLocation.getLongitude()
        );



//        MarkerOptions markerOptions = new MarkerOptions()
//                .position(latLng).title("Posisi anda saat ini").icon(
//                        BitmapDescriptorFactory.fromResource(R.drawable.marker_bus));
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
//        mMap.addMarker(markerOptions);
//        mMap.setMyLocationEnabled(true);
    }

    public void onBackPressed() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Konfirmasi keluar aplikasi");
        builder.setIcon(R.drawable.ic_exit_to_app_black_24dp);
        builder.setMessage("Anda yakin ingin Logout ?\n\nJika anda belum menyelesaikan perjalanan, maka otomatis perjalanan anda akan diberhentikan ");
        builder.setCancelable(false);

        builder.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                FirebaseAuth.getInstance().signOut();
//                startActivity(new Intent(ProfileDriverActivity.this, LoginPage.class));
//                finish();

                DatabaseReference driverLocationRef = FirebaseDatabase.getInstance().getReference("Driver Location");
                HashMap<String, Object> driverLocRef = new HashMap<>();
                driverLocRef.put("trayek", null);
                driverLocationRef.child(key).updateChildren(driverLocRef);
                if (id_bus != null && id_trip != null) {
                    DatabaseReference busRef = FirebaseDatabase.getInstance().getReference("bus");
                    HashMap<String, Object> status = new HashMap<>();
                    status.put("status", "tidak aktif");
                    busRef.child(id_bus).updateChildren(status);
                }

                if (id_trip != null) {
                    //getCurrent time clock
                    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+8:00"));
                    Date currentLocalTime = cal.getTime();
                    @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("HH:mm a");
                    String localTime = dateFormat.format(currentLocalTime);

                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("history_trip_dashboard");
                    HashMap<String, Object> status_endTime = new HashMap<>();
                    status_endTime.put("end_time", localTime);
                    status_endTime.put("status", "tidak aktif");
                    endtime_hist = localTime; //mengirim data waktu selesai untuk history driver
                    reference.child(id_trip).updateChildren(status_endTime);

                    //Mengirim data ke DB history driver
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference();
                    HashMap<String, Object> Etime = new HashMap<>();
                    Etime.put("end_time", endtime_hist);
                    Etime.put("status", "done");
                    myRef.child("Mobile_Apps").child("Driver").child(key).child("History_Trip_Driver").child(chKey).updateChildren(Etime);
                }


                startActivity(new Intent(HomeActivity.this, LoginPage.class));
                finish();
            }
        });

        builder.setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        androidx.appcompat.app.AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
