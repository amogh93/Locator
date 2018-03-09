package com.nascentech.locator.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.nascentech.locator.model.Chat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Amogh on 25-01-2018.
 */

public class ChatDB extends SQLiteOpenHelper
{
    private Context context;

    private static final String TABLE_NAME ="chats";
    private static final String CHAT_ID="chat_id";
    private static final String PHONE_NUMBER="phone_number";
    private static final String MESSAGE="message";
    private static final String TIMESTAMP="timestamp";
    private static final String TYPE="type";

    // Database Information
    private static final String DB_NAME = "LOCATOR_CHAT.DB";

    // database version
    private static final int DB_VERSION = 1;

    private static final String CREATE_TABLE = "create table " + TABLE_NAME + "("
            + CHAT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + PHONE_NUMBER + " TEXT NOT NULL, "
            + MESSAGE + " TEXT NOT NULL, "
            + TIMESTAMP + " TEXT, "
            + TYPE + " TEXT);";

    public ChatDB(Context context)
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

    public void addChat(Chat chat)
    {
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues values=new ContentValues();
        values.put(PHONE_NUMBER,chat.getPhoneNumber());
        values.put(MESSAGE,chat.getMessage());
        values.put(TIMESTAMP,chat.getTimestamp());
        values.put(TYPE,chat.getType());
        db.insert(TABLE_NAME,null,values);
        db.close();
    }

    public List<Chat> getChatByNumber(String phoneNumber)
    {
        List<Chat> chatList=new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor=db.query(TABLE_NAME,new String[] { CHAT_ID,PHONE_NUMBER,MESSAGE,TIMESTAMP,TYPE},PHONE_NUMBER + "=?",new String[] { String.valueOf(phoneNumber) },null,null,CHAT_ID+" ASC");
        if(cursor.moveToFirst())
        {
            do
            {
                Chat chat=new Chat();
                chat.setId(cursor.getInt(0));
                chat.setPhoneNumber(cursor.getString(1));
                chat.setMessage(cursor.getString(2));
                chat.setTimestamp(cursor.getString(3));
                chat.setType(cursor.getString(4));
                chatList.add(chat);
            }
            while (cursor.moveToNext());
        }
        db.close();

        return chatList;
    }

    public List<Chat> getAllChats()
    {
        List<Chat> chatList=new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if(cursor.moveToFirst())
        {
            do
            {
                Chat chat=new Chat();
                chat.setId(cursor.getInt(0));
                chat.setPhoneNumber(cursor.getString(1));
                chat.setMessage(cursor.getString(2));
                chat.setTimestamp(cursor.getString(3));
                chat.setType(cursor.getString(4));
                chatList.add(chat);
            }
            while (cursor.moveToNext());
        }
        db.close();

        return chatList;
    }

    public void clearChat(List<Chat> chatList)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        for(Chat chat:chatList)
        {
            db.delete(TABLE_NAME, CHAT_ID + " = ?", new String[] { String.valueOf(chat.getId()) });
        }
        db.close();
    }

    public void clearChat(String phoneNumber)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, PHONE_NUMBER + " = ?", new String[] { phoneNumber });
        db.close();
    }
}
