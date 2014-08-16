package com.jujujuijk.android.fragment;

import com.jujujuijk.android.rssreader.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

public class ErrorFragment extends Fragment {
	
	ShowFeedFragment m_parent = null;
	
	public ErrorFragment() {
	}

	public ErrorFragment(ShowFeedFragment parent) {
		m_parent = parent;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.image_error, container, false);
		ImageButton b = (ImageButton) v.findViewById(R.id.button_reload_feed);
		
		if (m_parent != null) {
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				m_parent.launchParser();
			}
		});
		} else {
			TextView msg = (TextView) v.findViewById(R.id.error_msg);
			msg.setText(R.string.load_error);
			b.setVisibility(View.INVISIBLE);
		}
		
		return v;
	}
}
