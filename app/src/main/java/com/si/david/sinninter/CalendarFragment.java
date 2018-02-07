package com.si.david.sinninter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.InputStream;

public class CalendarFragment extends Fragment
{
    boolean listMode = false;

    Bitmap calendarBitmap, listModeBitmap;

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

        InputStream imageStream = getResources().openRawResource(R.raw.kalender1);
        calendarBitmap = BitmapFactory.decodeStream(imageStream);

        imageStream = getResources().openRawResource(R.raw.kalender2);
        listModeBitmap = BitmapFactory.decodeStream(imageStream);


        ((ImageView)view.findViewById(R.id.calender1)).setImageBitmap(calendarBitmap);
        FloatingActionButton fab =  (FloatingActionButton)view.findViewById(R.id.listModeButton);
        ((ImageView)view.findViewById(R.id.calender1)).setVisibility(View.INVISIBLE);


        fab.setOnClickListener(v -> {
            if(listMode)
            {
                ((ImageView)view.findViewById(R.id.calender1)).setImageBitmap(calendarBitmap);
                ((ImageView)view.findViewById(R.id.calender1)).setVisibility(View.INVISIBLE);
                fab.setImageDrawable(getResources().getDrawable(R.drawable.si_liste));

            }else
            {
                ((ImageView)view.findViewById(R.id.calender1)).setImageBitmap(listModeBitmap);
                ((ImageView)view.findViewById(R.id.calender1)).setVisibility(View.VISIBLE);
                fab.setImageDrawable(getResources().getDrawable(R.drawable.si_geschichte_kalender));

            }
            listMode = !listMode;
        });

        return view;
    }
}
