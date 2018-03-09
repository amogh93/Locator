package com.nascentech.locator.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.nascentech.locator.R;
import com.nascentech.locator.model.Chat;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Amogh on 22-01-2018.
 */

public class NotificationUtils
{
    private static final long[] DEFAULT_VIBRATE_PATTERN = {0, 250, 250, 250};
    private static final String DATA_CHANNEL_ID="channel_data_payload";
    private static final String NOTIFICATION_CHANNEL_NAME="push_notification";

    public static void showDataNotification(Context context,String phoneNumber,String iconUrl,int notificationId,PendingIntent pendingIntent, PendingIntent dismissPendingIntent)
    {
        String contentText="From "+ContactUtils.getContactName(phoneNumber,context);
        String description="Click to view my location";

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder notification = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel notificationChannel = new NotificationChannel(DATA_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, importance);
            notificationManager.createNotificationChannel(notificationChannel);
            notification = new NotificationCompat.Builder(context,notificationChannel.getId());
        }
        else
        {
            notification = new NotificationCompat.Builder(context);
        }

        Bitmap dataIcon=getBitmapFromURL(iconUrl);
        if(dataIcon!=null)
        {
            NotificationCompat.BigPictureStyle bigPictureStyle=new NotificationCompat.BigPictureStyle();
            bigPictureStyle.setBigContentTitle(description);
            bigPictureStyle.setSummaryText(description);
            bigPictureStyle.bigPicture(dataIcon);
            notification.setContentTitle("Location update")
                    .setContentText(contentText)
                    .setSmallIcon(R.drawable.ic_locator_notification)
                    .setColor(context.getResources().getColor(R.color.blue))
                    .setContentIntent(pendingIntent)
                    .setVibrate(DEFAULT_VIBRATE_PATTERN)
                    .setLights(0x0000ff,800,800)
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setStyle(bigPictureStyle)
                    .addAction(R.drawable.ic_dismiss, "Dismiss", dismissPendingIntent)
                    .setDeleteIntent(dismissPendingIntent)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        }
        else
        {
            notification.setContentTitle("Location update")
                    .setContentText(contentText)
                    .setSmallIcon(R.drawable.ic_locator_notification)
                    .setColor(context.getResources().getColor(R.color.blue))
                    .setContentIntent(pendingIntent)
                    .setVibrate(DEFAULT_VIBRATE_PATTERN)
                    .setLights(0x0000ff,800,800)
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(description))
                    .addAction(R.drawable.ic_dismiss, "Dismiss", dismissPendingIntent)
                    .setDeleteIntent(dismissPendingIntent)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            notification.setVisibility(Notification.VISIBILITY_PUBLIC);
        }

        notificationManager.notify(notificationId,notification.build());
    }

    public static void showBigTextDataNotification(Context context, Chat chat,String title,int notificationId,PendingIntent pendingIntent, PendingIntent dismissPendingIntent)
    {
        String sender=ContactUtils.getContactName(chat.getPhoneNumber(),context);
        String contentText="From "+sender;
        String description=chat.getMessage();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder notification = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel notificationChannel = new NotificationChannel(DATA_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, importance);
            notificationManager.createNotificationChannel(notificationChannel);
            notification = new NotificationCompat.Builder(context,notificationChannel.getId());
        }
        else
        {
            notification = new NotificationCompat.Builder(context);
        }

        notification.setContentTitle(title)
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_locator_notification)
                .setColor(context.getResources().getColor(R.color.blue))
                .setContentIntent(pendingIntent)
                .setVibrate(DEFAULT_VIBRATE_PATTERN)
                .setLights(0x0000ff,800,800)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M?description:sender+":"+description))
                .addAction(R.drawable.ic_dismiss, "Dismiss", dismissPendingIntent)
                .setDeleteIntent(dismissPendingIntent)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            notification.setVisibility(Notification.VISIBILITY_PUBLIC);
        }

        notificationManager.notify(notificationId,notification.build());
    }

    private static Bitmap getBitmapFromURL(String strURL)
    {
        try
        {
            URL url = new URL(strURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }
}
