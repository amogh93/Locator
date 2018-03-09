package com.nascentech.locator.data;

/**
 * Created by Amogh on 25-01-2018.
 */

public class TempStorage
{
    public static String fcmMessage;
    public static String fcmRecipient;

    public static String currentState;

    public static void reset()
    {
        fcmMessage="";
        fcmRecipient="";
    }
}
