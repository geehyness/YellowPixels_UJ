package com.yukisoft.yellowpixels.JavaRepositories.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yukisoft.yellowpixels.JavaRepositories.Models.CategoryModel;
import com.yukisoft.yellowpixels.R;

import java.io.IOException;
import java.util.ArrayList;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CommentViewHolder> {
    private ArrayList<CategoryModel> CatList;
    private CategoryAdapter.OnItemClickListener catListener;

    public interface OnItemClickListener {
        void onItemClick(int position) throws IOException;
    }

    public void setOnItemClickListener(CategoryAdapter.OnItemClickListener listener) {
        catListener = listener;
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        public ImageView catIconView;
        public TextView catNameView;

        public CommentViewHolder(View itemView, final CategoryAdapter.OnItemClickListener listener) {
            super(itemView);
            catIconView = itemView.findViewById(R.id.catIcon);
            catNameView = itemView.findViewById(R.id.catName);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            try {
                                listener.onItemClick(position);
                            } catch (IOException e) {
                                e.printStackTrace();
                                Toast.makeText(v.getContext(), "Unable to play message!\nPlease report error in 'Help' menu.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
            });
        }
    }

    public CategoryAdapter(ArrayList<CategoryModel> exampleList) {
        CatList = exampleList;
    }

    @NonNull
    @Override
    public CategoryAdapter.CommentViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_category, viewGroup, false);
        CategoryAdapter.CommentViewHolder evh = new CategoryAdapter.CommentViewHolder(v, catListener);
        return evh;
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryAdapter.CommentViewHolder commentViewHolder, int i) {
        CategoryModel currentItem = CatList.get(i);

        commentViewHolder.catIconView.setImageResource(R.drawable.ic_sell);
        commentViewHolder.catNameView.setText(currentItem.getName());
    }

    @Override
    public int getItemCount() {
        return CatList.size();
    }

}
