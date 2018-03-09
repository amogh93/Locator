package com.nascentech.locator.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
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
import com.nascentech.locator.adapter.ChatAdapter;
import com.nascentech.locator.constants.AppConstants;
import com.nascentech.locator.data.TempStorage;
import com.nascentech.locator.model.Chat;
import com.nascentech.locator.model.FCMToken;
import com.nascentech.locator.providers.Provider;
import com.nascentech.locator.sqlite.ChatDB;
import com.nascentech.locator.sqlite.FCMTable;
import com.nascentech.locator.utils.ServerUtils;

import org.apache.commons.lang3.StringEscapeUtils;

import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChatActivity extends AppCompatActivity
{
    private RecyclerView mRecyclerView;
    private ChatAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<Chat> chatList=new ArrayList<>();
    private EditText chatText;
    private Button sendChat;
    private String number;
    private Toolbar toolbar;
    private Handler stateHandler,handler;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Intent intent=getIntent();
        final String phoneNumber=intent.getStringExtra("phoneNumber");
        number=phoneNumber;
        String activityTitle=intent.getStringExtra("activityTitle");
        setTitle(activityTitle);

        chatList=new ChatDB(this).getChatByNumber(phoneNumber);

        toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mRecyclerView=findViewById(R.id.chatRecyclerView);
        mLayoutManager=new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter=new ChatAdapter(chatList);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        mRecyclerView.scrollToPosition((mAdapter.getItemCount()>0)?mAdapter.getItemCount()-1:0);
        Provider.chatList=chatList;
        Provider.chatAdapter=mAdapter;
        Provider.mRecyclerView=mRecyclerView;

        chatText=findViewById(R.id.chatText);
        sendChat=findViewById(R.id.sendChat);

        chatText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                if(stateHandler!=null)
                {
                    stateHandler.removeCallbacks(StateChangeRunnable);
                }
                ServerUtils.setUserState("Typing...",ChatActivity.this);
            }

            @Override
            public void afterTextChanged(Editable s)
            {
                stateHandler=new Handler();
                stateHandler.postDelayed(StateChangeRunnable,2000);
            }

            Runnable StateChangeRunnable=new Runnable() {
                @Override
                public void run() {
                    ServerUtils.setUserState("Online", ChatActivity.this);
                }
            };
        });

        sendChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!chatText.getText().toString().trim().isEmpty())
                {
                    String message=chatText.getText().toString().trim();
                    Chat chat=new Chat();
                    chat.setMessage(message);
                    chat.setTimestamp(new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(new Date()));
                    chat.setPhoneNumber(phoneNumber);
                    chat.setType("sent");
                    chatList.add(chat);
                    mAdapter.notifyDataSetChanged();
                    mRecyclerView.scrollToPosition(mAdapter.getItemCount()-1);
                    new ChatDB(ChatActivity.this).addChat(chat);
                    new NetworkCheck().execute(message,phoneNumber);
                    chatText.setText("");
                    hideIme();
                }
            }
        });
    }

    @Override
    public void onPause()
    {
        super.onPause();
        handler.removeCallbacks(runnable);
        ServerUtils.setUserState(new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(new Date()),this);
    }

    @Override
    public void onResume()
    {
        ServerUtils.setUserState("Online",this);
        checkUserState(number);
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.locate_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.locate:
                new NetworkCheck().execute("locate_map",number);
                break;
            case R.id.clear_chat:
                AlertDialog.Builder builder=new AlertDialog.Builder(this);
                builder.setTitle("Clear chat history");
                builder.setMessage("Do you want to delete this chat history?");
                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ChatDB chatDB=new ChatDB(ChatActivity.this);
                        chatDB.clearChat(number);
                        chatList.clear();
                        mAdapter.notifyDataSetChanged();
                        Toast.makeText(ChatActivity.this, "Chat history deleted successfully", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.setCancelable(false);
                builder.create().show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void hideIme()
    {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(chatText.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
    }

    private class NetworkCheck extends AsyncTask<String, Void, Boolean>
    {
        private String message = "";
        private String phoneNumber = "";

        @Override
        protected Boolean doInBackground(String... params)
        {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnectedOrConnecting())
            {
                try
                {
                    URL url = new URL(AppConstants.CHECK_SERVER);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setConnectTimeout(5000);
                    connection.connect();
                    if (connection.getResponseCode() == 200)
                    {
                        message = params[0];
                        phoneNumber = params[1];
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
            if (result)
            {
                FCMTable fcmTable=new FCMTable(ChatActivity.this);
                if(fcmTable.getRowCount()>0)
                {
                    FCMToken fcmToken=fcmTable.getToken();
                    sendMessage(message,phoneNumber,fcmToken.getDevice_id(),ChatActivity.this);
                }
            }
            else
            {
                AlertDialog.Builder builder=new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle("Network error");
                builder.setMessage("Please check your internet connection and try again");
                builder.setPositiveButton("CLOSE",null);
                builder.setCancelable(false);
                builder.create().show();
            }
        }

        private void sendMessage(final String message, final String phoneNumber, final String deviceId,Context context)
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
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        AlertDialog.Builder builder=new AlertDialog.Builder(ChatActivity.this);
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
    }

    private void checkUserState(final String phoneNumber)
    {
        ServerUtils.getUserState(phoneNumber,toolbar,this);

        handler=new Handler();
        handler.postDelayed(runnable,2000);
    }

    Runnable runnable=new Runnable() {
        @Override
        public void run() {
            checkUserState(number);
        }
    };
}
