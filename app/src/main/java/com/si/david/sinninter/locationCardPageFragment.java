package com.si.david.sinninter;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
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

    //turns arrow at the top upside down
    public void setExpanded()
    {
        if(getView() == null)
            return;

        ImageView arrow = (ImageView)getView().findViewById(R.id.upArrow);
        Animation mirrorAnim = AnimationUtils.loadAnimation(getContext(), R.anim.arrow_up_to_down);
        mirrorAnim.setFillAfter(true);
        arrow.startAnimation(mirrorAnim);
    }

    public void setCollapsed()
    {
        if(getView() == null)
            return;

        ImageView arrow = (ImageView)getView().findViewById(R.id.upArrow);
        Animation mirrorAnim = AnimationUtils.loadAnimation(getContext(), R.anim.arrow_down_to_up);
        mirrorAnim.setFillAfter(true);
        arrow.startAnimation(mirrorAnim);
    }

    public interface OnLocationCardInteractionListener
    {
        void onArRequest();
    }
}
