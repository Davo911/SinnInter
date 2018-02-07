package com.si.david.sinninter;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class ArchitectFragment extends Fragment
{
    ArchitectFragmentListener listener;

    public ArchitectFragment()
    {
        // Required empty public constructor
    }

    public static ArchitectFragment newInstance(ArchitectFragmentListener listener)
    {
        ArchitectFragment frag = new ArchitectFragment();
        frag.listener = listener;

        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        ViewGroup view = (ViewGroup) inflater.inflate(
                R.layout.fragment_architect, container, false);

        ((FloatingActionButton)view.findViewById(R.id.backButton))
                .setOnClickListener(v -> listener.onCloseArchitectInfo());

        return view;
    }

    public interface ArchitectFragmentListener
    {
        void onCloseArchitectInfo();
    }
}

