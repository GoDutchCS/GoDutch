package com.example.godutch.ui.gallery;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.godutch.R;

public class ImageGalleryAdapter extends RecyclerView.Adapter<ImageGalleryAdapter.MyViewHolder>  {
    private GoDutchPhoto[] photos;
    private Context context;
    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView imageView;
        public MyViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.item_photo);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            if(position != RecyclerView.NO_POSITION) {
                GoDutchPhoto spacePhoto = photos[position];
                Intent intent = new Intent(context, GoDutchPhotoActivity.class);
                intent.putExtra(GoDutchPhotoActivity.EXTRA_SPACE_PHOTO, spacePhoto);
                context.startActivity(intent);
            }
        }
    }

    public ImageGalleryAdapter(Context context, GoDutchPhoto[] photos) {
        this.context = context;
        this.photos = photos;
    }
    @Override
    public ImageGalleryAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View photoView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo, parent, false);
        return new ImageGalleryAdapter.MyViewHolder(photoView);
    }

    @Override
    public void onBindViewHolder(ImageGalleryAdapter.MyViewHolder holder, int position) {
        GoDutchPhoto photo = photos[position];
        Glide.with(this.context)
                .load(photo.getUrl())
                .placeholder(R.drawable.ic_notifications_black_24dp)
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return photos.length;
    }
}
