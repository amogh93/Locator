package com.nascentech.locator.geocoder;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import java.util.List;
import java.util.Locale;

/**
 * Created by Amogh on 08-12-2017.
 */

public class GetAddress
{
    public static String getAddressString(Context context,double latitude, double longitude)
    {
        String complete_address="";
        List<Address> addresses;

        try
        {
            addresses = new Geocoder(context.getApplicationContext(), Locale.getDefault()).getFromLocation(latitude,longitude, 1);
            if (addresses != null && addresses.size() > 0)
            {
                Address address = addresses.get(0);
                String[] temp=address.toString().split("\"");
                complete_address=temp.length>1?temp[1]:"Not available";
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return complete_address;
    }
}
