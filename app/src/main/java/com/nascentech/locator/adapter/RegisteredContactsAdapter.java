package com.nascentech.locator.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.telephony.PhoneNumberUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.nascentech.locator.R;
import com.nascentech.locator.model.RegisteredContacts;
import com.nascentech.locator.utils.ContactUtils;

import java.util.List;

/**
 * Created by Amogh on 24-01-2018.
 */

public class RegisteredContactsAdapter extends RecyclerView.Adapter<RegisteredContactsAdapter.RegisteredContactsViewHolder>
{
    private Context context;
    private List<RegisteredContacts> registeredContactsList;

    public RegisteredContactsAdapter(List<RegisteredContacts> registeredContactsList)
    {
        this.registeredContactsList=registeredContactsList;
    }

    @Override
    public RegisteredContactsAdapter.RegisteredContactsViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_list_view, parent, false);
        context=itemView.getContext();
        return new RegisteredContactsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RegisteredContactsAdapter.RegisteredContactsViewHolder holder, int position)
    {
        RegisteredContacts registeredContacts=registeredContactsList.get(position);
        if(registeredContacts.getGender().equalsIgnoreCase("male"))
        {
            holder.userImageView.setImageResource(R.drawable.user_male);
        }
        else
        {
            holder.userImageView.setImageResource(R.drawable.user_female);
        }

        holder.userName.setText(ContactUtils.getContactName(registeredContacts.getPhoneNumber(),context));
        holder.phoneNumber.setText(registeredContacts.getPhoneNumber());
        String lastSeen=registeredContacts.getLastSeen();
        if(lastSeen.contains("Online") || lastSeen.contains("Typing..."))
        {
            holder.lastSeen.setText(registeredContacts.getLastSeen());
        }
        else
        {
            holder.lastSeen.setText("last seen: "+registeredContacts.getLastSeen());
        }
    }

    @Override
    public int getItemCount()
    {
        return registeredContactsList.size();
    }

    public class RegisteredContactsViewHolder extends RecyclerView.ViewHolder
    {
        public ImageView userImageView;
        public TextView userName,phoneNumber,lastSeen;

        public RegisteredContactsViewHolder(View itemView)
        {
            super(itemView);
            userImageView=itemView.findViewById(R.id.userImage);
            userName=itemView.findViewById(R.id.userName);
            phoneNumber=itemView.findViewById(R.id.phoneNumber);
            lastSeen=itemView.findViewById(R.id.lastSeen);
        }
    }
}
