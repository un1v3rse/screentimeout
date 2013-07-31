package com.bywrights.screentimeout.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;

import com.bywrights.screentimeout.Controller;
import com.bywrights.screentimeout.activity.BaseActivity;
import com.bywrights.screentimeout.dialog.BaseDialogFragment;
import com.bywrights.screentimeout.event.UIEvent;

/**
 * Created by chris on 13-07-28.
 */
public class BaseFragment extends Fragment implements UIEvent.Handler {

    private final String
        TAG = getClass().getCanonicalName();

    public void startActivity( Intent intent ) {
        ((BaseActivity)getActivity()).startActivity( intent );
    }

    public void startActivityForResult( Intent intent, BaseActivity.ActivityResultListener listener ) {
        ((BaseActivity)getActivity()).startActivityForResult( intent, listener );
    }

    public android.view.MenuInflater getMenuInflater() {
        return ((BaseActivity)getActivity()).getMenuInflater();
    }

    protected boolean child_activity_up() {
        return ((BaseActivity)getActivity()).child_activity_up();
    }

    void show_progress( Dialog dialog ) { show_dialog( dialog ); }
    public void show_dialog( Dialog dialog ) {
        ((BaseActivity)getActivity()).show_dialog( dialog );
    }

    public void show_dialog( BaseDialogFragment fragment ) {
        ((BaseActivity)getActivity()).show_dialog( fragment );
    }

    public void hide_progress() { hide_dialog(); }
    public void hide_dialog() {
        ((BaseActivity)getActivity()).hide_dialog();
    }

    protected void allow_back( boolean allow ) {
        ((BaseActivity)getActivity()).allow_back( allow );
    }

    protected void show_error( int resId, boolean long_duration ) {
        Controller.show_error(getActivity(), resId, long_duration);
    }

    protected void show_error( CharSequence msg, boolean long_duration ) {
        Controller.show_error( getActivity(), msg, long_duration );
    }

    protected void show_message( int resId, boolean long_duration ) {
        Controller.show_message( getActivity(), resId, long_duration );
    }

    protected void show_message( CharSequence msg, boolean long_duration ) {
        Controller.show_message( getActivity(), msg, long_duration );
    }

    protected void show_keyboard( boolean show, View v ) {
        ((BaseActivity)getActivity()).show_keyboard( show, v );
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach( activity );

        if (activity instanceof BaseActivity) {
            ((BaseActivity)activity).register( this );
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        Log.d(TAG, "onSaveInstanceState");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        setRetainInstance(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        hide_dialog();
    }

    @Override
    public void onDetach() {
        super.onDetach();

        Activity activity = getActivity();
        if (activity instanceof BaseActivity) {
            ((BaseActivity)activity).unregister( this );
        }
    }

    public boolean handle_event( UIEvent event ) {
        return false;
    }

    public boolean reshow_dialog( Dialog dialog ) {
        return false;
    }
}
