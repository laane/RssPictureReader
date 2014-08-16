package com.jujujuijk.android.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.util.Pair;

import com.jujujuijk.android.rssreader.ApplicationContextProvider;

import java.util.ArrayList;
import java.util.List;

public class MyDatabase extends SQLiteOpenHelper {

    // database version
    private static final int DATABASE_VERSION = 2;

    // the name of the database and the table(s)
    private static String DB_NAME = "db_urls";
    private static String TABLE_NAME = "name_url";

    // For singleton
    private static MyDatabase mInstance = null;
    private final List<Feed> mFeedList = new ArrayList<Feed>();

    // Database watchers to notify when data changed
    private final List<IDatabaseWatcher> mWatcherList = new ArrayList<IDatabaseWatcher>();

    private MyDatabase() {
        super(ApplicationContextProvider.getContext(), DB_NAME, null,
                DATABASE_VERSION);
    }

    public static synchronized MyDatabase getInstance() {
        if (mInstance == null) {
            mInstance = new MyDatabase();
        }
        return mInstance;
    }

    public void follow(IDatabaseWatcher watcher) {
        mWatcherList.add(watcher);
    }

    public void unFollow(IDatabaseWatcher watcher) {
        mWatcherList.remove(watcher);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME +
                " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "url TEXT NOT NULL, " +
                "notify INTEGER DEFAULT 0, " +
                "picture_seen TEXT DEFAULT '', " +
                "picture_last TEXT DEFAULT ''" +
                ");";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            try {
                // Backup old pairs name/url to push them after rebuilding the table
                List<Pair<String, String>> backupList = new ArrayList<Pair<String, String>>();
                Cursor c = db.rawQuery("SELECT * FROM name_url", null);
                if (c != null) {
                    int indexName = c.getColumnIndex("name");
                    int indexUrl = c.getColumnIndex("url");
                    while (c.moveToNext()) {
                        backupList.add(new Pair<String, String>(c.getString(indexName), c.getString(indexUrl)));
                    }
                    c.close();
                }

                // Drop table
                final String DROP_TABLE = "DROP TABLE IF EXISTS '" + TABLE_NAME + "'";
                db.execSQL(DROP_TABLE);

                // Create new table
                onCreate(db);

                // Fill the new table
                for (Pair<String, String> p : backupList) {
                    ContentValues values = new ContentValues();
                    values.put("name", p.first);
                    values.put("url", p.second);
                    db.insert("name_url", null, values);
                }
            } catch (SQLiteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * CRUD Operations
     */

    // Adding new feed
    public synchronized long addFeed(Feed feed) {
        SQLiteDatabase db = this.getWritableDatabase();
        if (db == null)
            return -1;

        ContentValues values = new ContentValues();
        values.put("name", feed.getName());
        values.put("url", feed.getUrl());
        values.put("notify", feed.getNotify());
        values.put("picture_seen", feed.getPictureSeen());
        values.put("picture_last", feed.getPictureLast());

        // Inserting Row
        long id = db.insert(TABLE_NAME, null, values);

        synchronizeFeedList();
        db.close();
        return id;
    }

    public synchronized int getFeedIdx(long id) {
        int ret = 0;
        for (Feed f : mFeedList) {
            if (f.getId() == id) {
                break;
            }
            ret++;
        }
        return ret;
    }

    // Getting single feed
    public synchronized Feed getFeed(long id) {
        Feed ret = null;
        for (Feed f : mFeedList) {
            if (f.getId() == id) {
                ret = f;
                break;
            }
        }
        return ret;
    }

    // Getting All feeds
    public synchronized List<Feed> getAllFeeds() {

        if (mFeedList.size() != 0)
            return mFeedList;

        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_NAME;

        SQLiteDatabase db = this.getReadableDatabase();

        if (db == null)
            return mFeedList;

        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Feed contact = new Feed();

                contact.setId(Integer.parseInt(cursor.getString(0)));
                contact.setName(cursor.getString(1));
                contact.setUrl(cursor.getString(2));

                contact.setNotify(Integer.parseInt(cursor.getString(3)));
                contact.setPictureSeen(cursor.getString(4));
                contact.setPictureLast(cursor.getString(5));

                // Adding feed to list
                mFeedList.add(contact);
            } while (cursor.moveToNext());
        }

        cursor.close();

        db.close();
        return mFeedList;
    }

    public synchronized  int updateFeed(Feed feed) { return updateFeed(feed, true); }
    // Updating single feed
    private synchronized int updateFeed(Feed feed, boolean synchronize) {
        SQLiteDatabase db = this.getWritableDatabase();

        if (db == null)
            return -1;

        ContentValues values = new ContentValues();
        values.put("name", feed.getName());
        values.put("url", feed.getUrl());
        values.put("notify", feed.getNotify());
        values.put("picture_seen", feed.getPictureSeen());
        values.put("picture_last", feed.getPictureLast());

        // updating row
        int ret = db.update(TABLE_NAME, values, "id" + " = ?",
                new String[] { String.valueOf(feed.getId()) });

        db.close();
        if (synchronize)
            synchronizeFeedList();
        return ret;
    }

    // Deleting single feed
    public synchronized void deleteFeed(Feed feed) {
        SQLiteDatabase db = this.getWritableDatabase();

        if (db == null)
            return;

        db.delete(TABLE_NAME, "id = ?",
                new String[] { String.valueOf(feed.getId()) });
        db.close();
        synchronizeFeedList();
    }

    private synchronized void  synchronizeFeedList() {
        mFeedList.clear();
        getAllFeeds();
        for (IDatabaseWatcher i : mWatcherList)
            i.notifyFeedListChanged();
    }

    public void updateFeeds(List<Feed> feedList) {
        for (Feed f : feedList)
            updateFeed(f, false);
        synchronizeFeedList();
    }

    public interface IDatabaseWatcher {
        abstract void notifyFeedListChanged();
    }

}
