package com.guochuang.campusplayer.ui;

import android.app.Dialog;
import android.content.Context;
import android.support.v7.widget.SearchView;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;

import com.guochuang.campusplayer.R;

public class SearchDialog extends Dialog {
    private ListView listView;
    private SearchView searchView;
    public SearchDialog(Context context, int width, int height, View layout, int style) {
        super(context, style);
        setContentView(layout);

        listView = (ListView) findViewById(R.id.list_view);
        searchView = (SearchView) findViewById(R.id.search_view);

        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.gravity = Gravity.CENTER;
        window.setAttributes(params);
    }

    public ListView getListView() {
        return listView;
    }

    public SearchView getSearchView() {
        return searchView;
    }
}