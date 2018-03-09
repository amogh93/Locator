package com.nascentech.locator.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.nascentech.locator.model.FCMToken;

/**
 * Created by Amogh on 24-01-2018.
 */

public class FCMTable extends SQLiteOpenHelper
{
    private Context mContext;

    private static final String TABLE_NAME ="fcm_token";
    private static final String TOKEN_ID="token_id";
    private static final String FCM_TOKEN="fcm_token";
    private static final String DEVICE_ID="device_id";
    private static final String STATE="state";

    // Database Information
    private static final String DB_NAME = "LOCATOR_TOKEN.DB";

    // database version
    private static final int DB_VERSION = 1;

    private static final String CREATE_TABLE = "create table " + TABLE_NAME + "("
            + TOKEN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + FCM_TOKEN + " TEXT NOT NULL, "
            + DEVICE_ID + " TEXT NOT NULL, "
            + STATE + " TEXT);";

    public FCMTable(Context context)
    {
        super(context, DB_NAME, null, DB_VERSION);
        mContext=context;
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void addToken(FCMToken fcmToken)
    {
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues values=new ContentValues();
        values.put(FCM_TOKEN,fcmToken.getToken());
        values.put(DEVICE_ID,fcmToken.getDevice_id());
        values.put(STATE,fcmToken.getState());
        db.insert(TABLE_NAME,null,values);
        db.close();
    }

    public FCMToken getTokenById(int id)
    {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME, new String[] { TOKEN_ID,FCM_TOKEN,DEVICE_ID,STATE}, TOKEN_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        FCMToken fcmToken=new FCMToken();
        fcmToken.setId(cursor.getInt(0));
        fcmToken.setToken(cursor.getString(1));
        fcmToken.setDevice_id(cursor.getString(2));
        fcmToken.setState(cursor.getString(3));
        db.close();

        return fcmToken;
    }

    public FCMToken getToken()
    {
        FCMToken fcmToken=new FCMToken();
        String selectQuery = "SELECT  * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if(cursor.moveToFirst())
        {
            do
            {
                fcmToken.setId(cursor.getInt(0));
                fcmToken.setToken(cursor.getString(1));
                fcmToken.setDevice_id(cursor.getString(2));
                fcmToken.setState(cursor.getString(3));
            }
            while (cursor.moveToNext());
        }
        db.close();

        return fcmToken;
    }
    public void updateToken(FCMToken fcmToken)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(FCM_TOKEN,fcmToken.getToken());
        db.update(TABLE_NAME, values, TOKEN_ID + " = ?", new String[] { String.valueOf(fcmToken.getId())});
        db.close();
    }

    public void updateState(FCMToken fcmToken)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(STATE,fcmToken.getState());
        db.update(TABLE_NAME, values, TOKEN_ID + " = ?", new String[] { String.valueOf(fcmToken.getId())});
        db.close();
    }

    public int getRowCount()
    {
        String countQuery = "SELECT  * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count=cursor.getCount();
        cursor.close();
        db.close();
        return count;
    }
}
