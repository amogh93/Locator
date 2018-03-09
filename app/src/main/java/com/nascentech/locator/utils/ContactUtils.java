package com.nascentech.locator.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;

import com.nascentech.locator.constants.AppConstants;
import com.nascentech.locator.model.UserContacts;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by Amogh on 20-01-2018.
 */

public class ContactUtils
{
    private List<UserContacts> userContactsList =new ArrayList<>();

    public List<UserContacts> getContactList(Context context)
    {
        try
        {
            ContentResolver contentResolver=context.getContentResolver();

            Cursor cursor=contentResolver.query(ContactsContract.Contacts.CONTENT_URI,null,null,null,null);

            if(cursor.getCount()>0)
            {
                while (cursor.moveToNext())
                {
                    String phoneNumber="";
                    String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                    String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                    if (Integer.parseInt(cursor.getString(cursor.getColumnIndex
                            (ContactsContract.Contacts.HAS_PHONE_NUMBER)))>0)
                    {
                        Cursor pCur = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                new String[]{id}, null,null);

                        while (pCur.moveToNext())
                        {
                            phoneNumber=pCur.getString(pCur.getColumnIndex
                                    (ContactsContract.CommonDataKinds.Phone.NUMBER));

                            if(userContactsList.isEmpty())
                            {
                                UserContacts contacts=new UserContacts();
                                contacts.setContact_id(id);
                                contacts.setContact_name(name);
                                contacts.setContact_number(phoneNumber);
                                userContactsList.add(contacts);
                            }
                            else
                            {
                                boolean isDuplicate=false;
                                for(UserContacts contacts: userContactsList)
                                {
                                    if(PhoneNumberUtils.compare(contacts.getContact_number(),phoneNumber))
                                    {
                                        isDuplicate=true;
                                        break;
                                    }
                                }
                                if(!isDuplicate)
                                {
                                    UserContacts contacts=new UserContacts();
                                    contacts.setContact_id(id);
                                    contacts.setContact_name(name);
                                    contacts.setContact_number(phoneNumber);
                                    userContactsList.add(contacts);
                                }
                            }
                        }
                        pCur.close();
                    }
                }
            }
            cursor.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return userContactsList;
    }

    public static String getContactName(String phoneNumber, Context context)
    {
        Uri uri=Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,Uri.encode(phoneNumber));

        String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME};

        String contactName="";
        Cursor cursor=context.getContentResolver().query(uri,projection,null,null,null);

        if (cursor != null)
        {
            if(cursor.moveToFirst())
            {
                contactName=cursor.getString(0);
            }
            cursor.close();
        }

        return contactName;
    }

    public static String getPhoneNumber(String name, Context context)
    {
        String ret = "";
        Cursor c=null;
        try
        {
            boolean doPrintName=false;
            String oldName="";
            String selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY+" like'%" + name +"%'";
            String[] projection = new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER};
            c = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection, selection, null, null);
            while (c.moveToNext())
            {
                String number=c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                String contactName=getContactName(number,context);
                if(!oldName.equals(contactName))
                {
                    doPrintName=true;
                }
                if(doPrintName)
                {
                    ret+=contactName+"\n";
                    doPrintName=false;
                    oldName=contactName;
                }
                ret+=number+"\n";
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                c.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        if(ret.length()>0)
        {
            return  ret;
        }
        return "No such contact";
    }

    public static boolean isValidNumber(int region,String number)
    {
        boolean isValid=false;
        if(region== AppConstants.REGION_INDIA)
        {
            isValid=Pattern.compile("[0-9]{10}").matcher(number).matches();
        }

        return isValid;
    }
}
