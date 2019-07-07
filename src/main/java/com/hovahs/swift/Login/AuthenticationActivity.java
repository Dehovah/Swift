package com.hovahs.swift.Login;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.hovahs.swift.R;

public class AuthenticationActivity extends AppCompatActivity {

    FragmentManager fm = getSupportFragmentManager();

    MenuFragment menuFragment = new MenuFragment();

    private FirebaseAuth.AuthStateListener firebaseAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);


        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user!=null){
                    Intent intent = new Intent(AuthenticationActivity.this, LauncherActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        };

        fm.beginTransaction()
                .replace(R.id.container, menuFragment, "StartFragment")
                .addToBackStack(null)
                .commit();
    }

    public void registrationClick(){
        fm.beginTransaction()
                .replace(R.id.container, new RegisterFragment(), "RegisterFragment")
                .addToBackStack(null)
                .commit();
    }
    public void loginClick(){
        fm.beginTransaction()
                .replace(R.id.container, new LoginFragment(), "RegisterFragment")
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            getSupportFragmentManager().popBackStack();
        } else {
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener(firebaseAuthListener);
    }
    @Override
    protected void onStop() {
        super.onStop();
        FirebaseAuth.getInstance().removeAuthStateListener(firebaseAuthListener);
    }
}
