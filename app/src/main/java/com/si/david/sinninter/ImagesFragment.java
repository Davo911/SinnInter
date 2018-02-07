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


public class ImagesFragment extends Fragment
{
    ImagesFragmentListener listener;

    public ImagesFragment()
    {
        // Required empty public constructor
    }

    public static ImagesFragment newInstance(ImagesFragmentListener listener)
    {
        ImagesFragment frag = new ImagesFragment();
        frag.listener = listener;

        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        ViewGroup view = (ViewGroup) inflater.inflate(
                R.layout.fragment_images, container, false);

        ((FloatingActionButton)view.findViewById(R.id.backButton))
                .setOnClickListener(v -> listener.onCloseImages());

        InputStream imageStream = getResources().openRawResource(R.raw.image_1);
        ((ImageView)view.findViewById(R.id.imageView1))
                .setImageBitmap(BitmapFactory.decodeStream(imageStream));

        imageStream = getResources().openRawResource(R.raw.image_2);
        ((ImageView)view.findViewById(R.id.imageView2))
                .setImageBitmap(BitmapFactory.decodeStream(imageStream));

        imageStream = getResources().openRawResource(R.raw.image_3);
        ((ImageView)view.findViewById(R.id.imageView3))
                .setImageBitmap(BitmapFactory.decodeStream(imageStream));

        imageStream = getResources().openRawResource(R.raw.image_4);
        ((ImageView)view.findViewById(R.id.imageView4))
                .setImageBitmap(BitmapFactory.decodeStream(imageStream));

        return view;
    }

    public interface ImagesFragmentListener
    {
        void onCloseImages();
    }
}

