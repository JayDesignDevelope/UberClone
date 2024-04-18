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

        initialise();


    }



    private void initialise() {

        providers = Arrays.asList(
                new  AuthUI.IdpConfig.PhoneBuilder().build(),
                new  AuthUI.IdpConfig.GoogleBuilder().build());


        firebaseAuth=FirebaseAuth.getInstance();
        listener=myFirebaseAuth->{
            FirebaseUser user=myFirebaseAuth.getCurrentUser();
            if (user!=null) {
                Toast.makeText(this,"Welcome"+user.getUid(),Toast.LENGTH_SHORT).show();

            }
            else{
                    showLoginLayout();
            }
        };
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


//        activitySplashscreenBinding.progressBar.setVisibility(View.VISIBLE);

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

