package com.dghan.vomeo.UI;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;

import com.dghan.vomeo.R;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //Set up vibrate
        Vibrator vibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(2000);

        //Set up Noti
        Notification notification = new Notification.Builder(context)
                .setContentTitle("Time to study with VoMeO")
                .setContentText("You had set up your alarm")
                .setSmallIcon(R.mipmap.ic_launcher).build();
        NotificationManager manager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        notification.flags|= Notification.FLAG_AUTO_CANCEL;
        manager.notify(0,notification);

        //Set up Ringtone
        Uri noti = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        Ringtone ringtone = RingtoneManager.getRingtone(context, noti);

        ringtone.play();
    }
}
