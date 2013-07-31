package com.bywrights.screentimeout.controls;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.bywrights.screentimeout.R;
import com.bywrights.screentimeout.model.App;
import com.bywrights.screentimeout.model.AppCollection;
import com.bywrights.screentimeout.model.Model;

/**
 * Created by chris on 13-07-28.
 */
public class AppList extends ListView {

    private final LayoutInflater
        layout_inflater_;
    private final Adapter
        adapter_;
    private AppCollection
        apps_;

    public AppList(Context context, AttributeSet attrs) {
        super( context, attrs );
        layout_inflater_ = LayoutInflater.from( context );
        apps_ = Model.sharedInstance().ordered_apps();
        adapter_ = new Adapter();
        setAdapter(adapter_);
        //setFadingEdgeLength(0);
    }

    public void apps( AppCollection apps ) {
        apps_ = apps;
        adapter_.notifyDataSetChanged();
    }

    public void notify_changed() {
        adapter_.notifyDataSetChanged();
    }

    public void setSelection( int position ) {
        super.setSelection( position );
        adapter_.set_selection( position );
    }

    public void set_adapter_selection( int position, boolean show_selection ) {
        adapter_.show_selection( show_selection );
        adapter_.set_selection( position );
    }

    private final class Adapter extends BaseAdapter {

        public final static int
            TYPE_ROW = 0,
            TYPE_COUNT = 1;

        private int
            selection_;
        private boolean
            show_selection_;

        private final void show_selection( boolean show_selection ) {
            show_selection_ = show_selection;
        }

        private final void set_selection( int position ) {
            selection_ = position;
            notifyDataSetChanged();
        }

        @Override
        public Object getItem(int position) {
            return apps_.app( position );
        }

        @Override
        public int getCount() {
            return apps_.count();
        }

        @Override
        public int getViewTypeCount() {
            return TYPE_COUNT;
        }

        @Override
        public int getItemViewType(int position) {
            return TYPE_ROW;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            Object item = getItem( position );
            App app = (App)item;
            AppRow row = (convertView instanceof AppRow)
                ? (AppRow)convertView
                : (AppRow)layout_inflater_.inflate( R.layout.app_row, parent, false );
            row.update( app );
            row.setBackgroundColor( position == selection_ && show_selection_ ? Color.GRAY: Color.TRANSPARENT );

            return row;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
    }
}
