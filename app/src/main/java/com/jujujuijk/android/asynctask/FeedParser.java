package com.jujujuijk.android.asynctask;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.jujujuijk.android.database.Feed;
import com.jujujuijk.android.fragment.ShowFeedFragment;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;

public class FeedParser extends
        AsyncTask<Integer, Void, List<Bundle>> {

    protected RssParserCallBack m_parent = null;

    protected static final int NB_MAX_LOAD = ShowFeedFragment.NB_MAX_IMAGES;

    protected Feed mFeed;
    protected URL mUrl;

    private Document mDoc;
    private Element mRoot;
    private NodeList mNodes;

    public FeedParser(RssParserCallBack parent, Feed feed) {
        m_parent = parent;
        mFeed = feed;
        try {
            mUrl = new URL(mFeed.getUrl());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected List<Bundle> doInBackground(Integer... nbMax) {
        List<Bundle> ret = null;

        if (mUrl == null)
            return ret;

        try {
            mDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(mUrl.openStream());
            mRoot = mDoc.getDocumentElement();
            mNodes = mRoot.getElementsByTagName("item");

            for (int i = 0; i < mNodes.getLength() && i < NB_MAX_LOAD && i < nbMax[0]; ++i) {
                try {
                    Element elem = (Element) mNodes.item(i);
                    NodeList desc = elem.getElementsByTagName("description");
                    NodeList date = elem.getElementsByTagName("pubDate");
                    NodeList title = elem.getElementsByTagName("title");

                    if (desc.getLength() != 1 || date.getLength() != 1
                            || title.getLength() != 1)
                        continue;

                    if (ret == null)
                        ret = new ArrayList<Bundle>();

                    String strDesc = desc.item(0).getTextContent();

                    final Pattern ptrn = Pattern.compile("<img src=\"(.+?)\"/>");
                    final Matcher mtchr = ptrn.matcher(strDesc);
                    if (mtchr.find()) {
                        strDesc = strDesc.substring(mtchr.start(), mtchr.end()).replace("<img src=\"", "").replace("\"/>", "");
                    } else {
                        NodeList desc2 = elem.getElementsByTagName("content:encoded");
                        strDesc = desc2.item(0).getTextContent();
                        strDesc = (String) strDesc.substring(strDesc.indexOf("<img"));
                        strDesc = (String) strDesc.subSequence(
                                strDesc.indexOf("src=\"http://"),
                                strDesc.indexOf("\"", strDesc.indexOf("src=\"http://") + 10));
                        strDesc = strDesc.substring(strDesc.indexOf("http://"));
                    }

                    String strDate = date.item(0).getTextContent();
                    strDate = (String) strDate.subSequence(5, 16);

                    String strTitle = title.item(0).getTextContent();

                    Bundle b = new Bundle();
                    b.putString("feed", mFeed.getName());
                    b.putString("title", strTitle);
                    b.putString("date", strDate);
                    b.putString("url", strDesc);
                    b.putInt("id", i);

                    ret.add(b);
                } catch (Exception e) {
                    e.printStackTrace();
                } // Expecting here for <item>'s that do not contain images
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return ret;
    }

    /**
     * The system calls this to perform work in the UI thread and delivers the
     * result from doInBackground()
     */

    protected void onPostExecute(List<Bundle> result) {
        if (m_parent == null)
            return;
        if (m_parent instanceof Fragment && (((Fragment) m_parent).isRemoving()
                || ((Fragment) m_parent).isDetached() || ((Fragment) m_parent).getActivity() == null))
            return;
        m_parent.onRssParserPostExecute(result, mFeed);
    }

    public interface RssParserCallBack {
        abstract void onRssParserPostExecute(List<Bundle> images, Feed feed);
    }
}
