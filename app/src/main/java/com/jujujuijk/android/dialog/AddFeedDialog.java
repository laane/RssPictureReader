package com.jujujuijk.android.dialog;

import android.app.Dialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;

import com.jujujuijk.android.rssreader.MainActivity;
import com.jujujuijk.android.rssreader.R;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Claudy Focan on 05/07/13.
 */
public class AddFeedDialog extends Dialog {

    MainActivity mParent;

    // Synchronized lists to get all suggestions and their url
    private ArrayList<String> mAutocompleteNameList;
    private ArrayList<String> mAutocompleteUrlList;
    private AddFeedTextWatcher mTextWatcher;

    private AutoCompleteTextView mAutoTextView;

    public AddFeedDialog(MainActivity parent) {
        super(parent);
        mParent = parent;
        mAutocompleteNameList = mParent.getAutocompleteNameList();
        mAutocompleteUrlList = mParent.getAutocompleteUrlList();
        mTextWatcher = new AddFeedTextWatcher();
        init();
    }

    private void init() {
        this.setContentView(R.layout.add_url);
        this.setTitle(mParent.getResources().getString(R.string.new_feed));

        mAutoTextView = (AutoCompleteTextView) findViewById(R.id.new_name);
        mAutoTextView.setAdapter(new ArrayAdapter<String>(mParent,
                android.R.layout.simple_dropdown_item_1line,
                mAutocompleteNameList));
        mAutoTextView.setOnItemClickListener(new AddFeedItemClickListener());

        Button confirmAddFeed = (Button) findViewById(R.id.button_confirm_add_item);
        confirmAddFeed.setOnClickListener(new AddFeedClickListener());

        Button browseFeed = (Button) findViewById(R.id.button_browse_autocomplete);
        browseFeed.setOnClickListener(new AddFeedClickListener());

        CheckBox tumblr = (CheckBox) findViewById(R.id.tumblr_checkbox);
        tumblr.setOnCheckedChangeListener(new AddFeedCheckedChangedListener());
    }

    private class AddFeedCheckedChangedListener implements CompoundButton.OnCheckedChangeListener {

        private String mOldUrlText = "";

        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            final EditText name = (EditText) findViewById(R.id.new_name);
            final EditText url = (EditText) findViewById(R.id.new_url);

            if (b) {
                mAutoTextView.setAdapter(null);
                mAutoTextView.addTextChangedListener(mTextWatcher);
                mOldUrlText = url.getText().toString();
                if (name.length() > 0)
                    url.setText("http://" + name.getText() + ".tumblr.com/rss");
            } else {
                mAutoTextView.setAdapter(new ArrayAdapter<String>(mParent,
                        android.R.layout.simple_dropdown_item_1line,
                        mAutocompleteNameList));
                mAutoTextView.removeTextChangedListener(mTextWatcher);
                url.setText(mOldUrlText);
            }
        }
    }

    private class AddFeedTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            final EditText name = (EditText) findViewById(R.id.new_name);
            final EditText url = (EditText) findViewById(R.id.new_url);

            url.setText("http://" + name.getText() + ".tumblr.com/rss");
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    }

    private class AddFeedItemClickListener implements AdapterView.OnItemClickListener {

        Dialog mCaller = null;

        public AddFeedItemClickListener() {}

        public AddFeedItemClickListener(Dialog caller) {
            mCaller = caller;
        }

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
            int item_idx = mAutocompleteNameList.indexOf(adapterView
                    .getItemAtPosition(pos));

            EditText name = (EditText) findViewById(R.id.new_name);
            EditText url = (EditText) findViewById(R.id.new_url);

            name.setText(mAutocompleteNameList.get(item_idx));
            url.setText(mAutocompleteUrlList.get(item_idx));

            InputMethodManager inputManager = (InputMethodManager) mParent.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(
                    mAutoTextView.getApplicationWindowToken(), 0);

            if (mCaller != null)
                mCaller.dismiss();

            mAutoTextView.dismissDropDown();
        }
    }

    private class AddFeedClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.button_confirm_add_item:
                    EditText newName = (EditText) findViewById(R.id.new_name);
                    EditText newUrl = (EditText) findViewById(R.id.new_url);

                    String newNameString = newName.getText().toString();
                    String newUrlString = newUrl.getText().toString();
                    mParent.addNewUrl(newNameString, newUrlString);
                    dismiss();

                    mParent.selectLastItem();
                    break;
                case R.id.button_browse_autocomplete:
                    BrowseFeedDialog browseDialog = new BrowseFeedDialog();
                    browseDialog.show();
                    break;
            }
        }

        private class BrowseFeedDialog extends Dialog {
            public BrowseFeedDialog() {
                super(mParent);
                Init();
            }

            private void Init() {
                this.setTitle(mParent.getResources().getString(R.string.feed_list));
                setContentView(R.layout.browse_feeds);
                ListView lv = (ListView) findViewById(R.id.list_feeds);
                ArrayList<String> SortedArray = new ArrayList<String>(mAutocompleteNameList);
                Collections.sort(SortedArray);
                lv.setAdapter(new ArrayAdapter<CharSequence>(mParent,
                        android.R.layout.simple_list_item_1, new ArrayList<CharSequence>(SortedArray)));
                lv.setOnItemClickListener(new AddFeedItemClickListener(this));
            }
        }
    }
}
