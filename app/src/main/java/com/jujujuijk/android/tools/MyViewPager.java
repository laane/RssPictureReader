package com.jujujuijk.android.tools;

import com.jujujuijk.android.fragment.ShowFeedFragment;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

public class MyViewPager extends ViewPager {

	public int m_lastLoadedId = -1;
	private ShowFeedFragment m_parent = null;
	private PageChangeListener m_listener = new PageChangeListener();

	public MyViewPager(Context context) {
		super(context);
	}

	public MyViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setParent(ShowFeedFragment parent) {
		m_parent = parent;
	}

	public void setOnPageChangeListener() {
		super.setOnPageChangeListener(m_listener);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

//		int currentIdx = getCurrentItem();
//		MyPagerAdapter adapt = (MyPagerAdapter) getAdapter();
//
//		try {
//			ImageFragment current = (ImageFragment) adapt.getItem(currentIdx);
//			if (current.isZoomed())
//				return true;
//		} catch (ClassCastException e) {
//			e.printStackTrace();
//		}

		return super.onTouchEvent(event);
	}

	private class PageChangeListener implements OnPageChangeListener {

		@Override
		public void onPageSelected(int pos) {
			if (m_lastLoadedId == -1
					|| m_lastLoadedId + 1 < ShowFeedFragment.NB_BASE_IMAGES
					|| m_lastLoadedId >= ShowFeedFragment.NB_MAX_IMAGES)
				return;

			if (pos + ShowFeedFragment.NB_BEFORE_CONTINUE_LOAD >= ShowFeedFragment.NB_BASE_IMAGES) {
				int id = m_lastLoadedId;
				m_lastLoadedId = -1;
				if (m_parent != null)
					m_parent.launchLoading(id + 1);
				else
					Log.v("ERROR", "myviewpager: no parent set");
			}
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}
	}
}
