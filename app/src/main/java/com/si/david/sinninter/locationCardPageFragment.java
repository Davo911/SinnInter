package com.si.david.sinninter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.NestedScrollView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

public class locationCardPageFragment extends Fragment implements
        HistoryFragment.HistoryFragmentListener,
        VotingFragment.VotingFragListener,
        ArchitectFragment.ArchitectFragmentListener,
        ImagesFragment.ImagesFragmentListener
{
    OnArRequestListener listener = null;
    HistoryFragment historyFragment;
    ArchitectFragment architectFragment;
    ImagesFragment imagesFragment;
    VotingFragment votingFragment;
    boolean isBlockedScrollView = false;

    MapFragment.LocationInfo locationInfo;

    public static locationCardPageFragment newInstance(MapFragment.LocationInfo locationInfo)
    {
        locationCardPageFragment fragment = new locationCardPageFragment();
        fragment.locationInfo = locationInfo;

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_location_card, container, false);

        ImageButton arButton = (ImageButton)(rootView.findViewById(R.id.arButton));
        arButton.setOnClickListener(v -> {
            if(listener != null)
                listener.onArRequest();
        });

        historyFragment = HistoryFragment.newInstance(this);
        architectFragment = ArchitectFragment.newInstance(this);
        imagesFragment = ImagesFragment.newInstance(this);
        votingFragment = VotingFragment.newInstance(this);

        ImageButton historyButton = (ImageButton) (rootView.findViewById(R.id.historyBtn));
        ImageButton architectButton = (ImageButton) (rootView.findViewById(R.id.architectBtn));
        ImageButton imagesButton = (ImageButton) (rootView.findViewById(R.id.imagesBtn));
        ImageView arrow = (ImageView)rootView.findViewById(R.id.upArrow);
        NestedScrollView sv = (NestedScrollView)rootView.findViewById(R.id.scrollView);

        arrow.setOnClickListener(v -> listener.onClickArrow());

        historyButton.setOnClickListener(v ->
        {
            sv.scrollTo(0, 0);
            FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.container, historyFragment);
            fragmentTransaction.commit();
        });
        historyButton.setClickable(false);


        architectButton.setOnClickListener(v ->
        {
            sv.scrollTo(0, 0);
            FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.container, architectFragment);
            fragmentTransaction.commit();
        });
        architectButton.setClickable(false);


        imagesButton.setOnClickListener(v ->
        {
            sv.scrollTo(0, 0);
            FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.container, imagesFragment);
            fragmentTransaction.commit();
        });
        imagesButton.setClickable(false);


        ImageButton likeButton = (ImageButton)(rootView.findViewById(R.id.likesIcon));
        likeButton.setOnClickListener(v -> {
            locationInfo.favorite = !locationInfo.favorite;

            if(locationInfo.favorite)
            {
                ((MainActivity)getContext()).displayDialog("Favorit hinzugefügt", 1500,
                        0, R.drawable.si_favorit_aktiv, 0, null);
                locationInfo.likes++;
            }
            else
            {
                ((MainActivity)getContext()).displayDialog("Favorit entfernt", 1500,
                        0, R.drawable.si_favorit_inaktiv, 0, null);
                locationInfo.likes--;
            }
            updateLikes(null);
        });

        ImageButton nofityButton = (ImageButton)(rootView.findViewById(R.id.notificationIcon));
        nofityButton.setOnClickListener(v -> {
            locationInfo.notificationsEnabled = !locationInfo.notificationsEnabled;

            if(locationInfo.notificationsEnabled)
            {
                ((MainActivity)getContext()).displayDialog("Benachrichtigungen aktiviert", 1500,
                        0, R.drawable.si_push_aktiv, 0, null);
            }
            else
            {
                ((MainActivity)getContext()).displayDialog("Benachrichtigungen deaktiviert", 1500,
                        0, R.drawable.si_push_inaktiv, 0, null);
            }
            updateNotificationSettings(null);
        });

        FloatingActionButton voteButton = (FloatingActionButton)rootView.findViewById(R.id.votingButton);
        voteButton.setBackgroundTintList(getResources().getColorStateList(R.color.si_fab_statelist));

        sv.setOnTouchListener((v, event) -> isBlockedScrollView);

        voteButton.setOnClickListener(v -> {
            FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.container, votingFragment);
            fragmentTransaction.commit();
            sv.scrollTo(0, 0);
            isBlockedScrollView = true;
        });


        ((TextView)rootView.findViewById(R.id.title)).setText(locationInfo.title);
        ((TextView)rootView.findViewById(R.id.subtitle)).setText(locationInfo.subtitle);
        ((TextView)rootView.findViewById(R.id.description)).setText(locationInfo.description);

        updateLikes(rootView);
        updateNotificationSettings(rootView);
        updateUnlockedLocations(rootView);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
    }

    private void updateLikes(View v)
    {
        if(v == null)
            v = getView();
        if(v == null)
            return;

        ((TextView)v.findViewById(R.id.likesTextView)).setText("" + locationInfo.likes);

        if(locationInfo.favorite)
        {
            ((ImageButton)v.findViewById(R.id.likesIcon)).setImageDrawable(
                    getResources().getDrawable(R.drawable.si_favorit_aktiv));
        }else
        {
            ((ImageButton)v.findViewById(R.id.likesIcon)).setImageDrawable(
                    getResources().getDrawable(R.drawable.si_favorit_inaktiv));
        }
    }

    private void updateNotificationSettings(View v)
    {
        if(v == null)
            v = getView();
        if(v == null)
            return;

        ImageButton notifyButton = (ImageButton)v.findViewById(R.id.notificationIcon);

        if(locationInfo.notificationsEnabled)
        {
            notifyButton.setImageDrawable(getResources().getDrawable(R.drawable.si_push_aktiv));
            notifyButton.getDrawable().setTint(getResources().getColor(R.color.colorPrimaryDark));

            ((TextView)v.findViewById(R.id.notificationText)).setText("on");

        }
        else
        {
            notifyButton.setImageDrawable(getResources().getDrawable(R.drawable.si_push_inaktiv));
            notifyButton.getDrawable().setTint(getResources().getColor(R.color.lightGrey));

            ((TextView)v.findViewById(R.id.notificationText)).setText("off");
        }
    }

    private void updateUnlockedLocations(View v)
    {
        if(v == null)
            v = getView();
        if(v == null)
            return;

        int unlocked = 0;
        for(MapFragment.LocationInfo.ChildLocation child : locationInfo.childLocations)
        {
            if(child.unlocked)
                unlocked++;
        }

        ((TextView)v.findViewById(R.id.spotsUnlocked)).setText(
                "Du hast " + unlocked + " von " + locationInfo.childLocations.size() + " Spots freigeschaltet");

        if(unlocked == locationInfo.childLocations.size())
        {
            ((TextView)v.findViewById(R.id.spotsUnlockedDescription)).setText(
                    "Du kannst jetzt an der Abstimmung teilnehmen");

            ((FloatingActionButton)v.findViewById(R.id.votingButton)).setEnabled(true);

            ImageButton historyButton = (ImageButton) (v.findViewById(R.id.historyBtn));
            historyButton.setClickable(true);
            historyButton.setImageTintList(new ColorStateList(new int[][]{{}},
                    new int[]{getResources().getColor(R.color.colorPrimaryDark)}));

            ImageButton architectButton = (ImageButton) (v.findViewById(R.id.architectBtn));
            architectButton.setClickable(true);
            architectButton.setImageTintList(new ColorStateList(new int[][]{{}},
                    new int[]{getResources().getColor(R.color.colorPrimaryDark)}));

            ImageButton imagesButton = (ImageButton) (v.findViewById(R.id.imagesBtn));
            imagesButton.setClickable(true);
            imagesButton.setImageTintList(new ColorStateList(new int[][]{{}},
                    new int[]{getResources().getColor(R.color.colorPrimaryDark)}));

        }else
        {
            ((TextView)v.findViewById(R.id.spotsUnlockedDescription)).setText(
                    "Besuche weitere Spots um die Abstimmung freizuschalten");

            ((FloatingActionButton)v.findViewById(R.id.votingButton)).setEnabled(false);

        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnArRequestListener)
        {
            listener = (OnArRequestListener) context;
        } else
        {
            throw new RuntimeException(context.toString()
                    + " must implement OnArRequestListener");
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

    @Override
    public void onCloseVoting()
    {
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.slide_out, R.anim.slide_out);
        fragmentTransaction.remove(votingFragment);
        fragmentTransaction.commit();
        isBlockedScrollView = false;
    }

    @Override
    public void onCloseHistory()
    {
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        fragmentTransaction.remove(historyFragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onCloseArchitectInfo()
    {
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        fragmentTransaction.remove(architectFragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onCloseImages()
    {
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        fragmentTransaction.remove(imagesFragment);
        fragmentTransaction.commit();
    }

    public MapFragment.LocationInfo.ChildLocation unlock(LatLng userPos)
    {
        float result[] = new float[1];
        for(MapFragment.LocationInfo.ChildLocation childLocation : locationInfo.childLocations)
        {
            if(!childLocation.unlocked)
            {
                Location.distanceBetween(userPos.latitude, userPos.longitude,
                        childLocation.coords.latitude, childLocation.coords.longitude, result);

                Log.d("unlockTest", childLocation.title + "-->" + result[0] + "m");

                if (result[0] < 150f)
                {

                    childLocation.unlocked = true;
                    updateUnlockedLocations(getView());
                    return childLocation;
                }
            }
        }
        return null;
    }

    public interface OnArRequestListener
    {
        void onArRequest();
        void onClickArrow();
    }
}
