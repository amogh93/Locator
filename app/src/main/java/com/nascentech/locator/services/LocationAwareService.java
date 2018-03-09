package com.nascentech.locator.services;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.nascentech.locator.MainActivity;
import com.nascentech.locator.R;
import com.nascentech.locator.data.LastKnownLocation;
import com.nascentech.locator.receivers.LocationReceiver;
import com.nascentech.locator.utils.SystemUtils;
import com.nascentech.locator.utils.WakeLockUtils;

/**
 * Created by Amogh on 15-11-2017.
 */

public class LocationAwareService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener
{
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private boolean mInProgress;
    private PendingIntent pendingIntent;

    private static final int FOREGROUND_NOTIFICATION_ID=33451;

    public class LocalBinder extends Binder
    {
        public LocationAwareService getServerInstance()
        {
            return LocationAwareService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        mInProgress = false;
        if(mGoogleApiClient==null)
        {
            buildGoogleApiClient();
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        WakeLockUtils.acquireWakeLock(LocationAwareService.class,getApplicationContext());

        if(Build.VERSION.SDK_INT == Build.VERSION_CODES.O)
        {
            Intent main_intent=new Intent(getApplicationContext(), MainActivity.class);
            main_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent main_pending_intent = PendingIntent.getActivity(getApplicationContext(), FOREGROUND_NOTIFICATION_ID+1, main_intent, PendingIntent.FLAG_ONE_SHOT);

            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel notificationChannel = new NotificationChannel("channel_0", "Main_Service", importance);
            notificationManager.createNotificationChannel(notificationChannel);
            NotificationCompat.Builder notification=new NotificationCompat.Builder(getApplicationContext(),notificationChannel.getId());
            notification.setContentTitle(getString(R.string.app_name)+" service")
                    .setContentText("Retrieving and updating location...")
                    .setTicker("Getting current location...")
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setContentIntent(main_pending_intent)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setOngoing(true);

            startForeground(FOREGROUND_NOTIFICATION_ID,notification.build());
        }

        if(mGoogleApiClient.isConnected() || mInProgress)
        {
            return START_STICKY;
        }

        if(mGoogleApiClient==null)
        {
            buildGoogleApiClient();
        }

        if(!mGoogleApiClient.isConnected() || !mGoogleApiClient.isConnecting() && !mInProgress)
        {
            mGoogleApiClient.connect();
            mInProgress=true;
        }

        return START_STICKY;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle)
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if(SystemUtils.isPermissionGranted(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) && SystemUtils.isPermissionGranted(getApplicationContext(),Manifest.permission.ACCESS_COARSE_LOCATION))
            {
                startLocationService();
            }
            else
            {
                stopSelf();
            }
        }
        else
        {
            startLocationService();
        }
    }

    private void startLocationService()
    {
        Location location=LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        LastKnownLocation.setLastKnownLocation(location);
        createLocationRequest();

        Intent intent = new Intent(this, LocationReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(this, 54321, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        if(mGoogleApiClient.isConnected())
        {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, pendingIntent);
        }
    }

    private void createLocationRequest()
    {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(1000);
    }

    @Override
    public void onConnectionSuspended(int i)
    {
        mInProgress = false;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
    {
        mInProgress = false;
    }

    @Override
    public void onLocationChanged(Location location)
    {
        LastKnownLocation.setLastKnownLocation(location);
    }

    @Override
    public void onDestroy()
    {
        try
        {
            mInProgress = false;
            if(this.mGoogleApiClient.isConnected())
            {
                LocationServices.FusedLocationApi.removeLocationUpdates(this.mGoogleApiClient, pendingIntent);
                pendingIntent.cancel();

                this.mGoogleApiClient.unregisterConnectionCallbacks(this);
                this.mGoogleApiClient.unregisterConnectionFailedListener(this);
                this.mGoogleApiClient.disconnect();
            }

            WakeLockUtils.tryReleaseWakeLock(LocationAwareService.class);
        }
        catch (Exception e)
        {
            //e.printStackTrace();
        }

        super.onDestroy();
    }

    protected synchronized void buildGoogleApiClient()
    {
        this.mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }
}
