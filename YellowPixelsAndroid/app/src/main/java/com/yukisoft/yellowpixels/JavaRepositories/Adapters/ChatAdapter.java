package com.yukisoft.yellowpixels.JavaRepositories.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.yukisoft.yellowpixels.JavaActivities.UserManagement.SettingsActivity;
import com.yukisoft.yellowpixels.JavaRepositories.Models.ChatModelFull;
import com.yukisoft.yellowpixels.R;

import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ItemViewHolder> {
    private Context mContext;
    private ArrayList<ChatModelFull> itemList;
    private ChatAdapter.OnItemClickListener itemListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(ChatAdapter.OnItemClickListener listener) {
        itemListener = listener;
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        public ImageView businessPic;
        public TextView chatName;
        public TextView chatLine;
        public TextView chatDate;

        public ItemViewHolder(View itemView, final ChatAdapter.OnItemClickListener listener) {
            super(itemView);
            businessPic = itemView.findViewById(R.id.chatIcon);
            chatName = itemView.findViewById(R.id.chatName);
            chatLine = itemView.findViewById(R.id.chatLine);
            chatDate = itemView.findViewById(R.id.chatDate);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(position);
                    }
                }
            });
        }
    }

    public ChatAdapter(Context context, ArrayList<ChatModelFull> exampleList) {
        mContext = context;
        itemList = exampleList;
    }

    @NonNull
    @Override
    public ChatAdapter.ItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_chat, viewGroup, false);
        ChatAdapter.ItemViewHolder evh = new ChatAdapter.ItemViewHolder(v, itemListener);
        return evh;
    }

    @Override
    public void onBindViewHolder(@NonNull ChatAdapter.ItemViewHolder itemViewHolder, int i) {
        ChatModelFull currentItem = itemList.get(i);

        itemViewHolder.businessPic.setImageResource(R.drawable.ic_business);
        itemViewHolder.chatName.setText(currentItem.getBusiness().getName());
        itemViewHolder.chatLine.setText(currentItem.getLastMessage());

        Picasso.with(mContext)
                .load(currentItem.getBusiness().getDpURI())
                .resize(500, 500)
                .centerCrop()
                .placeholder(R.drawable.ic_person_dark)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(itemViewHolder.businessPic, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        //Try again online if cache failed
                        Picasso.with(mContext)
                                .load(currentItem.getBusiness().getDpURI())
                                .resize(500, 500)
                                .centerCrop()
                                .error(R.drawable.ic_person_dark)
                                .placeholder(R.drawable.ic_person_dark)
                                .into(itemViewHolder.businessPic, new Callback() {
                                    @Override
                                    public void onSuccess() {

                                    }

                                    @Override
                                    public void onError() {
                                        Log.v("Picasso","Could not fetch image");
                                    }
                                });
                    }
                });

        itemViewHolder.chatDate.setText(currentItem.getLastMessageDate().toString());
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

}