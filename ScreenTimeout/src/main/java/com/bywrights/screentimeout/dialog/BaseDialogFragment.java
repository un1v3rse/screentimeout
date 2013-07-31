package com.bywrights.screentimeout.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;

import com.bywrights.screentimeout.Controller;
import com.bywrights.screentimeout.activity.BaseActivity;
import com.bywrights.screentimeout.event.UIEvent;

/**
 * Created by chris on 13-07-28.
 */
public class BaseDialogFragment extends DialogFragment implements UIEvent.Handler {

    private DialogInterface.OnDismissListener
            external_dismiss_listener_,
            internal_dismiss_listener_,
            uber_dismiss_listener_ = new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (external_dismiss_listener_ != null)
                        external_dismiss_listener_.onDismiss( dialog );
                    if (internal_dismiss_listener_ != null)
                        internal_dismiss_listener_.onDismiss( dialog );
                }
            };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach( activity );

        if (activity instanceof BaseActivity) {
            ((BaseActivity)activity).register( this );
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    // fix a bug in the Android compatibility library:
    //  http://stackoverflow.com/questions/8235080/fragments-dialogfragment-and-screen-rotation
    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance())
            getDialog().setOnDismissListener(null);
        super.onDestroyView();
    }

    @Override
    public void onStop() {
        super.onStop();

        uber_dismiss_listener_.onDismiss(getDialog());
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

    public void setDismissListener( DialogInterface.OnDismissListener listener ) {
        external_dismiss_listener_ = listener;
    }

    public void setInternalDismissListener( DialogInterface.OnDismissListener listener ) {
        internal_dismiss_listener_ = listener;
    }

    public void show_dialog( BaseDialogFragment fragment, String id ) {
        ((BaseActivity)getActivity()).show_dialog( fragment, id );
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
}
