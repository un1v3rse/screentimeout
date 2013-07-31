package com.bywrights.screentimeout.controls;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bywrights.screentimeout.R;
import com.bywrights.screentimeout.model.App;

/**
 * Created by chris on 13-07-28.
 */
public class AppRow extends RelativeLayout {

    public AppRow(Context context, AttributeSet attrs) {
        super( context, attrs );
    }

    public void update( App app ) {

        setTag( app );

        ImageView icon = (ImageView)findViewById(R.id.icon);
        icon.setImageDrawable( app.icon() );

        TextView name = (TextView)findViewById(R.id.name);
        name.setText( app.name() );

        TextView package_name = (TextView)findViewById(R.id.package_name);
        package_name.setText( app.package_name() );
    }
}
