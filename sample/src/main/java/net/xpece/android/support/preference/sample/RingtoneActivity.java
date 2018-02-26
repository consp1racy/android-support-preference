package net.xpece.android.support.preference.sample;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.XpPreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import net.xpece.android.support.preference.RingtoneManagerCompat;

/**
 * Created by Eugen on 25.02.2018.
 */

public class RingtoneActivity extends AppCompatActivity {

    private Button buttonPlayDefaultNotificationSound;
    private Button buttonPlayPickedNotificationSound;
    private Button buttonPlayValidNotificationSound;
    private TextView textRingtoneTitle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ringtone);

        textRingtoneTitle = findViewById(R.id.textRingtoneTitle);

        buttonPlayDefaultNotificationSound = findViewById(R.id.buttonPlayDefaultNotificationSound);
        buttonPlayDefaultNotificationSound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Context context = v.getContext();
                Uri uri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_NOTIFICATION);
                Ringtone ringtone = RingtoneManager.getRingtone(context, uri);
                ringtone.play();
                textRingtoneTitle.setText(ringtone.getTitle(context));
            }
        });

        buttonPlayPickedNotificationSound = findViewById(R.id.buttonPlayPickedNotificationSound);
        buttonPlayPickedNotificationSound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Context context = v.getContext();
                final String value = XpPreferenceManager.getDefaultSharedPreferences(context).getString("notifications_new_message_ringtone", null);
                Uri uri;
                if (value != null) {
                    uri = Uri.parse(value);
                } else {
                    uri = Settings.System.DEFAULT_NOTIFICATION_URI;
                }
                Ringtone ringtone = RingtoneManager.getRingtone(context, uri);
                ringtone.play();
                textRingtoneTitle.setText(ringtone.getTitle(context));
            }
        });

        buttonPlayValidNotificationSound = findViewById(R.id.buttonPlayValidNotificationSound);
        buttonPlayValidNotificationSound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Context context = v.getContext();
                RingtoneManagerCompat rm = new RingtoneManagerCompat(context);
                rm.setType(RingtoneManager.TYPE_NOTIFICATION);
                final Uri uri = rm.getValidRingtoneUri();
                Ringtone ringtone = RingtoneManager.getRingtone(context, uri);
                ringtone.play();
                textRingtoneTitle.setText(ringtone.getTitle(context));
            }
        });
    }
}
