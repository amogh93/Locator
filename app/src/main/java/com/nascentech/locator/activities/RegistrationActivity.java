package com.nascentech.locator.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.IdRes;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.nascentech.locator.MainActivity;
import com.nascentech.locator.R;
import com.nascentech.locator.constants.AppConstants;
import com.nascentech.locator.model.FCMToken;
import com.nascentech.locator.sqlite.FCMTable;
import com.nascentech.locator.utils.ContactUtils;
import com.nascentech.locator.utils.CountryUtils;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class RegistrationActivity extends AppCompatActivity
{
    private Button registerUserButton;
    private EditText name,phoneNumber;
    private Spinner countrySpinner;
    private ProgressBar progressBar;
    private RadioGroup genderGroup;
    private String gender="Male";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Boolean isRegistered= PreferenceManager.getDefaultSharedPreferences(this).getBoolean("isRegistered",false);

        if(isRegistered)
        {
            Intent intent=new Intent(this,MainActivity.class);
            startActivity(intent);
            finish();
        }
        else
        {
            setContentView(R.layout.activity_registration);
            name=findViewById(R.id.name);
            phoneNumber=findViewById(R.id.phoneNumber);
            countrySpinner=findViewById(R.id.countrySpinner);
            progressBar=findViewById(R.id.progressBar);
            registerUserButton=findViewById(R.id.registerUserButton);
            genderGroup=findViewById(R.id.genderRadioGroup);

            genderGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
            {
                @Override
                public void onCheckedChanged(RadioGroup group, @IdRes int checkedId)
                {
                    RadioButton radioButton=findViewById(checkedId);
                    if(radioButton.getText().toString().equalsIgnoreCase("Male"))
                    {
                        gender="Male";
                    }
                    else if(radioButton.getText().toString().equalsIgnoreCase("Female"))
                    {
                        gender="Female";
                    }
                }
            });
            final CountryUtils countryUtils=new CountryUtils();
            ArrayAdapter<String> dataAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,countryUtils.getCountryList());
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            countrySpinner.setAdapter(dataAdapter);

            registerUserButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if(name.getText().toString().isEmpty())
                    {
                        name.setError("Enter your name");
                    }
                    else if(phoneNumber.getText().toString().isEmpty())
                    {
                        phoneNumber.setError("Enter phone number");
                    }
                    else if(countrySpinner.getSelectedItem().toString().equalsIgnoreCase("select country"))
                    {
                        Toast.makeText(RegistrationActivity.this, "Select country", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        String user_name=name.getText().toString().trim();
                        String phone_number=phoneNumber.getText().toString().trim();
                        String country=countrySpinner.getSelectedItem().toString().trim();
                        String countryCode=countryUtils.getCountryCode(country).trim();
                        if(country.equalsIgnoreCase("india"))
                        {
                            if(ContactUtils.isValidNumber(AppConstants.REGION_INDIA,phone_number))
                            {
                                new NetworkCheck().execute(user_name,countryCode+phone_number,country);
                            }
                            else
                            {
                                phoneNumber.setError("Invalid mobile number");
                            }
                        }
                    }
                }
            });
        }
    }

    private class NetworkCheck extends AsyncTask<String, Void, Boolean>
    {
        private String name = "";
        private String phoneNumber = "";
        private String country = "";

        @Override
        protected void onPreExecute()
        {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(String... params)
        {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnectedOrConnecting())
            {
                try
                {
                    URL url = new URL("http://www.google.com");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setConnectTimeout(5000);
                    connection.connect();
                    if (connection.getResponseCode() == 200)
                    {
                        name = params[0];
                        phoneNumber = params[1];
                        country = params[2];
                        return true;
                    }
                }
                catch (Exception e)
                {
                    return false;
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result)
        {
            progressBar.setVisibility(View.GONE);
            if (result)
            {
                FCMTable fcmTable=new FCMTable(RegistrationActivity.this);
                if(fcmTable.getRowCount()>0)
                {
                    FCMToken fcmToken=fcmTable.getToken();
                    registerUser(name,phoneNumber,country,gender,fcmToken,fcmTable,RegistrationActivity.this);
                }
            }
            else
            {
                AlertDialog.Builder builder=new AlertDialog.Builder(RegistrationActivity.this);
                builder.setTitle("Network error");
                builder.setMessage("Please check your internet connection and try again");
                builder.setPositiveButton("CLOSE",null);
                builder.setCancelable(false);
                builder.create().show();
            }
        }

        private void registerUser(final String name, final String phoneNumber, final String country,final String gender,final FCMToken fcmToken,final FCMTable fcmTable,Context context)
        {
            try
            {
                RequestQueue queue = Volley.newRequestQueue(context);
                StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConstants.REGISTRATION_URL, new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response)
                    {
                        if (response.equalsIgnoreCase("success"))
                        {
                            new Handler(Looper.getMainLooper()).post(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    try
                                    {
                                        fcmToken.setState("registered");
                                        fcmTable.updateState(fcmToken);
                                        PreferenceManager.getDefaultSharedPreferences(RegistrationActivity.this).edit().putBoolean("isRegistered",true).commit();
                                    }
                                    catch (Exception e)
                                    {
                                        e.printStackTrace();
                                    }
                                    finally
                                    {
                                        startActivity(new Intent(RegistrationActivity.this,MainActivity.class));
                                        finish();
                                    }
                                }
                            });
                        }
                        else
                        {
                            new Handler(Looper.getMainLooper()).post(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    AlertDialog.Builder builder=new AlertDialog.Builder(RegistrationActivity.this);
                                    builder.setTitle("Request error");
                                    builder.setMessage("We are unable to fetch the request sent from your mobile due to incorrect request format, please contact the developer of this app to rectify the issue");
                                    builder.setPositiveButton("CLOSE",null);
                                    builder.setCancelable(false);
                                    builder.create().show();
                                }
                            });
                        }
                    }
                },
                        new Response.ErrorListener()
                        {
                            @Override
                            public void onErrorResponse(final VolleyError error)
                            {
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        AlertDialog.Builder builder=new AlertDialog.Builder(RegistrationActivity.this);
                                        builder.setTitle("Server error");
                                        builder.setMessage("We are experiencing some technical issues right now, we will be back online soon..."+ error.getMessage());
                                        builder.setPositiveButton("CLOSE",null);
                                        builder.setCancelable(false);
                                        builder.create().show();
                                    }
                                });
                            }
                        })
                {
                    @Override
                    protected Map<String, String> getParams()
                    {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("name", name);
                        params.put("phoneNumber", phoneNumber);
                        params.put("country", country);
                        params.put("gender", gender);
                        params.put("deviceId", fcmToken.getDevice_id());
                        params.put("tokenId", fcmToken.getToken());
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
    }
}
