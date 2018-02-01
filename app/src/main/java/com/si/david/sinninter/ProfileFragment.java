package com.si.david.sinninter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ProfileFragment extends Fragment
{

    public ProfileFragment()
    {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance()
    {
        return new ProfileFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        ViewGroup view = (ViewGroup) inflater.inflate(
                R.layout.profile_fragment, container, false);

        return view;
    }
}
