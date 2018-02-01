package com.si.david.sinninter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class CalendarFragment extends Fragment
{
    public CalendarFragment()
    {
        // Required empty public constructor
    }

    public static CalendarFragment newInstance()
    {
        return new CalendarFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        ViewGroup view = (ViewGroup) inflater.inflate(
                R.layout.calendar_fragment, container, false);

        return view;
    }
}
