package com.hovahs.swift.Login;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.hovahs.swift.R;


public class MenuFragment extends Fragment implements View.OnClickListener {

    Button mLogin, mRegistration;

    View view;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (view == null)
            view = inflater.inflate(R.layout.fragment_menu, container, false);
        else
            container.removeView(view);


        return view;
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeObjects();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.registration:
                ((AuthenticationActivity) getActivity()).registrationClick();
                break;
            case R.id.login:
                ((AuthenticationActivity) getActivity()).loginClick();
                break;
        }
    }



    void initializeObjects(){
        mLogin = view.findViewById(R.id.login);
        mRegistration = view.findViewById(R.id.registration);

        mRegistration.setOnClickListener(this);
        mLogin.setOnClickListener(this);

    }
}