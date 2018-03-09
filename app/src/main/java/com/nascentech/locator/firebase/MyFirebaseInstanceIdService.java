package com.nascentech.locator.firebase;

import android.os.Build;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.nascentech.locator.model.FCMToken;
import com.nascentech.locator.sqlite.FCMTable;

/**
 * Created by Amogh on 20-01-2018.
 */

public class MyFirebaseInstanceIdService extends FirebaseInstanceIdService
{
    private static final String TAG="FCMInstanceIdService";

    @Override
    public void onTokenRefresh()
    {
        String refreshedToken= FirebaseInstanceId.getInstance().getToken();
        String serial="";
        if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.O)
        {
            serial=Build.getSerial();
        }
        else
        {
            serial=Build.SERIAL;
        }
        addTokenToDB(serial,refreshedToken);
    }

    private void addTokenToDB(String serial,String token)
    {
        FCMToken fcmToken=new FCMToken();
        fcmToken.setToken(token);
        fcmToken.setDevice_id(serial);
        fcmToken.setState("unregistered");

        FCMTable fcmTable=new FCMTable(getApplicationContext());
        if(fcmTable.getRowCount()==0)
        {
            fcmTable.addToken(fcmToken);
        }
        else
        {
            fcmTable.updateToken(fcmToken);
        }
    }
}
