package com.bywrights.screentimeout.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.bywrights.screentimeout.Controller;
import com.bywrights.screentimeout.R;
import com.bywrights.screentimeout.fragment.AppDetailFragment;
import com.bywrights.screentimeout.model.App;
import com.bywrights.screentimeout.model.AppCollection;
import com.bywrights.screentimeout.model.Model;

/**
 * Created by chris on 2013-07-29.
 */
public class AppDetailActivity extends BaseActivity {

    public static final class State extends RetainedState {
        private String
            app_uid_;

        public void save( BaseActivity activity ) {
            AppDetailActivity a = (AppDetailActivity)activity;
            app_uid_ = a.app_uid_;
        }

        public void load( BaseActivity activity ) {
            AppDetailActivity a = (AppDetailActivity)activity;
            a.app_uid_ = app_uid_;
        }
    }

    private AppCollection
        apps_;
    private String
        app_uid_;
    private App
        app_;
    private AppDetailFragment.Pager
        pager_;

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        save_state( STATE );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.app_detail_activity);

        if (!load_state( STATE )) {
            app_uid_ = Controller.app_uid(getIntent());
            create_state( new State(), STATE );
        }

        apps_ = Model.sharedInstance().ordered_apps();
        pager_ = new AppDetailFragment.Pager( getSupportFragmentManager(), apps_ );
        app_ = apps_.app( app_uid_ );
    }

    @Override
    protected void onResume() {
        super.onResume();

        update_app();
    }

    private final void update_app() {

        setTitle( app_.name() );

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById( R.id.content );
        if (fragment instanceof AppDetailFragment && ((AppDetailFragment)fragment).app() == app_) {
            // do nothing, the fragment will update itself
        } else {
            if (app_ != null) {
                AppDetailFragment af = (AppDetailFragment)pager_.getItem( apps_.position( app_ ) );
                FragmentTransaction ft = fm.beginTransaction();
                //if (fragment != null) // only animate if there was already something there
                //    ft.setCustomAnimations( forward ? R.anim.slide_in_from_right : R.anim.slide_in_from_left, forward ? R.anim.slide_out_to_left : R.anim.slide_out_to_right );
                ft.replace( R.id.content, af );
                ft.commitAllowingStateLoss();
            }
        }
    }
}
