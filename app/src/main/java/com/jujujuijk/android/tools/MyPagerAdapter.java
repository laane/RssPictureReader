package com.jujujuijk.android.tools;

import com.jujujuijk.android.rssreader.FragmentHandler;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.ViewGroup;

public class MyPagerAdapter extends FragmentStatePagerAdapter {

	private FragmentHandler m_fh;

	public MyPagerAdapter(FragmentManager fm) {
		super(fm);
		m_fh = new FragmentHandler();
	}

	@Override
	public Fragment getItem(int pos) {
		return m_fh.get(pos);
	}

	@Override
	public int getCount() {
		return m_fh.size();
	}

	@Override
	public int getItemPosition(Object o) {
		int pos = m_fh.indexOf(o);

		if (pos == -1) {
			return POSITION_NONE;
		}
		return pos;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {

		FragmentManager manager = ((Fragment) object).getFragmentManager();
		FragmentTransaction trans = manager.beginTransaction();
		trans.remove((Fragment) object);
		trans.commit();
		Log.v("debug", "Destroy fragment #" + position);

		super.destroyItem(container, position, object);
	}

	public void add(Fragment f) {
		Log.v("list", "List add #" + m_fh.size() + " : " + f);
		m_fh.add(f);
	}

	public void insert(int pos, Fragment f) {
		m_fh.insert(pos, f);
	}

	public void delete(int pos) {
		m_fh.delete(pos);
	}

	public void clear() {
		m_fh.clear();
	}
}
