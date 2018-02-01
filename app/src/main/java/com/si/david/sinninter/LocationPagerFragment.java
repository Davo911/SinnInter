package com.si.david.sinninter;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;


public class LocationPagerFragment extends Fragment
{
    ViewPager viewPager;
    LocationsPageAdapter pageAdapter;
    ViewPager.OnPageChangeListener onPageChangeListener;

    ArrayList<locationCardPageFragment> locationCards = new ArrayList<>();


    class LocationsPageAdapter extends FragmentStatePagerAdapter
    {
        public LocationsPageAdapter(FragmentManager fm)
        {
            super(fm);
        }


        @Override
        public Fragment getItem(int position)
        {
            return locationCards.get(position);
        }

        @Override
        public int getCount()
        {
            return locationCards.size();
        }

        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1)
        {
            //super.restoreState(arg0, arg1);
        }
    }

    public LocationPagerFragment()
    {
        // Required empty public constructor
    }

    public static LocationPagerFragment newInstance()
    {
        return new LocationPagerFragment();
    }


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        pageAdapter = new LocationsPageAdapter(getChildFragmentManager());

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        ViewGroup view = (ViewGroup) inflater.inflate(
                R.layout.location_info_pager, container, false);

        viewPager = (ViewPager)view.findViewById(R.id.viewPager);
        viewPager.setAdapter(pageAdapter);

        TabLayout dots = (TabLayout)view.findViewById(R.id.dots);
        dots.setupWithViewPager(viewPager, true);

        if(onPageChangeListener != null)
            viewPager.addOnPageChangeListener(onPageChangeListener);

        return view;
    }

    public void addLocation(String title, String subtitle, String description, LatLng position, String parent)
    {
        locationCardPageFragment newCard = new locationCardPageFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("subtitle", subtitle);
        args.putString("content", description);
        newCard.setArguments(args);

        locationCards.add(newCard);
    }

    public void addOnPageChangeListener(ViewPager.OnPageChangeListener listener)
    {
        if(viewPager != null)
            viewPager.addOnPageChangeListener(listener);
        onPageChangeListener = listener;
    }

    public void setCollapsed()
    {
        if(!locationCards.isEmpty())
        {
            locationCards.get(viewPager.getCurrentItem()).setCollapsed();
        }
    }

    public void setExpanded()
    {
        if(!locationCards.isEmpty())
        {
            locationCards.get(viewPager.getCurrentItem()).setExpanded();
        }
    }
}
