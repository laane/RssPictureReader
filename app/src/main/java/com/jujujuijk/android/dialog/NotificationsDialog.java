package com.jujujuijk.android.dialog;

import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;

import com.jujujuijk.android.rssreader.MainActivity;
import com.jujujuijk.android.rssreader.R;
import com.jujujuijk.android.database.Feed;
import com.jujujuijk.android.database.MyDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Claudy Focan on 12/07/13.
 */
public class NotificationsDialog extends Dialog {

    MainActivity mParent = null;
    ListView mFeedListView = null;
    List<Feed> mFeedList = null;

    public NotificationsDialog(MainActivity context) {
        super(context);
        mParent = context;
        init();
    }

    private void init() {
        this.setContentView(R.layout.notifications);
        this.setTitle(getContext().getResources().getString(R.string.notifications));

        mFeedList = new ArrayList<Feed>(mParent.getFeedList());

        mFeedListView = (ListView) findViewById(R.id.notif_feed_list);
        mFeedListView.setAdapter(new NotificationsAdapter());
    }

    @Override
    public void dismiss() {
        // Save into db new feeds to check
        synchronized (mFeedList) {
            for (Feed feed : mFeedList) {
                MyDatabase.getInstance().updateFeed(feed);
            }
        }

        // call a method to update current star
        super.dismiss();
    }

    public class NotificationsAdapter extends BaseAdapter {

        public NotificationsAdapter() {
        }

        @Override
        public int getCount() {
            return mFeedList.size();
        }

        @Override
        public String getItem(int i) {
            return mFeedList.get(i).getName();
        }

        @Override
        public long getItemId(int i) {
            return mFeedList.get(i).getId();
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            Feed feed = mFeedList.get(i);

            // set xml layout
            LayoutInflater inflater = mParent.getLayoutInflater();
            View row = inflater.inflate(R.layout.notification_row, null, true);

            CheckBox cb = (CheckBox) row.findViewById(R.id.notif_feed_checkbox);
            // WARNING, setting here the feed's idx INSIDE mFeedList, not its own id in db
            cb.setId(i);
            cb.setText(feed.getName());
            cb.setChecked((feed.getNotify() & Feed.Notify.NOTIF) != 0);

            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    int idx = compoundButton.getId(); // RIGHT, Combobox-ID == index in mFeedList

                    mFeedList.get(idx).setNotify(b ?
                            mFeedList.get(idx).getNotify() | Feed.Notify.NOTIF :
                            mFeedList.get(idx).getNotify() & ~Feed.Notify.NOTIF);
                }
            });

            return row;
        }
    }

}
