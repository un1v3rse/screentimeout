package com.bywrights.screentimeout.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.bywrights.screentimeout.Controller;
import com.bywrights.screentimeout.dialog.BaseDialogFragment;
import com.bywrights.screentimeout.event.UIEvent;
import com.bywrights.screentimeout.fragment.BaseFragment;

import java.lang.ref.WeakReference;
import java.util.Vector;

/**
 * Created by chris on 13-07-28.
 */
public class BaseActivity extends android.support.v4.app.FragmentActivity {

    abstract public static class RetainedState extends BaseFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Tell the framework to try to keep this fragment around
            // during a configuration change. (this is actually set by default)
            setRetainInstance(true);
        }

        abstract public void load( BaseActivity activity );
        abstract public void save( BaseActivity activity );
    }

    public static class BaseState extends RetainedState {
        private ActivityResultListener
                activity_result_listener_;
        private Dialog
                dialog_;
        private boolean
                allow_back_;

        public void save( BaseActivity activity ) {
            activity_result_listener_ = activity.activity_result_listener_;
            dialog_ = activity.dialog_ == null ? null : activity.dialog_.get();
            allow_back_ = activity.allow_back_;
        }

        public void load( BaseActivity activity ) {
            activity.activity_result_listener_ = activity_result_listener_;
            if (dialog_ != null) {
                activity.dialog_ = new WeakReference<Dialog>( dialog_ );
            }
            activity.allow_back_ = allow_back_;
        }
    }

    // assume that we'll never start more than one activity at a time from our activity (a safe assumption?)
    private static final int
        LISTENED_REQUEST_CODE = 0x7FFF; // apparently we can only use the lower 16 bits for this

    protected static final String
        BASE_STATE = "base_state",
        DIALOG_FRAGMENT = "dialog",
        STATE = "state";

    private final String
        TAG = getClass().getCanonicalName();
    private final Vector<WeakReference<UIEvent.Handler>>
        fragments_ = new Vector<WeakReference<UIEvent.Handler>>();
    private boolean
        child_activity_up_;
    private WeakReference<Dialog>
        dialog_;
    private boolean
        allow_back_ = true;

    public boolean child_activity_up() { return child_activity_up_; }
    protected Dialog dialog() { return dialog_.get(); }

    public interface ActivityResultListener {
        public void onResultCode( int resultCode, Intent data );
    }
    private ActivityResultListener
            activity_result_listener_;

    public void register( UIEvent.Handler fragment ) {
        unregister( fragment );
        fragments_.add( new WeakReference<UIEvent.Handler>( fragment ) );
    }

    public void unregister( UIEvent.Handler fragment ) {
        for (int ii = fragments_.size(); ii-->0;) {
            UIEvent.Handler found = fragments_.get( ii ).get();
            if (found == fragment || found == null)
                fragments_.remove( ii );
        }
    }

    public void startActivity( Intent intent ) {
        if (!child_activity_up_) {
            child_activity_up_ = true;
            super.startActivity( intent );
        }
    }

    public void startActivityForResult( Intent intent, ActivityResultListener listener ) {

        if (!child_activity_up_) {
            child_activity_up_ = true;

            // paranoia
            if (activity_result_listener_ != null) {
                Log.e(TAG, "Activity trying to start more than one activity at a time...");
                return;
            }

            activity_result_listener_ = listener;
            startActivityForResult( intent, LISTENED_REQUEST_CODE );
        }
    }

    @Override
    public void onActivityResult( int requestCode, int resultCode, Intent data ) {
        if (requestCode == LISTENED_REQUEST_CODE) {
            if (activity_result_listener_ != null) {
                ActivityResultListener listener = activity_result_listener_;
                activity_result_listener_ = null;
                child_activity_up_ = false;
                listener.onResultCode( resultCode, data );
                return;
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    void show_progress( Dialog dialog ) { show_dialog( dialog ); }
    public synchronized void show_dialog( Dialog dialog ) {
        if (!child_activity_up_) {
            if (dialog == null) {
                dialog_ = null;
            } else {
                Dialog old = dialog_ == null ? null : dialog_.get();
                if (old != null) {
                    old.setOnDismissListener( null );
                }

                dialog_ = new WeakReference<Dialog>( dialog );
                // HACK: no way to figure out if the listener has been set, lame...
                dialog.setOnDismissListener( new OnDialogDismissListener() );
                child_activity_up_ = true;
                dialog.show();
            }
        }
    }

    public void hide_progress() { hide_dialog(); }
    public synchronized void hide_dialog() {
        Dialog dialog = dialog_ == null ? null : dialog_.get();
        if (dialog != null) {
            dialog.dismiss();
            child_activity_up_ = false;
        }
        dialog_ = null;
    }

    public void show_dialog( BaseDialogFragment fragment, String id ) {

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag( id );
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        fragment.show(ft, id);
    }

    public void show_dialog( BaseDialogFragment fragment ) {
        if (!child_activity_up_) {
            child_activity_up_ = true;
            fragment.setInternalDismissListener( new OnDialogFragmentDismissListener() );

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            Fragment prev = getSupportFragmentManager().findFragmentByTag( DIALOG_FRAGMENT );
            if (prev != null) {
                ft.remove(prev);
            }
            ft.addToBackStack(null);
            fragment.show(ft, DIALOG_FRAGMENT);
        }
    }

    protected void create_state( RetainedState state, String tag ) {
        getSupportFragmentManager().beginTransaction().add( state, tag ).commitAllowingStateLoss();
    }
    protected void save_state( String tag ) {
        RetainedState state = (RetainedState)getSupportFragmentManager().findFragmentByTag( tag );
        if (state != null)
            state.save( this );
    }
    protected boolean load_state( String tag ) {
        RetainedState state = (RetainedState)getSupportFragmentManager().findFragmentByTag( tag );
        if (state != null)
            state.load( this );
        return state != null;
    }

    public void allow_back( boolean allow ) {
        allow_back_ = allow;
        //getSupportActionBar().setDisplayHomeAsUpEnabled( allow_back_ );
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        Log.d(TAG, "onSaveInstanceState");
        save_state( BASE_STATE );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        Controller.sharedInstance().register( this );

        if (!load_state( BASE_STATE ))
            create_state( new BaseState(), BASE_STATE );

        allow_back( allow_back_ || !isTaskRoot() );

        if (!allow_back_)
            this.getWindow().setWindowAnimations( android.R.anim.fade_in );
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        Dialog dialog = dialog_ == null ? null : dialog_.get();
        child_activity_up_ = dialog_ != null;
        if (child_activity_up_ && dialog.getContext() != this) {
            dialog.dismiss();
            child_activity_up_ = false; // so show_dialog will work
            reshow_dialog( dialog );
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        hide_dialog();

        Controller.sharedInstance().unregister( this );
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                if (allow_back_)
                    onBackPressed();
                return true;

//            case R.id.about:
//                Controller.sharedInstance().show_about( this );
//                return true;
        }

        return super.onOptionsItemSelected( item );
    }

    public class OnDialogDismissListener implements DialogInterface.OnDismissListener {

        @Override
        public void onDismiss(DialogInterface dialog) {
            if (dialog_ != null && dialog_.get() == dialog) {
                child_activity_up_ = false;
                dialog_ = null;
            }
        }
    }

    public class OnDialogFragmentDismissListener implements DialogInterface.OnDismissListener {

        @Override
        public void onDismiss(DialogInterface dialog) {
            child_activity_up_ = false;
        }
    }

    public void show_error( int resId, boolean long_duration ) {
        Controller.show_error( this, resId, long_duration );
    }

    public void show_error( CharSequence msg, boolean long_duration ) {
        Controller.show_error( this, msg, long_duration );
    }

    public void show_message( int resId, boolean long_duration ) {
        Controller.show_message( this, resId, long_duration );
    }

    public void show_message( CharSequence msg, boolean long_duration ) {
        Controller.show_message( this, msg, long_duration );
    }

    public void show_keyboard( boolean show, View v) {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        if (show)
            imm.showSoftInput( v, 0 );
        else
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    public boolean handle_event( UIEvent event ) {
        // pass along to fragments
        for (WeakReference<UIEvent.Handler> ref: fragments_) {
            UIEvent.Handler handler = ref.get();
            if (handler != null)
                handler.handle_event( event );
        }
        return false;
    }

    protected boolean reshow_dialog( Dialog dialog ) {
        UIEvent event = new UIEvent( UIEvent.RESHOW_DIALOG, dialog );
        for (WeakReference<UIEvent.Handler> ref: fragments_) {
            UIEvent.Handler handler = ref.get();
            if (handler != null && handler.handle_event( event ))
                return true;
        }
        return false;
    }
}
