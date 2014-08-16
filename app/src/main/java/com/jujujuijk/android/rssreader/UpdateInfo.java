package com.jujujuijk.android.rssreader;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.view.LayoutInflater;
import android.view.View;

public class UpdateInfo {

	public static final String PRIVATE_PREF = "rssreader";
	public static final String VERSION_KEY = "version_number";
    public static final String PICTURE_SAVE_LOCATION = "picture_save_location";

	private Activity m_parent = null;

	UpdateInfo(Activity parent) {
		m_parent = parent;
	}

	public void run() {

		if (m_parent == null)
			return;

		SharedPreferences sharedPref = m_parent.getSharedPreferences(
				PRIVATE_PREF, Context.MODE_PRIVATE);

		int currentVersionNumber = 0;
		int savedVersionNumber = sharedPref.getInt(VERSION_KEY, 0);

		try {
			PackageInfo pi = m_parent.getPackageManager().getPackageInfo(
					m_parent.getPackageName(), 0);
			currentVersionNumber = pi.versionCode;
		} catch (Exception e) {
		}

		if (currentVersionNumber > savedVersionNumber) {
			_showWhatsNewDialog();

			Editor editor = sharedPref.edit();

			editor.putInt(VERSION_KEY, currentVersionNumber);
			editor.commit();
		}
	}
	
	private void _showWhatsNewDialog() {
		
        LayoutInflater inflater = m_parent.getLayoutInflater();
 
        View view = inflater.inflate(R.layout.dialog_whatsnew, null);
 
        Builder builder = new Builder(m_parent);
 
        builder.setView(view).setTitle("Whats New")
        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
 
        builder.create().show();
    }
}
