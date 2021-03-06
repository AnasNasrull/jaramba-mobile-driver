package com.example.jarambadriver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

public class ProfileDriverActivity extends AppCompatActivity {


    FirebaseDatabase database;
    DatabaseReference databaseReference;

    //storage
    StorageReference storageReference;
    //path where image will be created
    String storagePath = "Driver_Profile_Imgs/";

    ProgressDialog progressDialog;

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;

    private static final int IMAGE_PICK_GALLERY_CODE = 300;
    private static final int IMAGE_PICK_CAMERA_CODE = 400;

    private TextView nameTv, emailTv, phoneTv, usernameTv;
    private ImageView avatarIv;

    String[] cameraPermission;
    String[] storagePermission;

    private String uid;

    //uri of picked image
    Uri image_uri;

    //for checking profile picture
    String profile;


    String driverName, trayek_pilihan, id_trip, id_bus, id_driver, chKey, endtime_hist;


//    ChipNavigationBar bottomNavigationView;
    ImageView icon_name, icon_email, icon_phone, add_photo;
    RelativeLayout greetImg;

    String key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_driver);

        //firebase component init
        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("driver");
        storageReference = FirebaseStorage.getInstance().getReference();

        //init array of permission
        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //bottom nav init
        //ChipNavigationBar bottomNavigationView =  findViewById(R.id.chipNavigationBar);
        //bottomNavigationView.setItemSelected(R.id.profile,true);

        //casting imageView
        greetImg = findViewById(R.id.layoutHeader);
        icon_email = findViewById(R.id.icon_email);
        icon_name = findViewById(R.id.icon_name);
        icon_phone = findViewById(R.id.icon_phone);
        add_photo = findViewById(R.id.add_photo);

        Glide.with(this).load(R.drawable.user_icon).into(icon_name);
        Glide.with(this).load(R.drawable.email_icon).into(icon_email);
        Glide.with(this).load(R.drawable.phone_icon).into(icon_phone);
        Glide.with(this).load(R.drawable.ic_add_a_photo_black_24dp).into(add_photo);




        Intent intent = getIntent();
        driverName = intent.getStringExtra("nama");
        trayek_pilihan = intent.getStringExtra("trayek");
        id_trip = intent.getStringExtra("id_trip");
        id_bus = intent.getStringExtra("id_bus");
        id_driver = intent.getStringExtra("key");
        chKey = intent.getStringExtra("chKey");


        //view init
        avatarIv = findViewById(R.id.img_profile_page);
        nameTv = findViewById(R.id.tv_profil_username);
        usernameTv = findViewById(R.id.txtNameProfile);
        emailTv = findViewById(R.id.tv_profil_email);
        phoneTv = findViewById(R.id.tv_profil_phone);

        setTitle("Profile Driver");

        //init progres dialog
        progressDialog = new ProgressDialog(ProfileDriverActivity.this);

        //setOnNavigationSelectedListener();
        greeting();

        //progress dialog shown to wait retrieve data
        progressDialog();

        Query query = databaseReference.orderByChild("key").equalTo(id_driver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    //get children from firebase
                    String email = ""+ds.child("email").getValue();
                    String name = ""+ds.child("nama").getValue();
                    String phone = ""+ds.child("no_telp").getValue();
                    String image = ""+ds.child("image").getValue();
                    key = ""+ds.child("key").getValue();

                    //set data
                    nameTv.setText(name);
                    usernameTv.setText(name);
                    emailTv.setText(email);
                    phoneTv.setText(phone);


                    //Picasso.get().load(R.drawable.ic_face_black_24dp).into(avatarIv);
                    try {
                        //if image is received then set
                        Glide.with(ProfileDriverActivity.this).load(image).into(avatarIv);
                    } catch (Exception e) {
                        //if there is any exception while getting image then set default
                        Picasso.get().load(R.drawable.ic_face_black_24dp).into(avatarIv);
                    }

//                    Intent i = new Intent(ProfileDriverActivity.this, Trip_start.class);
//                    i.putExtra("driver_name", name);
//                    i.putExtra("id_driver", key);

                    progressDialog.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        ChipNavigationBar bottomNavigationView =  findViewById(R.id.chipNavigationBar);
        bottomNavigationView.setItemSelected(R.id.profile,true);
        bottomNavigationView.setOnItemSelectedListener(new ChipNavigationBar.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int i) {
                switch (i) {
                    case R.id.history:
                        Intent intent3 = new Intent(ProfileDriverActivity.this, HistoryDriver.class);
                        intent3.putExtra("nama", driverName);
                        intent3.putExtra("trayek", trayek_pilihan);
                        intent3.putExtra("key", id_driver);
                        intent3.putExtra("id_bus", id_bus);
                        intent3.putExtra("id_trip", id_trip);
                        intent3.putExtra("chKey", chKey);
                        startActivity(intent3);
                        finish();
                        break;

                    case R.id.nav_home:
                        Intent intent2 = new Intent(ProfileDriverActivity.this, HomeActivity.class);
                        intent2.putExtra("nama", driverName);
                        intent2.putExtra("key", id_driver);
                        intent2.putExtra("trayek", trayek_pilihan);
                        intent2.putExtra("id_trip", id_trip);
                        intent2.putExtra("id_bus", id_bus);
                        intent2.putExtra("chKey", chKey);
                        startActivity(intent2);
                        finish();
                        break;

                    case R.id.trip:
                        Intent intent = new Intent(ProfileDriverActivity.this, Trip_start.class);
                        intent.putExtra("nama", driverName);
                        intent.putExtra("key", id_driver);
                        intent.putExtra("trayek", trayek_pilihan);
                        intent.putExtra("id_trip", id_trip);
                        intent.putExtra("id_bus", id_bus);
                        intent.putExtra("chKey", chKey);
                        startActivity(intent);
                        finish();
                        break;

                }
            }
        });
    }

    private void progressDialog() {
        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }

