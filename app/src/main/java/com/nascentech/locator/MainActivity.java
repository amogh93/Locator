package com.nascentech.locator;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.nascentech.locator.activities.ChatActivity;
import com.nascentech.locator.activities.SettingsActivity;
import com.nascentech.locator.adapter.RegisteredContactsAdapter;
import com.nascentech.locator.data.TempStorage;
import com.nascentech.locator.listeners.RecyclerTouchListener;
import com.nascentech.locator.model.RegisteredContacts;
import com.nascentech.locator.sqlite.ContactDB;
import com.nascentech.locator.sqlite.FCMTable;
import com.nascentech.locator.utils.ContactUtils;
import com.nascentech.locator.utils.ServerUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
{
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RegisteredContactsAdapter adapter;
    private List<RegisteredContacts> registeredContactsList;
    private static final int CONTACT_PERMISSION_REQUEST=91;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        registeredContactsList=new ContactDB(this).getRegisteredContacts();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_CONTACTS},CONTACT_PERMISSION_REQUEST);
            }
            else
            {
                loadRecyclerView();
            }
        }
        else
        {
            loadRecyclerView();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.sync:
                try
                {
                    new ServerUtils().getRegisteredContacts(adapter,registeredContactsList,this);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        switch (requestCode)
        {
            case CONTACT_PERMISSION_REQUEST:
                if(grantResults.length>0)
                {
                    boolean readContactsPermission=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    if(readContactsPermission)
                    {
                        loadRecyclerView();
                    }
                    else
                    {
                        finish();
                    }
                }
        }
    }

    @Override
    public void onResume()
    {
        ServerUtils.setUserState("Online",this);
        super.onResume();
    }

    @Override
    public void onPause()
    {
        ServerUtils.setUserState(new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(new Date()),this);
        super.onPause();
    }

    private void loadRecyclerView()
    {
        mRecyclerView=findViewById(R.id.contactsRecyclerView);
        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(this,mRecyclerView, new RecyclerTouchListener.ClickListener()
        {
            @Override
            public void onClick(View view, int position)
            {
                RegisteredContacts registeredContacts=registeredContactsList.get(position);
                Intent intent=new Intent(MainActivity.this, ChatActivity.class);
                intent.putExtra("phoneNumber",registeredContacts.getPhoneNumber());
                intent.putExtra("activityTitle", ContactUtils.getContactName(registeredContacts.getPhoneNumber(),MainActivity.this));
                startActivity(intent);
            }

            @Override
            public void onLongClick(View view, int position)
            {

            }
        }));
        mLayoutManager=new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        adapter=new RegisteredContactsAdapter(registeredContactsList);
        mRecyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        if(registeredContactsList.isEmpty())
        {
            try
            {
                new ServerUtils().getRegisteredContacts(adapter,registeredContactsList,this);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
