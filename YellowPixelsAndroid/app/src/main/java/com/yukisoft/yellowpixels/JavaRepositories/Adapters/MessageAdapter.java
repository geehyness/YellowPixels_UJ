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
import com.yukisoft.yellowpixels.JavaRepositories.Models.MessageModel;
import com.yukisoft.yellowpixels.JavaRepositories.Models.MessageModelFull;
import com.yukisoft.yellowpixels.R;

import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ItemViewHolder> {
    private Context mContext;
    private ArrayList<MessageModelFull> itemList;
    private MessageAdapter.OnItemClickListener itemListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(MessageAdapter.OnItemClickListener listener) {
        itemListener = listener;
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        public ImageView imgFrom;
        public TextView txtFrom;
        public TextView txtMessage;
        public TextView txtTime;

        public ItemViewHolder(View itemView, final MessageAdapter.OnItemClickListener listener) {
            super(itemView);
            imgFrom = itemView.findViewById(R.id.imgFrom);
            txtFrom = itemView.findViewById(R.id.txtFrom);
            txtMessage = itemView.findViewById(R.id.txtMessage);
            txtTime = itemView.findViewById(R.id.txtTime);

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

    public MessageAdapter(Context context, ArrayList<MessageModelFull> exampleList) {
        mContext = context;
        itemList = exampleList;
    }

    @NonNull
    @Override
    public MessageAdapter.ItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_message, viewGroup, false);
        MessageAdapter.ItemViewHolder evh = new MessageAdapter.ItemViewHolder(v, itemListener);
        return evh;
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ItemViewHolder itemViewHolder, int i) {
        MessageModelFull currentItem = itemList.get(i);

        itemViewHolder.imgFrom.setImageResource(R.drawable.ic_business);

        Picasso.with(mContext)
                .load(currentItem.getFrom().getDpURI())
                .resize(500, 500)
                .centerCrop()
                .placeholder(R.drawable.ic_person_dark)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(itemViewHolder.imgFrom, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        //Try again online if cache failed
                        Picasso.with(mContext)
                                .load(currentItem.getFrom().getDpURI())
                                .resize(500, 500)
                                .centerCrop()
                                .error(R.drawable.ic_person_dark)
                                .placeholder(R.drawable.ic_person_dark)
                                .into(itemViewHolder.imgFrom, new Callback() {
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

        itemViewHolder.txtFrom.setText(currentItem.getFrom().getName());
        itemViewHolder.txtMessage.setText(currentItem.getMessage());

        if (currentItem.getTimeSent() != null) {
            itemViewHolder.txtTime.setText(currentItem.getTimeSent().toString());
        } else {
            itemViewHolder.txtTime.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

}
