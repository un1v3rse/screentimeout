package com.bywrights.screentimeout.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;

import com.bywrights.screentimeout.Controller;
import com.bywrights.screentimeout.R;
import com.bywrights.screentimeout.model.App;
import com.bywrights.screentimeout.model.AppCollection;
import com.bywrights.screentimeout.model.AppPreference;
import com.bywrights.screentimeout.model.Model;

import java.util.Hashtable;

/**
 * Created by chris on 13-07-28.
 */
public class AppDetailFragment extends BaseFragment {

    private static final String
        APP_NTH = "APP_NTH";

    private AppCollection
        apps_;
    private App
        app_;
    private CheckBox
        screen_timeout_enabled_,
        music_volume_enabled_;
    private SeekBar
        screen_timeout_,
        music_volume_;

    private static AppDetailFragment newInstance( int app_nth ) {
        AppDetailFragment f = new AppDetailFragment();

        Bundle args = new Bundle();
        args.putInt( APP_NTH, app_nth );
        f.setArguments(args);

        return f;
    }

    public AppDetailFragment() {
        super();
        setHasOptionsMenu( false );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate( R.layout.app_detail_fragment, container, false );
        if (v != null) {

            screen_timeout_enabled_ = (CheckBox)v.findViewById(R.id.screen_timeout_enabled);
            screen_timeout_enabled_.setOnCheckedChangeListener( new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    screen_timeout_.setEnabled( isChecked );
                }
            });

            screen_timeout_ = (SeekBar)v.findViewById(R.id.screen_timeout);

            music_volume_enabled_ = (CheckBox)v.findViewById(R.id.music_volume_enabled);
            music_volume_enabled_.setOnCheckedChangeListener( new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    music_volume_.setEnabled( isChecked );
                }
            });

            music_volume_ = (SeekBar)v.findViewById(R.id.music_volume);

        }
        return v;
    }

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);

        String uid = Controller.app_uid(getActivity().getIntent());
        apps_ = Model.sharedInstance().ordered_apps();
        int app_nth = getArguments() != null ? getArguments().getInt( APP_NTH ) : 0;
        app_ = apps_.app(app_nth);
    }

    @Override
    public void onResume() {
        super.onResume();

        update();
    }

    @Override
    public void onPause() {
        super.onPause();

        save();
    }


    public App app() { return app_; }

    private final void set_seekbar( SeekBar seek_bar, CheckBox check_box, String uid ) {

        Hashtable<String,AppPreference> prefs = app_.preferences();
        AppPreference pref = prefs.get( uid );

        boolean enabled = pref == null ? false : pref.enabled();
        check_box.setChecked( enabled );
        seek_bar.setEnabled( enabled );
        String value = null;
        if (pref == null) {
            pref = new AppPreference( uid );
            value = pref.current_system_value( getActivity() );
        } else {
            value = pref.value();
        }
        String min = pref.min( getActivity() );
        String max = pref.max( getActivity() );
        int value_int = Integer.parseInt( value == null ? min : value );
        int min_int = min == null ? 0 : Integer.parseInt( min );
        int max_int = max == null ? 10000 : Integer.parseInt( max );

        seek_bar.setMax( max_int - min_int );
        seek_bar.setProgress( value_int - min_int );
    }

    private final void save_seekbar( SeekBar seek_bar, CheckBox check_box, String uid ) {

        Hashtable<String,AppPreference> prefs = app_.preferences();
        AppPreference pref = prefs.get( uid );
        if (pref == null) {
            pref = new AppPreference( uid );
        }

        String min = pref.min( getActivity() );
        int value_int = seek_bar.getProgress();
        int min_int = min == null ? 0 : Integer.parseInt( min );
        boolean enabled = check_box.isChecked();

        pref.set_value( Integer.toString( value_int + min_int ) );
        pref.set_enabled( enabled );
        app_.update_preference( pref );
    }

    private final void update() {

        set_seekbar( screen_timeout_, screen_timeout_enabled_, AppPreference.UID_SCREEN_TIMEOUT );
        set_seekbar( music_volume_, music_volume_enabled_, AppPreference.UID_MUSIC_VOLUME );
    }


    private final void save() {

        save_seekbar( screen_timeout_, screen_timeout_enabled_, AppPreference.UID_SCREEN_TIMEOUT );
        save_seekbar( music_volume_, music_volume_enabled_, AppPreference.UID_MUSIC_VOLUME );

        app_.save();
    }

    public static final class Pager extends FragmentStatePagerAdapter {
        private final AppCollection
            apps_;

        public Pager(FragmentManager fm, AppCollection apps) {
            super(fm);
            apps_ = apps;
        }

        @Override
        public int getCount() {
            return apps_.count();
        }

        @Override
        public Fragment getItem(int position) {
            return AppDetailFragment.newInstance(position);
        }
    }
}
