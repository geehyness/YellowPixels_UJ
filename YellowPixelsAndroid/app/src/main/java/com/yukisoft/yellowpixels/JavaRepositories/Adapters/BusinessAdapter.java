package com.yukisoft.yellowpixels.JavaRepositories.Adapters;

import android.content.Context;
import android.text.TextUtils;
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
import com.yukisoft.yellowpixels.JavaRepositories.Fixed.AccountType;
import com.yukisoft.yellowpixels.JavaRepositories.Models.UserModel;
import com.yukisoft.yellowpixels.R;
import java.util.ArrayList;

public class BusinessAdapter extends RecyclerView.Adapter<BusinessAdapter.ItemViewHolder> {
    private Context mContext;
    private ArrayList<UserModel> itemList;
    private OnItemClickListener itemListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        itemListener = listener;
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        public ImageView businessPic;
        public TextView businessTitle;
        public TextView businessDescription;
        public TextView businessLocation;
        public TextView businessStatus;
        public ImageView verified;

        public ItemViewHolder(View itemView, final OnItemClickListener listener) {
            super(itemView);
            businessPic = itemView.findViewById(R.id.imgBusinessPic);
            businessTitle = itemView.findViewById(R.id.txtBusinessName);
            businessDescription = itemView.findViewById(R.id.txtBusinessDescription);
            businessLocation = itemView.findViewById(R.id.txtBusinessLocation);
            businessStatus = itemView.findViewById(R.id.txtBusinessStatus);
            verified = itemView.findViewById(R.id.icVerified);

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

    public BusinessAdapter(Context context, ArrayList<UserModel> exampleList) {
        mContext = context;
        itemList = exampleList;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_business, viewGroup, false);
        ItemViewHolder evh = new ItemViewHolder(v, itemListener);
        return evh;
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder itemViewHolder, int i) {
        UserModel currentItem = itemList.get(i);

        if (!currentItem.getType().equals(AccountType.Verified_Business)) {
            itemViewHolder.verified.setVisibility(View.GONE);
        }

        Picasso.with(mContext)
                .load(currentItem.getDpURI())
                .resize(500, 500)
                .centerCrop()
                .placeholder(R.drawable.ic_business)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(itemViewHolder.businessPic, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        //Try again online if cache failed
                        Picasso.with(mContext)
                                .load(currentItem.getDpURI())
                                .resize(500, 500)
                                .centerCrop()
                                .placeholder(R.drawable.ic_business)
                                .error(R.drawable.ic_business)
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

        itemViewHolder.businessTitle.setText(currentItem.getName());
        itemViewHolder.businessDescription.setText(currentItem.getEmail());

        if (currentItem.getDetails() != null && !TextUtils.isEmpty(currentItem.getDetails()))
            itemViewHolder.businessLocation.setText(currentItem.getDetails());
        else
            itemViewHolder.businessLocation.setText("No business description");

        if (currentItem.getLocation() != null && !TextUtils.isEmpty(currentItem.getLocation()))
            itemViewHolder.businessLocation.setText(currentItem.getLocation());
        else
            itemViewHolder.businessLocation.setText("Unknown Location");

        itemViewHolder.businessStatus.setText(currentItem.getType().toString().replace("_", " "));
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

}