package com.si.david.sinninter;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


public class LoginFragment extends Fragment
{
    LoginPageListener listener;

    public LoginFragment()
    {
        // Required empty public constructor
    }

    public static LoginFragment newInstance()
    {
        return new LoginFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        ViewGroup view = (ViewGroup) inflater.inflate(
                R.layout.login_screen_fragment, container, false);

        FloatingActionButton confirmButton = (FloatingActionButton)view.findViewById(R.id.confirm);
        confirmButton.setOnClickListener(v -> listener.onLoginSuccessful());

        FloatingActionButton loginButton = (FloatingActionButton)view.findViewById(R.id.positiveButton);
        FloatingActionButton registerButton = (FloatingActionButton)view.findViewById(R.id.neutralButotn);

        loginButton.setOnClickListener(v -> {
            loginButton.setElevation(0);
            loginButton.setEnabled(false);
            registerButton.setElevation(5);
            registerButton.setEnabled(true);
            ((ImageView)view.findViewById(R.id.registerButtonBackground2)).setVisibility(ImageView.INVISIBLE);
            ((ImageView)view.findViewById(R.id.loginButtonBackground)).setVisibility(ImageView.VISIBLE);
            ((TextView)view.findViewById(R.id.title)).setText("Anmelden");
        });

        registerButton.setOnClickListener(v -> {
            loginButton.setElevation(5);
            loginButton.setEnabled(true);
            registerButton.setElevation(0);
            registerButton.setEnabled(false);
            ((ImageView)view.findViewById(R.id.registerButtonBackground2)).setVisibility(ImageView.VISIBLE);
            ((ImageView)view.findViewById(R.id.loginButtonBackground)).setVisibility(ImageView.INVISIBLE);
            ((TextView)view.findViewById(R.id.title)).setText("Registrieren");
        });

        loginButton.callOnClick();

        return view;
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if (context instanceof LoginPageListener)
        {
            listener = (LoginPageListener) context;
        } else
        {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        listener = null;
    }

    public interface LoginPageListener
    {
        void onLoginSuccessful();
    }
}
