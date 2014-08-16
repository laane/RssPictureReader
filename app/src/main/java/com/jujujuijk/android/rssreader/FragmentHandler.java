package com.jujujuijk.android.rssreader;

import java.util.ArrayList;
import java.util.List;

import android.support.v4.app.Fragment;
import android.util.Log;

public class FragmentHandler {

    List<Fragment> m_list;

    public FragmentHandler() {
        m_list = new ArrayList<Fragment>();
    }

    public int size() {
        return m_list.size();
    }

    public Fragment get(int location) {
        return m_list.get(location);
    }

    public void add(Fragment f) {
        m_list.add(f);
    }

    public void insert(int pos, Fragment f) {
        m_list.add(pos, f);
    }

    public void delete(int pos) {
//		Log.v("FragmentPagerAdapter", "Removing item #" + pos);
        if (pos < m_list.size())
            m_list.remove(pos);
    }

    public int indexOf(Object o) {
        return m_list.indexOf(o);
    }

    public void clear() {
//		Log.v("debug", "JE CLEAR MES FRAGMENTS");
        m_list.clear();
    }
}
