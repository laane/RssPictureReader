package com.jujujuijk.android.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.jujujuijk.android.rssreader.MainActivity;
import com.jujujuijk.android.rssreader.R;
import com.jujujuijk.android.database.Feed;
import com.jujujuijk.android.database.MyDatabase;
import com.jujujuijk.android.asynctask.ImageLoader;
import com.jujujuijk.android.asynctask.FeedParser;
import com.jujujuijk.android.tools.MyPagerAdapter;
import com.jujujuijk.android.tools.MyViewPager;

import java.util.List;

public class ShowFeedFragment
        extends Fragment
        implements ImageLoader.ImageLoaderCallback, FeedParser.RssParserCallBack {

    public static final int NB_BASE_IMAGES = 5;
    public static final int NB_BEFORE_CONTINUE_LOAD = 2;
    public static final int NB_MAX_IMAGES = 90;

    private MyViewPager myViewPager = null;
    public MyPagerAdapter mPagerAdapter = null;

    private List<Bundle> mImages = null;

    private Feed mFeed;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View viewer = (View) inflater.inflate(R.layout.image_pager, container,
                false);

        if (!(getActivity() instanceof MainActivity))
            return null;

        mFeed = ((MainActivity)getActivity()).getCurrentFeed();

        myViewPager = (MyViewPager) viewer.findViewById(R.id.fragment_container);

        // Creation de l'adapter qui s'occupera de l'affichage de la liste de fragments
        mPagerAdapter = new MyPagerAdapter(getActivity()
                .getSupportFragmentManager());

        myViewPager.setAdapter(mPagerAdapter);
        myViewPager.setParent(this);
        myViewPager.setOnPageChangeListener();

        launchParser();
        return viewer;
    }

    @Override
    public void onImageLoaderPostExecute(int id, Bitmap image) {
        if (image == null) {
            Toast.makeText(getActivity(), getString(R.string.load_error),
                    Toast.LENGTH_LONG).show();
            myViewPager.m_lastLoadedId = -1;

            mPagerAdapter.delete(mPagerAdapter.getCount() - 1);
            ErrorFragment newFragment = new ErrorFragment();
            mPagerAdapter.add(newFragment);

            try {
                mPagerAdapter.notifyDataSetChanged();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            return;
        }

        Bundle b = mImages.get(id);

        // b.putParcelable("image", image);

        mPagerAdapter.delete(mPagerAdapter.getCount() - 1);

        ImageFragment newFragment = new ImageFragment(image);
        newFragment.setArguments(b);
        mPagerAdapter.add(newFragment);

        if (id == 0) { // 1st image
            mFeed.setPictureLast(b.getString("url"));
            mFeed.setPictureSeen(b.getString("url"));
            MyDatabase.getInstance().updateFeed(mFeed);
        }

        try {
            mPagerAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        if (id < mImages.size() - 1 && id < NB_BASE_IMAGES - 1) {
            // continue to load until NB_BASE_IMAGES
            launchLoading(id + 1);
        } else if (myViewPager.getCurrentItem() + NB_BEFORE_CONTINUE_LOAD >= id
                && id + 1 < mImages.size()) {
            // we were waiting for the picture, load next
            launchLoading(id + 1);
        } else if (id + 1 < mImages.size()
                && mImages.get(id + 1).containsKey("image")) {
            // repopulating views, we already have this image, lets go on
            launchLoading(id + 1);
        } else {
            myViewPager.m_lastLoadedId = id;
        }

    }

    @Override
    public void onRssParserPostExecute(List<Bundle> images, Feed feed) {
        // Toast.makeText(getActivity(), "callback XML", 0).show();
        mPagerAdapter.clear();
        mPagerAdapter.add(new LoadingFragment());
        mImages = images;
        if (mImages != null && mImages.size() > 0) {
            launchLoading(0);
        } else if (mImages == null) {
            mPagerAdapter.delete(mPagerAdapter.getCount() - 1);
            ErrorFragment newFragment = new ErrorFragment(this);
            mPagerAdapter.add(newFragment);

            try {
                mPagerAdapter.notifyDataSetChanged();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        } else { // m_image.size() == 0
            Toast.makeText(getActivity(), "Unable to find items into XML feed",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPagerAdapter != null)
            mPagerAdapter.clear();
    }


    public void launchParser() {
        mPagerAdapter.clear();
        mPagerAdapter.add(new LoadingFragment());
        try {
            mPagerAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        new FeedParser(this, mFeed).execute(999);
    }

    public void launchLoading(int id) {
        if (id >= NB_MAX_IMAGES || id >= mImages.size())
            return;

        Bundle b = mImages.get(id);

        if (id != 0) {
            mPagerAdapter.add(new LoadingFragment());
            mPagerAdapter.notifyDataSetChanged();
        }
        myViewPager.m_lastLoadedId = -1;
        if (!b.containsKey("image"))
            new ImageLoader(this, id).execute(b.getString("url"));
        else
            onImageLoaderPostExecute(id, (Bitmap) b.getParcelable("image"));
    }

    public boolean dispatchAction(ImageFragment.Action action) {
        try {
            Fragment currentFragment = mPagerAdapter.getItem(myViewPager.getCurrentItem());

            if (!this.isHidden() && currentFragment instanceof ImageFragment) {
                ImageFragment image = (ImageFragment) currentFragment;

                if (action == ImageFragment.Action.SAVE
                        || action == ImageFragment.Action.SET_WALLPAPER) {
                    return image.executeAction(action);
                } else
                    Toast.makeText(getActivity(), getString(R.string.action_unavailable), Toast.LENGTH_SHORT).show();
            } else {
                // Error message
                Toast.makeText(getActivity(), getString(R.string.action_unavailable), Toast.LENGTH_SHORT).show();
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
