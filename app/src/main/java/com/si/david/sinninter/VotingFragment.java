package com.si.david.sinninter;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

public class VotingFragment extends Fragment
{
    VotingFragListener listener;

    public VotingFragment()
    {
        // Required empty public constructor
    }

    public static VotingFragment newInstance(VotingFragListener listener)
    {
        VotingFragment frag = new VotingFragment();
        frag.listener = listener;

        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        ViewGroup view = (ViewGroup) inflater.inflate(
                R.layout.voting_fragment, container, false);


        view.findViewById(R.id.text_layout).setAlpha(0f);

        EditText editText = (EditText)view.findViewById(R.id.opinionText);

        FloatingActionButton positiveButton = (FloatingActionButton)view.findViewById(R.id.positiveButton);
        FloatingActionButton neutralButton = (FloatingActionButton)view.findViewById(R.id.neutralButotn);
        FloatingActionButton negativeButton = (FloatingActionButton)view.findViewById(R.id.negativeButton);
        FloatingActionButton voteButton = (FloatingActionButton)view.findViewById(R.id.confirmVote);

        ImageView posiviveBackground = (ImageView)view.findViewById(R.id.positiveBackground);
        ImageView neutralBackground = (ImageView)view.findViewById(R.id.neutralBackground);
        ImageView negativeBackground = (ImageView)view.findViewById(R.id.negativeBackground);

        posiviveBackground.setAlpha(0f);
        neutralBackground.setAlpha(0f);
        negativeBackground.setAlpha(0f);


        positiveButton.setOnClickListener(v -> {
            positiveButton.setEnabled(false);
            neutralButton.setEnabled(true);
            negativeButton.setEnabled(true);
            posiviveBackground.setVisibility(ImageView.VISIBLE);
            neutralBackground.setVisibility(ImageView.INVISIBLE);
            negativeBackground.setVisibility(ImageView.INVISIBLE);
            //((ConstraintLayout)view.findViewById(R.id.text_layout)).setVisibility(View.VISIBLE);
            view.findViewById(R.id.text_layout).animate().alpha(1f).setDuration(250);
            posiviveBackground.animate().alpha(1f).setDuration(250);
            neutralBackground.animate().alpha(1f).setDuration(250);
            negativeBackground.animate().alpha(1f).setDuration(250);

            editText.setText("Ich finde den Entwurf gut, weil\n");
            editText.setSelection(editText.getText().length());
            editText.requestFocus();
        });

        neutralButton.setOnClickListener(v -> {
            positiveButton.setEnabled(true);
            neutralButton.setEnabled(false);
            negativeButton.setEnabled(true);
            posiviveBackground.setVisibility(ImageView.INVISIBLE);
            neutralBackground.setVisibility(ImageView.VISIBLE);
            negativeBackground.setVisibility(ImageView.INVISIBLE);
            view.findViewById(R.id.text_layout).animate().alpha(1f).setDuration(250);

            posiviveBackground.animate().alpha(1f).setDuration(250);
            neutralBackground.animate().alpha(1f).setDuration(250);
            negativeBackground.animate().alpha(1f).setDuration(250);

            editText.setText("Ich bin dem Entwurf gegenüber neutral.\n");
            editText.setSelection(editText.getText().length());
            editText.requestFocus();
        });

        negativeButton.setOnClickListener(v -> {
            positiveButton.setEnabled(true);
            neutralButton.setEnabled(true);
            negativeButton.setEnabled(false);
            posiviveBackground.setVisibility(ImageView.INVISIBLE);
            neutralBackground.setVisibility(ImageView.INVISIBLE);
            negativeBackground.setVisibility(ImageView.VISIBLE);
            view.findViewById(R.id.text_layout).animate().alpha(1f).setDuration(250);

            posiviveBackground.animate().alpha(1f).setDuration(250);
            neutralBackground.animate().alpha(1f).setDuration(250);
            negativeBackground.animate().alpha(1f).setDuration(250);

            editText.setText("Ich finde den Entwurf nicht gut, weil ");
            editText.setSelection(editText.getText().length());
            editText.requestFocus();
        });

        voteButton.setOnClickListener(v -> listener.onCloseVoting());

        positiveButton.setEnabled(true);
        neutralButton.setEnabled(true);
        negativeButton.setEnabled(true);
        ((ImageView)view.findViewById(R.id.positiveBackground)).setVisibility(ImageView.INVISIBLE);
        ((ImageView)view.findViewById(R.id.neutralBackground)).setVisibility(ImageView.INVISIBLE);
        ((ImageView)view.findViewById(R.id.negativeBackground)).setVisibility(ImageView.INVISIBLE);

        return view;
    }


    public interface VotingFragListener
    {
        void onCloseVoting();
    }
}
