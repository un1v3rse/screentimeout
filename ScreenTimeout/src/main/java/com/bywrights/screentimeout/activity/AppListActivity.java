package com.bywrights.screentimeout.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.bywrights.screentimeout.Controller;
import com.bywrights.screentimeout.R;
import com.bywrights.screentimeout.event.UIEvent;
import com.bywrights.screentimeout.fragment.AppDetailFragment;
import com.bywrights.screentimeout.fragment.AppListFragment;
import com.bywrights.screentimeout.fragment.AppsProgressFragment;
import com.bywrights.screentimeout.model.App;
import com.bywrights.screentimeout.model.AppCollection;
import com.bywrights.screentimeout.model.Model;

/**
 * Created by chris on 13-07-25.
 */
public class AppListActivity extends BaseActivity {

    private static final String
        TAG = "AppsActivity";

    // if we are using a tablet, the view will be in master/detail, and rhs will contain the detail
    private View
        rhs_;

    private AppCollection
        apps_;
    private AppDetailFragment.Pager
        pager_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.app_list_activity);

        rhs_ = findViewById( R.id.rhs );
        apps_ = Model.sharedInstance().ordered_apps();
        if (rhs_ != null) {
            pager_ = new AppDetailFragment.Pager( getSupportFragmentManager(), apps_ );
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        reload();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void reload() {

        apps_ = Model.sharedInstance().ordered_apps();

        update_header();
        if (apps_.count() > 0) {
            show_apps();
        }
        else {
            show_progress();
        }
    }


    private void show_apps() {
        FragmentManager fm = getSupportFragmentManager();
        Fragment lhs_fragment = fm.findFragmentById( R.id.lhs_fragment );
        if (!(lhs_fragment instanceof AppListFragment)) {
            AppListFragment fragment = new AppListFragment();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace( R.id.lhs_fragment, fragment );
            ft.setTransition( FragmentTransaction.TRANSIT_FRAGMENT_FADE );

            if (rhs_ != null) {
                rhs_.setVisibility( View.VISIBLE );
                ft.replace( R.id.rhs_fragment, pager_.getItem( 0 ) );
            }

            ft.commitAllowingStateLoss();
        }
    }

    private void show_progress() {
        FragmentManager fm = getSupportFragmentManager();
        Fragment lhs_fragment = fm.findFragmentById( R.id.lhs_fragment );
        if (!(lhs_fragment instanceof AppsProgressFragment)) {
            if (rhs_ != null) {
                rhs_.setVisibility( View.GONE );
            }
            AppsProgressFragment fragment = new AppsProgressFragment();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.lhs_fragment, fragment);
            ft.setTransition( FragmentTransaction.TRANSIT_FRAGMENT_FADE );
            ft.commitAllowingStateLoss();
        }
    }

    public void show_app( App app ) {
        if (pager_ == null) {
            Controller.sharedInstance().show_app(this, app);
        } else {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace( R.id.rhs_fragment, pager_.getItem( apps_.position( app ) ) );
            ft.commitAllowingStateLoss();
        }
    }

    private void update_header() {
//        if (results_.had_response()) {
//            String duration = results_.search_duration();
//            this.getSupportActionBar().setTitle( duration == null
//                    ? Rx.fmt( R.string.fmt_x_messages_found, results_.messages_found() )
//                    : Rx.fmt( R.string.fmt_x_messages_found_ys, results_.messages_found(), duration ) );
//            this.getSupportActionBar().setSubtitle( Rx.fmt( R.string.fmt_last_search_x, DateUtil.friendly_datetime(this, results_.search_date(), false) ) );
//        } else {
//            TextView progress_search_using = (TextView)findViewById(R.id.progress_search_using);
//            if (progress_search_using != null) {
//                MAWASearchCriteria criteria = MAWASearchCriteria.find_or_new( results_.criteria_uid() );
//                progress_search_using.setText( criteria.description_text( this ) );
//            }
//        }
    }


    public boolean handle_event( UIEvent event ) {
        switch (event.type()) {
            case UIEvent.RELOAD:
                reload();
                break;
        }

        return super.handle_event( event );
    }

}