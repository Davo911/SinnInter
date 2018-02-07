package com.si.david.sinninter;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

import java.util.ArrayList;

import biz.laenger.android.vpbs.BottomSheetUtils;


public class LocationPagerFragment extends Fragment
{
    ViewPager viewPager;
    LocationsPageAdapter pageAdapter;
    ViewPager.OnPageChangeListener onPageChangeListener;
    TabLayout tabLayout;

    ArrayList<locationCardPageFragment> locationCards = new ArrayList<>();
    boolean expanded = false;

    public void setActive(String title)
    {
        for(int i = 0; i < locationCards.size(); i++)
        {
            if(locationCards.get(i).locationInfo.title.equals(title))
            {
                viewPager.setCurrentItem(i);
                return;
            }
        }
    }

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

        BottomSheetUtils.setupViewPager(viewPager);

        if(onPageChangeListener != null)
            viewPager.addOnPageChangeListener(onPageChangeListener);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener()
        {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
            {
            }

            @Override
            public void onPageSelected(int position)
            {
                if(expanded)
                    locationCards.get(position).setExpanded();
                else
                    locationCards.get(position).setCollapsed();
            }

            @Override
            public void onPageScrollStateChanged(int state)
            {
            }
        });

        if(tabLayout != null)
            tabLayout.setupWithViewPager(viewPager, true);

        return view;
    }

    public void addLocation(MapFragment.LocationInfo locationInfo)
    {
        locationCards.add(locationCardPageFragment.newInstance(locationInfo));
    }

    public void addOnPageChangeListener(ViewPager.OnPageChangeListener listener)
    {
        if(viewPager != null)
            viewPager.addOnPageChangeListener(listener);
        onPageChangeListener = listener;
    }

    public void setupTabLayout(TabLayout tabLayout)
    {
        if(viewPager != null)
            tabLayout.setupWithViewPager(viewPager, true);
        this.tabLayout = tabLayout;
    }

    public void setCollapsed()
    {
        expanded = false;
        if(!locationCards.isEmpty())
        {
            locationCards.get(viewPager.getCurrentItem()).setCollapsed();
        }
    }

    public void setExpanded()
    {
        expanded = true;
        if(!locationCards.isEmpty())
        {
            locationCards.get(viewPager.getCurrentItem()).setExpanded();
        }
    }

    public MapFragment.LocationInfo.ChildLocation unlock(LatLng userPosition)
    {

        for (locationCardPageFragment locationCard : locationCards)
        {
            MapFragment.LocationInfo.ChildLocation location = locationCard.unlock(userPosition);
            if(location != null)
                return location;
        }
        return null;
    }
}
