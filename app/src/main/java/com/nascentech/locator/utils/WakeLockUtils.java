package com.nascentech.locator.utils;

import android.content.Context;
import android.os.PowerManager;

import com.nascentech.locator.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Amogh on 25-01-2018.
 */

public class WakeLockUtils
{
    private static PowerManager.WakeLock wakeLock;
    private static HashMap<String,Class> wakeLockGrants=new HashMap<>();
    private static PowerManager powerManager;

    public static void acquireWakeLock(Class clazz,Context context)
    {
        if(powerManager==null)
        {
            powerManager = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        }

        if(wakeLock==null)
        {
            wakeLock=powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, context.getString(R.string.app_name));
        }

        if(!wakeLock.isHeld())
        {
            wakeLock.acquire();
        }

        wakeLockGrants.put(clazz.getSimpleName(),clazz);
    }

    public static void tryReleaseWakeLock(Class clazz)
    {
        wakeLockGrants.remove(clazz.getSimpleName());
        if(wakeLockGrants.isEmpty() && wakeLock.isHeld())
        {
            wakeLock.release();
        }
    }
}
