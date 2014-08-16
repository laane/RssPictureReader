package com.jujujuijk.android.fragment;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jujujuijk.android.asynctask.ImageSaver;
import com.jujujuijk.android.asynctask.WallpaperSetter;
import com.jujujuijk.android.rssreader.R;
import com.jujujuijk.android.tools.PinchImageView;

@SuppressLint("ValidFragment")
public class ImageFragment extends Fragment {

    Bitmap mImage;
    PinchImageView mPinchImage;

    public ImageFragment() {
        mImage = null;
    }

    public ImageFragment(Bitmap image) {
        mImage = image;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle b = getArguments();

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.image_show, container, false);
        TextView date = (TextView) v.findViewById(R.id.date);
        TextView title = (TextView) v.findViewById(R.id.title);
        mPinchImage = (PinchImageView) v.findViewById(R.id.image);

        if (b == null)
            return v;

        if (b.containsKey("date")) {
            date.setText(b.getString("date"));
        } else {
            date.setVisibility(View.GONE);
        }

        if (b.containsKey("title") && !b.getString("title").equals("Photo")) {
            title.setText(b.getString("title"));
        } else {
            title.setVisibility(View.GONE);
        }

        mPinchImage.setImageBitmap(mImage);

        return v;
    }

    public boolean isZoomed() {
        return mPinchImage.isZoomed();
    }

    public boolean executeAction(Action e) {

        switch (e) {
            case SAVE: // Launch Asyntask to save image
                new ImageSaver(mImage).execute(getArguments());
                break;
            case SET_WALLPAPER: // Launch Asyntask to set as wallpaper
                new WallpaperSetter().execute(mImage);
                break;
            default:
                return false;
        }
        return true;
    }

    public enum Action {
        SAVE,
        SET_WALLPAPER
    }
}
