package com.yukisoft.yellowpixels.JavaRepositories.Adapters;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.yukisoft.yellowpixels.R;

import java.io.IOException;
import java.util.ArrayList;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.CommentViewHolder> {
    private Context mContext;
    private ArrayList<String> ImageList;
    private ImageAdapter.OnItemClickListener imageListener;

    public interface OnItemClickListener {
        void onItemClick(int position) throws IOException;
    }

    public void setOnItemClickListener(ImageAdapter.OnItemClickListener listener) {
        imageListener = listener;
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;

        public CommentViewHolder(View itemView, final ImageAdapter.OnItemClickListener listener) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageUri);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        try {
                            listener.onItemClick(position);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    public ImageAdapter(Context context, ArrayList<String> exampleList) {
        mContext = context;
        ImageList = exampleList;
    }

    @NonNull
    @Override
    public ImageAdapter.CommentViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_image, viewGroup, false);
        ImageAdapter.CommentViewHolder evh = new ImageAdapter.CommentViewHolder(v, imageListener);
        return evh;
    }

    @Override
    public void onBindViewHolder(@NonNull ImageAdapter.CommentViewHolder commentViewHolder, int i) {
        String currentItem = ImageList.get(i);

        Picasso.with(mContext)
                .load(currentItem)
                .resize(500, 500)
                .centerCrop()
                .placeholder(R.drawable.ic_business)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(commentViewHolder.imageView, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        //Try again online if cache failed
                        Picasso.with(mContext)
                                .load(currentItem)
                                .resize(500, 500)
                                .centerCrop()
                                .error(R.drawable.ic_business)
                                .placeholder(R.drawable.ic_business)
                                .into(commentViewHolder.imageView, new Callback() {
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
    }

    @Override
    public int getItemCount() {
        return ImageList.size();
    }

}