/*    private void setOnNavigationSelectedListener() {
        bottomNavigationView.setOnItemSelectedListener(new ChipNavigationBar.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int i) {
                switch (i) {
                    case R.id.history:
                        Intent intent3 = new Intent(ProfileDriverActivity.this, HistoryDriver.class);
                        intent3.putExtra("nama", driverName);
                        intent3.putExtra("trayek",trayek_pilihan);
                        intent3.putExtra("key", id_driver);
                        intent3.putExtra("id_bus",id_bus);
                        intent3.putExtra("id_trip", id_trip);
                        intent3.putExtra("chKey", chKey);
                        startActivity(intent3);
                        finish();
                        break;

                    case R.id.nav_home:
                        Intent intent2 = new Intent(ProfileDriverActivity.this, HomeActivity.class);
                        intent2.putExtra("nama", driverName);
                        intent2.putExtra("key", id_driver);
                        intent2.putExtra("trayek", trayek_pilihan);
                        intent2.putExtra("id_trip", id_trip);
                        intent2.putExtra("id_bus", id_bus);
                        intent2.putExtra("chKey", chKey);
                        startActivity(intent2);
                        finish();
                        break;

                    case R.id.trip:
                        Intent intent = new Intent(ProfileDriverActivity.this, Trip_start.class);
                        intent.putExtra("nama", driverName);
                        intent.putExtra("key", id_driver);
                        intent.putExtra("trayek", trayek_pilihan);
                        intent.putExtra("id_trip", id_trip);
                        intent.putExtra("id_bus", id_bus);
                        intent.putExtra("chKey", chKey);
                        startActivity(intent);
                        finish();
                        break;

                }
            }
        });
    } */

    public void logout(View view) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Konfirmasi Logout aplikasi");
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
                driverLocationRef.child(id_driver).updateChildren(driverLocRef);

                if(id_bus != null){
                    DatabaseReference busRef = FirebaseDatabase.getInstance().getReference("bus");
                    HashMap<String, Object> status = new HashMap<>();
                    status.put("status", "tidak aktif");
                    busRef.child(id_bus).updateChildren(status);
                }
                if(id_trip != null){
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
                    myRef.child("Mobile_Apps").child("Driver").child(id_driver).child("History_Trip_Driver").child(chKey).updateChildren(Etime);
                }


                startActivity(new Intent(ProfileDriverActivity.this, LoginPage.class));
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


    @SuppressLint("SetTextI18n")
    private void greeting() {
        Calendar calendar = Calendar.getInstance();
        int timeOfDay = calendar.get(Calendar.HOUR_OF_DAY);

        if (timeOfDay >= 0 && timeOfDay < 18){
            greetImg.setBackgroundResource(R.drawable.header_morning);
        } else if (timeOfDay >= 18 && timeOfDay < 24) {
            greetImg.setBackgroundResource(R.drawable.header_night);
            usernameTv.setTextColor(Color.parseColor("#FFFFFF"));
        }

    }


    public void changeProfilePicture(View view) {
        profile = "image";
        String option[] = {"Kamera","Galeri"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pilih mode pengambilan gambar");
        builder.setItems(option, new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (which == 0) {
                    //kamera clicked
                    if(!checkCameraPermission()){
                        requestCameraPermission();
                    } else {
                        pickFromCamera();
                    }
                } else if (which == 1) {
                    //galeri clicked
                    if(!checkStoragePermission()){
                        requsetStoragePermission();
                    } else {
                        pickFromGaleri();
                    }
                }
            }
        });
        builder.create().show();
    }

    private void pickFromGaleri() {
        Intent galeryIntent = new Intent(Intent.ACTION_PICK);
        galeryIntent.setType("image/*");
        startActivityForResult(galeryIntent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera() {
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE, "Temp Pict");
        cv.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");

        //put image uri
        image_uri = ProfileDriverActivity.this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                cv);

        //intent to start camera
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requsetStoragePermission() {
        requestPermissions(storagePermission, STORAGE_REQUEST_CODE);
    }

    private boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestCameraPermission() {
        requestPermissions(cameraPermission, CAMERA_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return  result && result1;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case  CAMERA_REQUEST_CODE: {
                if(grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if(cameraAccepted && writeStorageAccepted) {
                        pickFromCamera();
                    } else {
                        Toast.makeText(ProfileDriverActivity.this, "Mohon setujui permission Kamera & Penyimpanan", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;

            case STORAGE_REQUEST_CODE: {
                if(grantResults.length > 0) {
                    boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if(writeStorageAccepted) {
                        pickFromGaleri();
                    } else {
                        Toast.makeText(ProfileDriverActivity.this, "Mohon setujui permission Penyimpanan", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode == RESULT_OK) {
            if(requestCode == IMAGE_PICK_GALLERY_CODE) {
                image_uri = data.getData();
                uploadProfileDriverPhoto(image_uri);
            }

            else if(requestCode == IMAGE_PICK_CAMERA_CODE) {
                uploadProfileDriverPhoto(image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadProfileDriverPhoto(final Uri uri) {
        progressDialog();

        String filePathandName = storagePath + ""+ profile + "_" + key;

        StorageReference storageReference2nd = storageReference.child(filePathandName);
        storageReference2nd.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();

                        while (!uriTask.isSuccessful());
                        final Uri downloadUri = uriTask.getResult();

                        if(uriTask.isSuccessful()) {
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put(profile, downloadUri.toString());

                            databaseReference.child(key).updateChildren(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            progressDialog.dismiss();
                                            Toast.makeText(ProfileDriverActivity.this, "Gambar berhasil diperbarui", Toast.LENGTH_SHORT).show();
                                            Intent intent2 = new Intent(ProfileDriverActivity.this, HomeActivity.class);
                                            intent2.putExtra("nama", driverName);
                                            intent2.putExtra("key", id_driver);
                                            intent2.putExtra("trayek", trayek_pilihan);
                                            intent2.putExtra("id_trip", id_trip);
                                            intent2.putExtra("id_bus", id_bus);
                                            intent2.putExtra("chKey", chKey);
                                            startActivity(intent2);
                                            finish();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    Toast.makeText(ProfileDriverActivity.this, "Maaf, upload tidak berhasil", Toast.LENGTH_SHORT).show();
                                }
                            });


                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(ProfileDriverActivity.this, "Maaf, terdapat gangguan ketika upload", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(ProfileDriverActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void editProfile (View view) {
        String []options = {"Ubah nama pengguna", "Ubah nomor handphone"};


        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileDriverActivity.this);
        builder.setTitle("Pilihan");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which == 0) {
                    showNamePhoneUpdateDialog("nama");
                } else if(which == 1) {
                    showNamePhoneUpdateDialog("no_telp");
                }
            }
        });
        builder.create().show();
    }


    private void showNamePhoneUpdateDialog(final String key_ki) {
        final String keys;
        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileDriverActivity.this);
        if(key_ki.equals("nama")){
            keys = "nama pengguna";
            builder.setIcon(R.drawable.ic_person_black_24dp);
        } else {
            keys = "nomor handphone";
            builder.setIcon(R.drawable.ic_smartphone_black_24dp);
        }
        builder.setTitle("Memperbarui " + keys);

        LinearLayout  linearLayout = new LinearLayout(ProfileDriverActivity.this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10,10,10,10);

        final EditText editText = new EditText(ProfileDriverActivity.this);
        if(key_ki.equals("nama")){
            editText.setHint("Masukkan " + keys);
            editText.setInputType(InputType.TYPE_CLASS_TEXT);
        } else {
            editText.setHint("Masukkan " + keys);
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        }

        linearLayout.addView(editText);
        builder.setView(linearLayout);

        //ADA YANG JANGGAL, KETIKA UBAH NAMA / NO TELEPON IA BAKAL REQUEST INTENT KE HOMEACTIVITY, PADAHAL TIDAK ADA PERINTAH DEMIKIAN
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                    final String value = editText.getText().toString().trim();
                    if(!TextUtils.isEmpty(value)) {
                        progressDialog();

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put(key_ki, value);

                        databaseReference.child(key).updateChildren(hashMap)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        progressDialog.dismiss();
                                        Toast.makeText(ProfileDriverActivity.this, keys + " diperbarui...",Toast.LENGTH_SHORT).show();


                                            Intent intent2 = new Intent(ProfileDriverActivity.this, HomeActivity.class);
                                            if(key_ki.equals("nama")){
                                                intent2.putExtra("nama", value);
                                                intent2.putExtra("key", id_driver);
                                                intent2.putExtra("trayek", trayek_pilihan);
                                                intent2.putExtra("id_trip", id_trip);
                                                intent2.putExtra("id_bus", id_bus);
                                                intent2.putExtra("chKey", chKey);

                                                if(id_trip!=null){
                                                    DatabaseReference refNama = FirebaseDatabase.getInstance().getReference("history_trip_dashboard");
                                                    HashMap<String, Object> hashMap = new HashMap<>();
                                                    hashMap.put("driver_name", value);
                                                    refNama.child(id_trip).updateChildren(hashMap);
                                                }
                                            } else {
                                                intent2.putExtra("nama", driverName);
                                                intent2.putExtra("key", id_driver);
                                                intent2.putExtra("trayek", trayek_pilihan);
                                                intent2.putExtra("id_trip", id_trip);
                                                intent2.putExtra("id_bus", id_bus);
                                                intent2.putExtra("chKey", chKey);
                                            }

                                            startActivity(intent2);
                                            finish();
                                        }


                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(ProfileDriverActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(ProfileDriverActivity.this, keys +  " tidak boleh kosong...", Toast.LENGTH_SHORT ).show();
                    }
            }
        });

        builder.setNegativeButton("Batal", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                progressDialog.dismiss();
            }
        });
        builder.create().show();


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
                driverLocationRef.child(id_driver).updateChildren(driverLocRef);

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


                startActivity(new Intent(ProfileDriverActivity.this, LoginPage.class));
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
