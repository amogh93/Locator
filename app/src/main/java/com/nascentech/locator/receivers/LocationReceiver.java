package com.nascentech.locator.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;

import com.google.android.gms.location.LocationResult;
import com.nascentech.locator.data.LastKnownLocation;
import com.nascentech.locator.data.TempStorage;
import com.nascentech.locator.geocoder.GetAddress;
import com.nascentech.locator.services.LocationAwareService;
import com.nascentech.locator.sqlite.FCMTable;
import com.nascentech.locator.utils.ServerUtils;
import com.nascentech.locator.utils.WakeLockUtils;


/**
 * Created by Amogh on 06-11-2017.
 */

public class LocationReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        if(LocationResult.hasResult(intent))
        {
            try
            {
                WakeLockUtils.acquireWakeLock(LocationReceiver.class,context);
                LocationResult mLocationResult = LocationResult.extractResult(intent);
                Location location=mLocationResult.getLastLocation();
                LastKnownLocation.setLastKnownLocation(location);
                FCMTable fcmTable=new FCMTable(context);

                if(TempStorage.fcmMessage.equalsIgnoreCase("locate_map"))
                {
                    String message="url,"+location.getLatitude()+","+location.getLongitude();
                    String deviceId=fcmTable.getToken().getDevice_id();
                    String recipient=TempStorage.fcmRecipient;

                    ServerUtils.sendMessage(message,recipient,deviceId,context);
                    TempStorage.reset();
                }
                else if(TempStorage.fcmMessage.equalsIgnoreCase("locate") || TempStorage.fcmMessage.equalsIgnoreCase("where are you"))
                {
                    String message="";
                    String deviceId=fcmTable.getToken().getDevice_id();
                    String recipient=TempStorage.fcmRecipient;
                    String street_address= GetAddress.getAddressString(context,location.getLatitude(),location.getLongitude());
                    if(!street_address.equalsIgnoreCase("not available") && street_address.length()>10)
                    {
                        message=street_address;
                        message=message+"\nhttps://www.google.com/maps/?q="+location.getLatitude()+","+location.getLongitude();
                    }
                    else
                    {
                        message="https://www.google.com/maps/?q="+location.getLatitude()+","+location.getLongitude();
                    }
                    ServerUtils.sendMessage(message,recipient,deviceId,context);
                    TempStorage.reset();
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                context.stopService(new Intent(context,LocationAwareService.class));
                WakeLockUtils.tryReleaseWakeLock(LocationReceiver.class);
            }
        }
    }
}
