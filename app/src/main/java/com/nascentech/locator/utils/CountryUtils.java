package com.nascentech.locator.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Amogh on 21-01-2018.
 */

public class CountryUtils
{
    private List<String> countryList=new ArrayList<>();
    private HashMap<String,String> countryCodes=new HashMap<>();

    public CountryUtils()
    {
        //init list
        countryList.add("SELECT COUNTRY");
        countryList.add("INDIA");

        //init map
        countryCodes.put("INDIA","+91");
    }

    public List<String> getCountryList()
    {
        return countryList;
    }

    public String getCountryCode(String countryName)
    {
        if(countryCodes.containsKey(countryName.toUpperCase()))
        {
            return countryCodes.get(countryName.toUpperCase());
        }

        return "null";
    }
}
