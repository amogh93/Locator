package com.nascentech.locator.model;

import android.support.annotation.NonNull;

import java.io.Serializable;

/**
 * Created by Amogh on 24-01-2018.
 */

public class RegisteredContacts implements Comparable<RegisteredContacts>,Serializable
{
    private long id;
    private String name;
    private String phoneNumber;
    private String gender;
    private String lastSeen;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(String lastSeen) {
        this.lastSeen = lastSeen;
    }

    @Override
    public int compareTo(@NonNull RegisteredContacts o)
    {
        if(this.name !=null)
        {
            return this.name.toLowerCase().compareTo(o.getName().toLowerCase());
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }
}
