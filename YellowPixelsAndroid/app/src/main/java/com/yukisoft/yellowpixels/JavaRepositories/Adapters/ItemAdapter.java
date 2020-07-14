package com.yukisoft.yellowpixels.JavaRepositories.Adapters;

import android.annotation.SuppressLint;
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
import com.yukisoft.yellowpixels.JavaRepositories.Models.ItemModel;
import com.yukisoft.yellowpixels.R;

import java.util.ArrayList;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {
    private Context mContext;
    private ArrayList<ItemModel> itemList;
    private OnItemClickListener itemListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        itemListener = listener;
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView itemImage;
        TextView itemTitle;
        TextView itemDescription;
        TextView itemPrice;

        ItemViewHolder(View itemView, final OnItemClickListener listener) {
            super(itemView);
            itemImage = itemView.findViewById(R.id.imgSellPic);
            itemTitle = itemView.findViewById(R.id.itemTitle);
            itemDescription = itemView.findViewById(R.id.itemDescription);
            itemPrice = itemView.findViewById(R.id.itemPrice);

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

    public ItemAdapter(Context context, ArrayList<ItemModel> exampleList) {
        mContext = context;
        itemList = exampleList;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_item, viewGroup, false);
        return new ItemViewHolder(v, itemListener);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder itemViewHolder, int i) {
        ItemModel currentItem = itemList.get(i);

        Picasso.with(mContext)
                .load(currentItem.getImages().get(0))
                .resize(500, 500)
                .centerCrop()
                .placeholder(R.drawable.ic_baseline_photo)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(itemViewHolder.itemImage, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        //Try again online if cache failed
                        Picasso.with(mContext)
                                .load(currentItem.getImages().get(0))
                                .resize(500, 500)
                                .centerCrop()
                                .error(R.drawable.ic_baseline_photo)
                                .placeholder(R.drawable.ic_baseline_photo)
                                .into(itemViewHolder.itemImage, new Callback() {
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

        itemViewHolder.itemTitle.setText(currentItem.getName());
        itemViewHolder.itemDescription.setText(currentItem.getDetails());
        itemViewHolder.itemPrice.setText(String.format("R %.2f", currentItem.getPrice()));
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

}