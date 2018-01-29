package com.si.david.sinninter;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;


public class locationCardPageFragment extends Fragment
{
    OnLocationCardInteractionListener arListener = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_location_card, container, false);


        ImageButton arButton = (ImageButton)(rootView.findViewById(R.id.arButton));
        arButton.setOnClickListener(v ->
        {
            if(arListener != null)
                arListener.onArRequest();
        });

        Bundle args = getArguments();

        if(args != null)
        {
            ((TextView)rootView.findViewById(R.id.title)).setText(args.getString("title"));
            ((TextView)rootView.findViewById(R.id.subtitle)).setText(args.getString("subtitle"));
            ((TextView)rootView.findViewById(R.id.content)).setText(args.getString("content"));
        }

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnLocationCardInteractionListener)
        {
            arListener = (OnLocationCardInteractionListener) context;
        } else
        {
            throw new RuntimeException(context.toString()
                    + " must implement OnLocationCardInteractionListener");
        }
    }

    public interface OnLocationCardInteractionListener
    {
        void onArRequest();
    }
}
