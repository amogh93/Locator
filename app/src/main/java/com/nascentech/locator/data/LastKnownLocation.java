package com.nascentech.locator.data;

import android.location.Location;

/**
 * Created by Amogh on 17-11-2017.
 */

public class LastKnownLocation
{
    private static Location location;

    public static void setLastKnownLocation(Location mLocation)
    {
        location=mLocation;
    }

    public static Location getLastKnownLocation()
    {
        return location;
    }
}
