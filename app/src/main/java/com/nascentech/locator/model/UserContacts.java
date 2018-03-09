package com.nascentech.locator.model;

import android.support.annotation.NonNull;

import java.io.Serializable;

/**
 * Created by Amogh on 20-01-2018.
 */

public class UserContacts implements Comparable<UserContacts>,Serializable
{
    private String contact_id;
    private String contact_name;
    private String contact_number;

    public String getContact_id() {
        return contact_id;
    }

    public void setContact_id(String contact_id) {
        this.contact_id = contact_id;
    }

    public String getContact_name() {
        return contact_name;
    }

    public void setContact_name(String contact_name) {
        this.contact_name = contact_name;
    }

    public String getContact_number() {
        return contact_number;
    }

    public void setContact_number(String contact_number) {
        this.contact_number = contact_number;
    }

    @Override
    public int compareTo(@NonNull UserContacts o)
    {
        if(this.contact_name !=null)
        {
            return this.contact_name.toLowerCase().compareTo(o.getContact_name().toLowerCase());
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }
}
