package com.jujujuijk.android.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.media.MediaScannerConnection;
import android.media.audiofx.BassBoost;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jujujuijk.android.asynctask.ImageSaver;
import com.jujujuijk.android.rssreader.ApplicationContextProvider;
import com.jujujuijk.android.rssreader.MainActivity;
import com.jujujuijk.android.rssreader.R;
import com.jujujuijk.android.rssreader.UpdateInfo;
import com.jujujuijk.android.dialog.NotificationsDialog;

import net.bgreco.DirectoryPicker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jujujuijk on 12/25/13.
 */
public class SettingsFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try { // Create the default saving directory
            new File(ImageSaver.DEFAULT_IMG_SAVE_DIR).mkdirs();
        } catch (Exception e) { // NINJA
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.settings, container, false);
        List<View> titles = findViewWithTagRecursively((ViewGroup)v, getResources().getString(R.string.yes));
        for (View title : titles) {
            if (!(title instanceof TextView))
                continue;
            ((TextView)title).setText(((TextView)title).getText().toString().toUpperCase());
        }

        LinearLayout ll = (LinearLayout) v.findViewById(R.id.settings_edit_notif);
        ll.setOnClickListener(new SettingsOnClickListener());

        ll = (LinearLayout) v.findViewById(R.id.settings_pic_reset_location);
        ll.setOnClickListener(new SettingsOnClickListener());

        ll = (LinearLayout) v.findViewById(R.id.settings_pic_location);
        ll.setOnClickListener(new SettingsOnClickListener());

        // Retrieve current save location
        SharedPreferences sp = getActivity().getSharedPreferences(UpdateInfo.PRIVATE_PREF, Context.MODE_PRIVATE);
        String location = sp.getString(UpdateInfo.PICTURE_SAVE_LOCATION, ImageSaver.DEFAULT_IMG_SAVE_DIR);

        TextView tv = (TextView) v.findViewById(R.id.settings_pic_current_location);
        tv.setText(location);

        return v;
    }

    public void updateStoragePicLocation(String path) {
        // Retrieve current save location
        SharedPreferences sp = getActivity().getSharedPreferences(UpdateInfo.PRIVATE_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        editor.putString(UpdateInfo.PICTURE_SAVE_LOCATION, path);
        editor.commit();

        TextView tv = (TextView) getView().findViewById(R.id.settings_pic_current_location);
        tv.setText(path);
    }

    /**
     * Get all the views which matches the given Tag recursively
     * @param root parent view. for e.g. Layouts
     * @param tag tag to look for
     * @return List of views
     */
    public static List<View> findViewWithTagRecursively(ViewGroup root, Object tag){
        List<View> allViews = new ArrayList<View>();

        final int childCount = root.getChildCount();
        for(int i=0; i<childCount; i++){
            final View childView = root.getChildAt(i);

            if(childView instanceof ViewGroup){
                allViews.addAll(findViewWithTagRecursively((ViewGroup)childView, tag));
            }
            else{
                final Object tagView = childView.getTag();
                if(tagView != null && tagView.equals(tag))
                    allViews.add(childView);
            }
        }

        return allViews;
    }

    private class SettingsOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.settings_edit_notif:
                    if (!(getActivity() instanceof MainActivity))
                        break;
                    NotificationsDialog notifAlert = new NotificationsDialog((MainActivity)getActivity());

                    notifAlert.setCanceledOnTouchOutside(true);
                    notifAlert.show();
                    break;
                case R.id.settings_pic_location:
                    Intent intent = new Intent(getActivity(), DirectoryPicker.class);
                    getActivity().startActivityForResult(intent, DirectoryPicker.PICK_DIRECTORY);
                    break;
                case R.id.settings_pic_reset_location:
                    updateStoragePicLocation(ImageSaver.DEFAULT_IMG_SAVE_DIR);
                    break;
            }
        }
    }


}
