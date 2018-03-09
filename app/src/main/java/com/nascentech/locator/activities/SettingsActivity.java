package com.nascentech.locator.activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;

import com.nascentech.locator.R;
import com.nascentech.locator.utils.SystemUtils;

public class SettingsActivity extends AppCompatPreferenceActivity
{
    private static final int PERMISSIONS_REQUEST_LOCATION=92;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsActivity.SettingsPreferenceFragment()).commit();
    }

    public static class SettingsPreferenceFragment extends PreferenceFragment
    {
        private static CheckBoxPreference my_location;
        private Preference about,battery_optimization;

        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_main);

            my_location=(CheckBoxPreference)findPreference("my_location");
            my_location.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
            {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue)
                {
                    Boolean isChecked=(Boolean)newValue;
                    if(isChecked)
                    {
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !SystemUtils.isPermissionGranted(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION))
                        {
                            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_LOCATION);
                        }
                    }
                    return true;
                }
            });

            about=findPreference("about_app");
            about.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
            {
                @Override
                public boolean onPreferenceClick(Preference preference)
                {
                    AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
                    builder.setTitle("About");
                    builder.setMessage("Locator\nVersion 0.0.1 beta\nDeveloped by Tech5");
                    builder.setPositiveButton("CLOSE",null);
                    builder.setCancelable(true);
                    builder.create().show();
                    return true;
                }
            });

            battery_optimization=findPreference("battery_optimization");
            battery_optimization.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference)
                {
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    {
                        PowerManager powerManager=(PowerManager)getActivity().getSystemService(POWER_SERVICE);
                        final String package_name=getActivity().getPackageName();
                        if(!powerManager.isIgnoringBatteryOptimizations(package_name))
                        {
                            AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
                            builder.setTitle("Ignore battery optimizations");
                            builder.setMessage("Permission to ignore battery optimizations is not granted, do you want to grant it now?");
                            builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent battery_optimisation_intent=new Intent();
                                    battery_optimisation_intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                                    battery_optimisation_intent.setData(Uri.parse("package:"+package_name));
                                    startActivity(battery_optimisation_intent);
                                }
                            });
                            builder.setNegativeButton("NO",null);
                            builder.setCancelable(false);
                            builder.create().show();
                        }
                        else
                        {
                            showPermissionGrantedDialog("Permission is already granted","Permission to ignore battery optimizations is already granted for this app");
                        }
                    }
                    else
                    {
                        showPermissionGrantedDialog("Android 6.0 and higher","Only for android 6.0 and higher");
                    }
                    return true;
                }
            });
        }

        public static void unCheck()
        {
            my_location.setChecked(false);
        }

        private void showPermissionGrantedDialog(String title,String message)
        {
            AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
            builder.setTitle(title);
            builder.setMessage(message);
            builder.setPositiveButton("CLOSE",null);
            builder.setCancelable(false);
            builder.create().show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        switch (requestCode)
        {
            case PERMISSIONS_REQUEST_LOCATION:

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {

                }
                else
                {
                    SettingsActivity.SettingsPreferenceFragment.unCheck();
                }
        }
    }
}
