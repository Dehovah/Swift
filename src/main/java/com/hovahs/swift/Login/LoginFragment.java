package com.hovahs.swift.Login;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.hovahs.swift.R;

public class LoginFragment extends Fragment implements View.OnClickListener {

    private EditText mEmail, mPassword;
    private Button mLogin;
    private TextView mForgotButton;


    View view;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (view == null)
            view = inflater.inflate(R.layout.fragment_login, container, false);
        else
            container.removeView(view);


        return view;
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeObjects();
    }

    void forgotPassword(){
        if (mEmail.getText().toString().trim().length() > 0)
            FirebaseAuth.getInstance().sendPasswordResetEmail(mEmail.getText().toString())
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Snackbar.make(view.findViewById(R.id.layout), "Email Sent", Snackbar.LENGTH_LONG).show();
                            }else
                                Snackbar.make(view.findViewById(R.id.layout), "Something went wrong", Snackbar.LENGTH_LONG).show();
                        }
                    });
    }
    void login(){
        final String email = mEmail.getText().toString();
        final String password = mPassword.getText().toString();
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(!task.isSuccessful()){
                    Snackbar.make(view.findViewById(R.id.layout), "sign in error", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }



    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.forgotButton:
                forgotPassword();
                break;
            case R.id.login:
                login();
                break;
        }
    }



    void initializeObjects(){
        mEmail = view.findViewById(R.id.email);
        mPassword = view.findViewById(R.id.password);
        mForgotButton = view.findViewById(R.id.forgotButton);
        mLogin = view.findViewById(R.id.login);


        mForgotButton.setOnClickListener(this);
        mLogin.setOnClickListener(this);

    }
}