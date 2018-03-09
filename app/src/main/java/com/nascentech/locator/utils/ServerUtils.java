package com.nascentech.locator.utils;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nascentech.locator.R;
import com.nascentech.locator.adapter.RegisteredContactsAdapter;
import com.nascentech.locator.constants.AppConstants;
import com.nascentech.locator.data.TempStorage;
import com.nascentech.locator.model.RegisteredContacts;
import com.nascentech.locator.model.UserContacts;
import com.nascentech.locator.sqlite.ContactDB;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONArray;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Amogh on 24-01-2018.
 */

public class ServerUtils
{
    private List<RegisteredContacts> registeredContactsList=new ArrayList<>();

    public void getRegisteredContacts(final RegisteredContactsAdapter adapter, final List<RegisteredContacts> list,final Context context)
    {
        try
        {
            List<UserContacts> userContacts=new ContactUtils().getContactList(context);
            Gson gson=new Gson();
            String element = gson.toJson(userContacts, new TypeToken<ArrayList<UserContacts>>() {}.getType());
            final JSONArray jsonArray=new JSONArray(element);
            RequestQueue queue = Volley.newRequestQueue(context);
            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.POST, AppConstants.GET_REGISTERED_CONTACTS, jsonArray,new Response.Listener<JSONArray>()
            {
                @Override
                public void onResponse(JSONArray response)
                {
                    if(response!=null)
                    {
                        Gson gson=new Gson();
                        Type listType = new TypeToken<List<RegisteredContacts>>(){}.getType();
                        registeredContactsList=gson.fromJson(response.toString(),listType);
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run()
                            {
                                Toast.makeText(context, "Contact list refreshed", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    if(!registeredContactsList.isEmpty())
                    {
                        ContactDB contactDB=new ContactDB(context);
                        try
                        {
                            list.clear();
                            Collections.sort(registeredContactsList);
                            if(contactDB.getRowCount()>0)
                            {
                                contactDB.updateRegisteredContacts(registeredContactsList);
                            }
                            else
                            {
                                contactDB.addContacts(registeredContactsList);
                            }
                        }
                        catch (Exception e)
                        {

                        }
                        finally
                        {
                            list.addAll(registeredContactsList);
                            adapter.notifyDataSetChanged();
                        }
                    }
                }
            },
                    new Response.ErrorListener()
                    {
                        @Override
                        public void onErrorResponse(VolleyError error)
                        {

                        }
                    })
            {
                @Override
                public Map<String, String> getHeaders()
                {
                    Map<String, String> headers = new HashMap<String, String>();
                    headers.put("api-key", context.getString(R.string.locator_key));
                    return headers;
                }
            };
            jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(jsonArrayRequest);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void sendMessage(final String message, final String phoneNumber, final String deviceId, final Context context)
    {
        try
        {
            RequestQueue queue = Volley.newRequestQueue(context);
            StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConstants.SEND_MESSAGE, new Response.Listener<String>()
            {
                @Override
                public void onResponse(String response)
                {
                    if (response.equalsIgnoreCase("success"))
                    {

                    }
                }
            },
                    new Response.ErrorListener()
                    {
                        @Override
                        public void onErrorResponse(final VolleyError error)
                        {

                        }
                    })
            {
                @Override
                protected Map<String, String> getParams()
                {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("message", StringEscapeUtils.escapeJava(message));
                    params.put("phoneNumber", phoneNumber);
                    params.put("deviceId", deviceId);
                    return params;
                }
            };
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(stringRequest);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void updateLastSeen(final String lastSeen,final String deviceId, final Context context)
    {
        try
        {
            RequestQueue queue = Volley.newRequestQueue(context);
            StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConstants.UPDATE_LAST_SEEN, new Response.Listener<String>()
            {
                @Override
                public void onResponse(String response)
                {
                    if (response.equalsIgnoreCase("success"))
                    {

                    }
                }
            },
                    new Response.ErrorListener()
                    {
                        @Override
                        public void onErrorResponse(final VolleyError error)
                        {

                        }
                    })
            {
                @Override
                protected Map<String, String> getParams()
                {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("lastSeen", StringEscapeUtils.escapeJava(lastSeen));
                    params.put("deviceId", deviceId);
                    return params;
                }
            };
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(stringRequest);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void getUserState(final String phoneNumber,final Toolbar toolbar, final Context context)
    {
        try
        {
            RequestQueue queue = Volley.newRequestQueue(context);
            StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConstants.GET_USER_STATE, new Response.Listener<String>()
            {
                @Override
                public void onResponse(final String response)
                {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run()
                        {
                            if(response.contains("Online") || response.contains("Typing..."))
                            {
                                toolbar.setSubtitle(response);
                            }
                            else
                            {
                                toolbar.setSubtitle("last seen "+response);
                            }
                        }
                    });
                }
            },
                    new Response.ErrorListener()
                    {
                        @Override
                        public void onErrorResponse(final VolleyError error)
                        {

                        }
                    })
            {
                @Override
                protected Map<String, String> getParams()
                {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("phoneNumber", phoneNumber);
                    return params;
                }
            };
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(stringRequest);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void setUserState(String state,Context context)
    {
        if(!state.equalsIgnoreCase(TempStorage.currentState))
        {
            String serial="";
            if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.O)
            {
                serial=Build.getSerial();
            }
            else
            {
                serial=Build.SERIAL;
            }

            updateLastSeen(state,serial,context);
            TempStorage.currentState=state;
        }
    }
}
