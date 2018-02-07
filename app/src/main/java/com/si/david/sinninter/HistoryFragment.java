package com.si.david.sinninter;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class HistoryFragment extends Fragment
{
    HistoryFragmentListener listener;

    public HistoryFragment()
    {
        // Required empty public constructor
    }

    public static HistoryFragment newInstance(HistoryFragmentListener listener)
    {
        HistoryFragment frag = new HistoryFragment();
        frag.listener = listener;

        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        ViewGroup view = (ViewGroup) inflater.inflate(
                R.layout.fragment_history, container, false);

        ((FloatingActionButton)view.findViewById(R.id.backButton))
                .setOnClickListener(v -> listener.onCloseHistory());

        return view;
    }

    public interface HistoryFragmentListener
    {
        void onCloseHistory();
    }
}
