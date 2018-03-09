package com.nascentech.locator.data;

/**
 * Created by Amogh on 25-01-2018.
 */

public class ActivityDetectionData
{
    private static int detectedActivity=7;

    public static int getDetectedActivity() {
        return detectedActivity;
    }

    public static void setDetectedActivity(int detectedActivity) {
        ActivityDetectionData.detectedActivity = detectedActivity;
    }
}
