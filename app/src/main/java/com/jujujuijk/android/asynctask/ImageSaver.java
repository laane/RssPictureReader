package com.jujujuijk.android.asynctask;

import com.jujujuijk.android.rssreader.ApplicationContextProvider;
import com.jujujuijk.android.rssreader.R;
import com.jujujuijk.android.rssreader.UpdateInfo;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;

public class ImageSaver extends AsyncTask<Bundle, Void, String> {

    public static final String DEFAULT_IMG_SAVE_DIR = Environment.getExternalStorageDirectory() + "/" +
            ApplicationContextProvider.getContext().getResources().getString(R.string.app_name);

    private Bitmap mImage;
    private Context mContext;

    Exception mLastError = null;

    public ImageSaver(Bitmap image) {
        mImage = image;
        mContext = ApplicationContextProvider.getContext();
    }

    @Override
    protected String doInBackground(Bundle... bundles) {
        if (bundles.length == 0)
            return null;

        Bundle b = bundles[0];
        String feed, date, url;
        int id;
        if (b == null) {
            feed = "Feed";
            date = "Date";
            url = null;
            id = 0;
        } else {
            date = b.getString("date") != null ? b.getString("date") : "Date";
            feed = b.getString("feed") != null ? b.getString("feed") : "Feed";
            url = b.getString("url") != null ? b.getString("url") : null;
            id = b.getInt("id");
        }

        try {
            // Retrieve current save location
            SharedPreferences sp = ApplicationContextProvider.getContext().getSharedPreferences(UpdateInfo.PRIVATE_PREF, Context.MODE_PRIVATE);
            String filedir = sp.getString(UpdateInfo.PICTURE_SAVE_LOCATION, ImageSaver.DEFAULT_IMG_SAVE_DIR);

            String filename = feed + " - " + date + (url != null ? url.substring(url.lastIndexOf(".")) : ".jpg");
            File file = new File(filedir, filename);

            if (!new File(filedir).isDirectory())
                new File(filedir).mkdirs();

            if (file.createNewFile() == false) { // add the ID of the picture to generate a unique name
                filename = feed + " - " + date + " (" + id + ")" + (url != null ? url.substring(url.lastIndexOf(".")) : ".jpg");
                file = new File(filedir, filename);
            }
            FileOutputStream out = new FileOutputStream(file);
            mImage.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            // Refreshes the Gallery to make the picture appear immediately
            MediaScannerConnection.scanFile(ApplicationContextProvider.getContext(), new String[] {file.getPath()}, null, null);

            return file.getPath();
        } catch (Exception e) {
            mLastError = e;
            return null;
        }
    }

    @Override
    protected void onPostExecute(String path) {
        if (path == null) // Error
            Toast.makeText(mContext,
                    mContext.getResources().getString(R.string.error) + " " + (mLastError != null ? mLastError.getMessage() : mLastError),
                    Toast.LENGTH_LONG).show();
        else
            Toast.makeText(mContext,
                    mContext.getResources().getString(R.string.message_saved) + " " + path,
                    Toast.LENGTH_LONG).show();
    }
}
