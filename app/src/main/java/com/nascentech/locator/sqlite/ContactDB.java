package com.nascentech.locator.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.nascentech.locator.model.RegisteredContacts;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Amogh on 27-01-2018.
 */

public class ContactDB extends SQLiteOpenHelper
{
    private Context context;

    private static final String TABLE_NAME ="contacts";
    private static final String CONTACT_ID="contact_id";
    private static final String NAME="name";
    private static final String PHONE_NUMBER="phone_number";
    private static final String GENDER="message";
    private static final String LAST_SEEN="timestamp";

    // Database Information
    private static final String DB_NAME = "LOCATOR_CONTACT.DB";

    // database version
    private static final int DB_VERSION = 1;

    private static final String CREATE_TABLE = "create table " + TABLE_NAME + "("
            + CONTACT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + NAME + " TEXT NOT NULL, "
            + PHONE_NUMBER + " TEXT NOT NULL, "
            + GENDER + " TEXT NOT NULL, "
            + LAST_SEEN + " TEXT);";

    public ContactDB(Context context)
    {
        super(context, DB_NAME, null, DB_VERSION);
        this.context=context;
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

    public void addContacts(List<RegisteredContacts> registeredContactsList)
    {
        SQLiteDatabase db=this.getWritableDatabase();
        db.beginTransaction();
        for(RegisteredContacts contacts:registeredContactsList)
        {
            ContentValues values=new ContentValues();
            values.put(NAME,contacts.getName());
            values.put(PHONE_NUMBER,contacts.getPhoneNumber());
            values.put(GENDER,contacts.getGender());
            values.put(LAST_SEEN,contacts.getLastSeen());
            db.insert(TABLE_NAME,null,values);
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
    }

    public List<RegisteredContacts> getRegisteredContacts()
    {
        List<RegisteredContacts> registeredContactsList=new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if(cursor.moveToFirst())
        {
            do
            {
                RegisteredContacts contacts=new RegisteredContacts();
                contacts.setId(cursor.getInt(0));
                contacts.setName(cursor.getString(1));
                contacts.setPhoneNumber(cursor.getString(2));
                contacts.setGender(cursor.getString(3));
                contacts.setLastSeen(cursor.getString(4));

                registeredContactsList.add(contacts);
            }
            while (cursor.moveToNext());
        }
        db.close();

        return registeredContactsList;
    }

    public void updateRegisteredContacts(List<RegisteredContacts> registeredContactsList)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from "+ TABLE_NAME);
        db.close();
        addContacts(registeredContactsList);
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
