package com.bywrights.screentimeout.fragment;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.bywrights.screentimeout.Controller;
import com.bywrights.screentimeout.R;
import com.bywrights.screentimeout.activity.AppListActivity;
import com.bywrights.screentimeout.controls.AppList;
import com.bywrights.screentimeout.model.App;
import com.bywrights.screentimeout.model.AppCollection;
import com.bywrights.screentimeout.model.Model;

/**
 * Created by chris on 13-07-28.
 */
public class AppListFragment extends BaseFragment {

    private AppCollection
        apps_;
    private AppList
        list_;
    private App
        active_app_;
    private int
        list_item_index_,
        list_item_offset_;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        list_ = (AppList)inflater.inflate( R.layout.app_list_fragment, container, false );
        if (list_ != null) {

            list_.setOnItemClickListener( new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                    Object tag = view.getTag();
                    if (tag instanceof App) {
                        list_.setSelection( position );
                        ((AppListActivity)getActivity()).show_app((App) tag);
                    }
                }
            } );

//            list_.setLongClickable(true);
//            list_.setOnItemLongClickListener(new OnItemLongClickListener() {
//
//                @Override
//                public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
//                    Object tag = view.getTag();
//                    if (tag instanceof App) {
//                        list_.setSelection( position );
//                        app_context(view, (App) tag);
//                    }
//                    return true;
//                }
//            });

            if (apps_ == null) {
                apps_ = Model.sharedInstance().ordered_apps();
            }
            list_.apps(apps_);

            FragmentManager fm = getFragmentManager();
            AppDetailFragment rhs_fragment = (AppDetailFragment)fm.findFragmentById( R.id.rhs_fragment );
            if (rhs_fragment != null) {
                list_.set_adapter_selection(1, true);
            }
        }
        return list_;
    }

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );

        if (savedInstanceState == null) {
            String uid = Controller.app_uid(getActivity().getIntent());
            apps_ = Model.sharedInstance().ordered_apps();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        list_.setSelectionFromTop( list_item_index_, list_item_offset_ );
    }

    @Override
    public void onPause() {
        super.onPause();

        list_item_index_ = list_.getSelectedItemPosition();
        if (list_item_index_ == -1) {
            list_item_index_ = list_.getFirstVisiblePosition();
        }
        View v = list_.getChildAt(0);
        list_item_offset_ = (v == null) ? 0 : v.getTop();
    }
}
