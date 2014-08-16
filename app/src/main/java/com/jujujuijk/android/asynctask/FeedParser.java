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

import javax.xml.parsers.DocumentBuilderFactory;

public class FeedParser extends
        AsyncTask<Integer, Void, List<Bundle>> {

    protected RssParserCallBack m_parent = null;

    protected static final int NB_MAX_LOAD = ShowFeedFragment.NB_MAX_IMAGES;

    protected Feed mFeed;
    protected URL m_url;

    private Document m_doc;
    private Element m_root;
    private NodeList m_nodes;

    public FeedParser(RssParserCallBack parent, Feed feed) {
        m_parent = parent;
        mFeed = feed;
        try {
            m_url = new URL(mFeed.getUrl());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected List<Bundle> doInBackground(Integer... nbMax) {
        List<Bundle> ret = null;

        if (m_url == null)
            return ret;

        try {
            m_doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(m_url.openStream());
            m_root = m_doc.getDocumentElement();
            m_nodes = m_root.getElementsByTagName("item");

            for (int i = 0; i < m_nodes.getLength() && i < NB_MAX_LOAD && i < nbMax[0]; ++i) {
                try {
                    Element elem = (Element) m_nodes.item(i);
                    NodeList desc = elem.getElementsByTagName("description");
                    NodeList date = elem.getElementsByTagName("pubDate");
                    NodeList title = elem.getElementsByTagName("title");

                    if (desc.getLength() != 1 || date.getLength() != 1
                            || title.getLength() != 1)
                        continue;

                    if (ret == null)
                        ret = new ArrayList<Bundle>();

                    String strDesc = desc.item(0).getTextContent();
                    try {
                        strDesc = (String) strDesc.substring(strDesc.indexOf("<img"));
                        strDesc = (String) strDesc.subSequence(
                                strDesc.indexOf("http://"),
                                strDesc.indexOf("\"", strDesc.indexOf("http://") + 10));
                    } catch (IndexOutOfBoundsException e) { // Example: bonsoir mademoiselle: Image is not in <description> but in <content:encoded>
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
                } // Attending here for <item>'s that do not contain images
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
        if (m_parent instanceof Fragment && (((Fragment)m_parent).isRemoving()
                || ((Fragment)m_parent).isDetached() || ((Fragment)m_parent).getActivity() == null))
            return;
        m_parent.onRssParserPostExecute(result, mFeed);
    }

    public interface RssParserCallBack {
        abstract void onRssParserPostExecute(List<Bundle> images, Feed feed);
    }
}
