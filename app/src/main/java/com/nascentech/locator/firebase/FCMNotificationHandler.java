package com.nascentech.locator.firebase;

import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.nascentech.locator.activities.ChatActivity;
import com.nascentech.locator.data.TempStorage;
import com.nascentech.locator.model.Chat;
import com.nascentech.locator.providers.Provider;
import com.nascentech.locator.receivers.NotificationActionReceiver;
import com.nascentech.locator.services.LocationAwareService;
import com.nascentech.locator.sqlite.ChatDB;
import com.nascentech.locator.utils.ContactUtils;
import com.nascentech.locator.utils.NotificationUtils;
import com.nascentech.locator.utils.SystemUtils;

import org.apache.commons.lang3.StringEscapeUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Amogh on 24-01-2018.
 */

public class FCMNotificationHandler extends FirebaseMessagingService
{
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage)
    {
        if(remoteMessage.getData().size()>0)
        {
            if (remoteMessage.getData().get("action").equalsIgnoreCase("text"))
            {
                String message= StringEscapeUtils.unescapeJava(remoteMessage.getData().get("message"));
                String phoneNumber=remoteMessage.getData().get("title");
                boolean honorLocationRequest= PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("my_location",false);
                if(message.equalsIgnoreCase("locate_map"))
                {
                    if(honorLocationRequest)
                    {
                        TempStorage.fcmMessage=message;
                        TempStorage.fcmRecipient=phoneNumber;
                        startService(new Intent(getApplicationContext(), LocationAwareService.class));
                    }
                }
                else if(message.equalsIgnoreCase("locate") || message.equalsIgnoreCase("where are you"))
                {
                    if(honorLocationRequest)
                    {
                        TempStorage.fcmMessage=message;
                        TempStorage.fcmRecipient=phoneNumber;
                        startService(new Intent(getApplicationContext(), LocationAwareService.class));
                    }
                }
                else
                {
                    final Chat chat=new Chat();
                    chat.setType("received");
                    chat.setPhoneNumber(phoneNumber);
                    chat.setTimestamp(new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(new Date()));
                    chat.setMessage(message);
                    new ChatDB(getApplicationContext()).addChat(chat);
                    if(SystemUtils.isForeground(getApplicationContext()))
                    {
                        Handler handler=new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                try
                                {
                                    Provider.chatList.add(chat);
                                    Provider.chatAdapter.notifyDataSetChanged();
                                    Provider.mRecyclerView.scrollToPosition(Provider.chatAdapter.getItemCount()-1);
                                }
                                catch (Exception e)
                                {
                                    e.printStackTrace();
                                }
                                finally
                                {
                                    Vibrator vibrator=(Vibrator)getSystemService(VIBRATOR_SERVICE);
                                    vibrator.vibrate(150);
                                }
                            }
                        });
                    }
                    else
                    {
                        int notificationId=(int)System.currentTimeMillis();
                        Intent intent=new Intent(getApplicationContext(), ChatActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("phoneNumber",phoneNumber);
                        intent.putExtra("activityTitle", ContactUtils.getContactName(phoneNumber,getApplicationContext()));
                        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), notificationId, intent, PendingIntent.FLAG_ONE_SHOT);

                        Intent dismiss_intent=new Intent(getApplicationContext(), NotificationActionReceiver.class);
                        dismiss_intent.putExtra("notificationId",notificationId);
                        dismiss_intent.setAction(Long.toString(System.currentTimeMillis()));
                        PendingIntent dismiss_pending_intent=PendingIntent.getBroadcast(getApplicationContext(), notificationId, dismiss_intent, PendingIntent.FLAG_ONE_SHOT);

                        NotificationUtils.showBigTextDataNotification(getApplicationContext(),chat,"New Message",notificationId,pendingIntent,dismiss_pending_intent);
                    }
                }
            }
            else if(remoteMessage.getData().get("action").equalsIgnoreCase("url"))
            {
                String phoneNumber=remoteMessage.getData().get("title");
                String iconUrl=remoteMessage.getData().get("image");

                final Chat chat=new Chat();
                chat.setType("received_image");
                chat.setPhoneNumber(phoneNumber);
                chat.setTimestamp(new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(new Date()));
                chat.setMessage(iconUrl);
                new ChatDB(getApplicationContext()).addChat(chat);

                if(SystemUtils.isForeground(getApplicationContext()))
                {
                    Handler handler=new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            try
                            {
                                Provider.chatList.add(chat);
                                Provider.chatAdapter.notifyDataSetChanged();
                                Provider.mRecyclerView.scrollToPosition(Provider.chatAdapter.getItemCount()-1);
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                            finally
                            {
                                Vibrator vibrator=(Vibrator)getSystemService(VIBRATOR_SERVICE);
                                vibrator.vibrate(150);
                            }
                        }
                    });
                }

                int notificationId=(int)System.currentTimeMillis();
                String action_url=remoteMessage.getData().get("action_destination");
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(action_url));
                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),notificationId , intent,0);

                Intent dismiss_intent=new Intent(getApplicationContext(), NotificationActionReceiver.class);
                dismiss_intent.putExtra("notificationId",notificationId);
                dismiss_intent.setAction(Long.toString(System.currentTimeMillis()));
                PendingIntent dismiss_pending_intent=PendingIntent.getBroadcast(getApplicationContext(), notificationId, dismiss_intent, PendingIntent.FLAG_ONE_SHOT);

                NotificationUtils.showDataNotification(getApplicationContext(),phoneNumber,iconUrl,notificationId,pendingIntent,dismiss_pending_intent);
            }
        }
    }
}
