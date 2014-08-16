package com.jujujuijk.android.database;

import android.R;

import java.util.HashMap;

/**
 * Created by jujujuijk on 7/17/13.
 */
public class Feed extends HashMap<String, String> {

    private long mId;
    private String mName;
    private String mUrl;
    private Integer mNotify = 0;
    private String mPictureSeen = "";
    private String mPictureLast = "";

    public Feed () {

    }

    public Feed (String name, String url) {
        mName = name;
        mUrl = url;
    }

    public Feed (long id, String name, String url) {
        mId = id;
        mName = name;
        mUrl = url;
    }

    public Feed (long id, String name, String url, int notify, String pictureSeen, String pictureLast) {
        mId = id;
        mName = name;
        mUrl = url;
        mNotify = notify;
        mPictureSeen = pictureSeen;
        mPictureLast = pictureLast;
    }

    @Override
    public String get(Object k) {
        String key = (String) k;
        if (key.equals("name"))
            return mName;
        else if (key.equals("notifystar")) {
            int res = 0;
            if ((mNotify & Notify.NOTIF) != 0) {
                if (!mPictureLast.equals(mPictureSeen))
                    res = R.drawable.star_big_on;
                else
                    res = R.drawable.star_big_off;
            }
            return Integer.toString(res);
        }
        return null;
    }

    /**
     * Getters/Setters
     */

    public long getId() {
        return mId;
    }

    public void setId(long mId) {
        this.mId = mId;
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String mUrl) {
        this.mUrl = mUrl;
    }

    public Integer getNotify() {
        return mNotify;
    }

    public void setNotify(Integer mNotify) {
        this.mNotify = mNotify;
    }

    public String getPictureSeen() {
        return mPictureSeen;
    }

    public void setPictureSeen(String mPictureSeen) {
        this.mPictureSeen = mPictureSeen;
    }

    public String getPictureLast() {
        return mPictureLast;
    }

    public void setPictureLast(String mPictureLast) {
        this.mPictureLast = mPictureLast;
    }

    static public abstract class Notify {
        static public int NOTIF = (1 << 0);
        static public int LIVE_WALLPAPER = (1 << 1);
    }
}
