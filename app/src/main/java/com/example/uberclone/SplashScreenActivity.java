package com.example.uberclone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.uberclone.databinding.ActivitySplashscreenBinding;
import com.firebase.ui.auth.AuthMethodPickerLayout;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;


import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;


public class SplashScreenActivity extends AppCompatActivity {

    private final static int LOGIN_REQUEST_CODE=2323;
    private List<AuthUI.IdpConfig> providers;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener listener;




    ProgressBar progressBar;

    FirebaseDatabase database;
    DatabaseReference driverInfoRef;

    private ActivitySplashscreenBinding activitySplashscreenBinding;

    @Override
    protected void onStart() {
        super.onStart();
        delaySplashScreen();
    }

    @Override
    protected void onStop() {
        if (firebaseAuth!=null && listener!=null){
            FirebaseAuth auth=FirebaseAuth.getInstance();
            auth.removeAuthStateListener(listener);

        }
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);
        initialise();


    }



    private void initialise() {
        activitySplashscreenBinding = ActivitySplashscreenBinding.inflate(getLayoutInflater());
        setContentView(activitySplashscreenBinding.getRoot());


        database=FirebaseDatabase.getInstance();
        driverInfoRef=database.getReference(Common.DRIVER_INFO_REFERENCE);

        providers = Arrays.asList(
                new  AuthUI.IdpConfig.PhoneBuilder().build(),
                new  AuthUI.IdpConfig.GoogleBuilder().build());


        firebaseAuth=FirebaseAuth.getInstance();
        listener=myFirebaseAuth->{
            FirebaseUser user=myFirebaseAuth.getCurrentUser();
            if (user!=null) {
                checkUserFromFirebase();

            }
            else{
                    showLoginLayout();
            }
        };
    }

    private void checkUserFromFirebase() {
        driverInfoRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            Toast.makeText(SplashScreenActivity.this,"User already Registered",Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(SplashScreenActivity.this,"Goingto Register acount",Toast.LENGTH_SHORT).show();

                            showRegisterLayout();

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(SplashScreenActivity.this,""+error.getMessage(),Toast.LENGTH_SHORT).show();

                    }
                });
        }

    private void showRegisterLayout() {

        AlertDialog.Builder builder=new AlertDialog.Builder(this,R.style.DialogTheme);
        View itemView= LayoutInflater.from(this).inflate(R.layout.layout_register,null);

        TextInputEditText edit_first_name=(TextInputEditText) itemView.findViewById(R.id.first_name);
        TextInputEditText edit_last_name=(TextInputEditText) itemView.findViewById(R.id.lastname_holder);
        TextInputEditText edit_phone=(TextInputEditText) itemView.findViewById(R.id.phonenumber);

        ImageButton btn_continue=(ImageButton)itemView.findViewById(R.id.continue_btn);



        //set data
//        if (FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()!=null &&
//        !TextUtils.isEmpty(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()))
//        edit_phone.setText(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());

        if (FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()!=null && !TextUtils.isEmpty(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()));
        edit_phone.setText(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());

        //set view
        builder.setView(itemView);
        AlertDialog dialog=builder.create();
        dialog.show();

        btn_continue.setOnClickListener(v ->  {

            if (TextUtils.isEmpty(edit_first_name.getText().toString())){
                Toast.makeText(this,"Please enter First name",Toast.LENGTH_SHORT).show();
                return;
            }else if (TextUtils.isEmpty(edit_last_name.getText().toString())){
                Toast.makeText(this,"Please enter Last name",Toast.LENGTH_SHORT).show();
                return;
            }else if (TextUtils.isEmpty(edit_phone.getText().toString())){
                Toast.makeText(this,"Please enter Phone number",Toast.LENGTH_SHORT).show();
                return;
            }else{
                DriverIndoModule model=new DriverIndoModule();
                model.setFirstName(edit_first_name.getText().toString());
                model.setLastName(edit_last_name.getText().toString());
                model.setPhoneNumber(edit_phone.getText().toString());
                model.setRating(0.0);


                driverInfoRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .setValue(model)
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                dialog.dismiss();
                                Toast.makeText(SplashScreenActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(SplashScreenActivity.this,"Registered Succesfully!",Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        });


            }
        });
    }














    private void showLoginLayout() {
        AuthMethodPickerLayout authMethodPickerLayout=new AuthMethodPickerLayout
                .Builder(R.layout.layout_signin)
                .setPhoneButtonId(R.id.imageButton)
                .setGoogleButtonId(R.id.google)
                .build();

        startActivityForResult(AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAuthMethodPickerLayout(authMethodPickerLayout)
                .setIsSmartLockEnabled(false)
                .setTheme(R.style.LoginTheme)
                .setAvailableProviders(providers)
                .build(),LOGIN_REQUEST_CODE);
    }


    private void delaySplashScreen() {


        activitySplashscreenBinding.progressBar.setVisibility(View.VISIBLE);

        Completable.timer(3, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    String message = "Welcome ";
                    if (currentUser != null) {
                        message += currentUser.getUid();
                    } else {
                        message += "Guest";
                    }
                    //after show splash screen, ask login if not login
                    firebaseAuth.addAuthStateListener(listener);



                }, throwable -> {
                    // Handle any errors that might occur during the delay
                    throwable.printStackTrace();
                });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(resultCode,resultCode,data);
        if (requestCode==LOGIN_REQUEST_CODE){
            IdpResponse response=IdpResponse.fromResultIntent(data);
            if (resultCode==RESULT_OK){
                FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
            }else {
                Toast.makeText(this,"Failed to sign in"+response.getError().getMessage(),Toast.LENGTH_SHORT).show();
            }

        }
    }






}

