package com.nascentech.locator.providers;

import android.support.v7.widget.RecyclerView;

import com.nascentech.locator.adapter.ChatAdapter;
import com.nascentech.locator.model.Chat;

import java.util.List;

/**
 * Created by Amogh on 25-01-2018.
 */

public class Provider
{
    public static List<Chat> chatList;
    public static ChatAdapter chatAdapter;
    public static RecyclerView mRecyclerView;
}
