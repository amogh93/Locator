package com.nascentech.locator.adapter;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nascentech.locator.R;
import com.nascentech.locator.model.Chat;
import com.nascentech.locator.utils.ContactUtils;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

/**
 * Created by Amogh on 25-01-2018.
 */

public class ChatAdapter extends RecyclerView.Adapter
{
    private List<Chat> chatList;
    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;
    private static final int VIEW_TYPE_IMAGE_RECEIVED=3;
    private Context context;

    public ChatAdapter(List<Chat> list)
    {
        chatList=list;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view;

        if (viewType == VIEW_TYPE_MESSAGE_SENT)
        {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_sent, parent, false);
            context=view.getContext();
            return new SentMessageHolder(view);
        }
        else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED)
        {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_received, parent, false);
            context=view.getContext();
            return new ReceivedMessageHolder(view);
        }
        else if(viewType == VIEW_TYPE_IMAGE_RECEIVED)
        {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image_received, parent, false);
            context=view.getContext();
            return new ReceivedImageHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position)
    {
        Chat chat = chatList.get(position);

        switch (holder.getItemViewType())
        {
            case VIEW_TYPE_MESSAGE_SENT:
                ((SentMessageHolder) holder).bind(chat);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((ReceivedMessageHolder) holder).bind(chat);
                break;
            case VIEW_TYPE_IMAGE_RECEIVED:
                ((ReceivedImageHolder) holder).bind(chat);
                break;
        }
    }

    @Override
    public int getItemViewType(int position)
    {
        Chat chat = chatList.get(position);

        if (chat.getType().equalsIgnoreCase("sent"))
        {
            return VIEW_TYPE_MESSAGE_SENT;
        }
        else if(chat.getType().equalsIgnoreCase("received"))
        {
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }
        else
        {
            return VIEW_TYPE_IMAGE_RECEIVED;
        }
    }

    @Override
    public int getItemCount()
    {
        return chatList.size();
    }

    private class SentMessageHolder extends RecyclerView.ViewHolder
    {
        TextView messageText, timeText;

        SentMessageHolder(View itemView)
        {
            super(itemView);
            messageText = itemView.findViewById(R.id.text_message_body);
            timeText = itemView.findViewById(R.id.text_message_time);
        }

        void bind(Chat message)
        {
            messageText.setText(message.getMessage());
            timeText.setText(message.getTimestamp());
        }
    }

    private class ReceivedMessageHolder extends RecyclerView.ViewHolder
    {
        TextView messageText, timeText, nameText;

        ReceivedMessageHolder(View itemView)
        {
            super(itemView);
            messageText = itemView.findViewById(R.id.text_message_body);
            timeText = itemView.findViewById(R.id.text_message_time);
            nameText = itemView.findViewById(R.id.text_message_name);
        }

        void bind(Chat message)
        {
            messageText.setText(message.getMessage());
            timeText.setText(message.getTimestamp());
            nameText.setText(ContactUtils.getContactName(message.getPhoneNumber(),context));
        }
    }

    private class ReceivedImageHolder extends RecyclerView.ViewHolder
    {
        TextView timeText, nameText;
        ImageView imageView;

        ReceivedImageHolder(View itemView)
        {
            super(itemView);
            imageView=itemView.findViewById(R.id.image_body);
            timeText = itemView.findViewById(R.id.text_message_time);
            nameText = itemView.findViewById(R.id.text_message_name);
        }

        void bind(final Chat message)
        {
            timeText.setText(message.getTimestamp());
            nameText.setText(ContactUtils.getContactName(message.getPhoneNumber(),context));
            new SetImageView(imageView).execute(message.getMessage());
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String action_url=message.getMessage();
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(action_url));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            });
        }
    }

    private class SetImageView extends AsyncTask<String,Void,Bitmap>
    {
        private ImageView imageView;

        public SetImageView(ImageView imageView)
        {
            this.imageView=imageView;
        }

        @Override
        protected Bitmap doInBackground(String... params)
        {
            Bitmap logo = null;
            try
            {
                InputStream is = new URL(params[0]).openStream();
                logo = BitmapFactory.decodeStream(is);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            return logo;
        }

        @Override
        protected void onPostExecute(Bitmap result)
        {
            imageView.setImageBitmap(result);
        }
    }
}
