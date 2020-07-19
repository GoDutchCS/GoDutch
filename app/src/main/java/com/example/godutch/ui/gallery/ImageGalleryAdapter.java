package com.example.godutch.ui.gallery;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.bumptech.glide.Glide;
import com.example.godutch.Constants;
import com.example.godutch.R;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ImageGalleryAdapter extends RecyclerView.Adapter<ImageGalleryAdapter.MyViewHolder> {
    protected ArrayList<GoDutchPhoto> photos;
    protected HashSet<Integer> selectedPhotos;
    private Context context;
    private GalleryFragment fragment;

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public ImageView imageView;
        public boolean selected;

        public MyViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.item_photo);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            if (position == RecyclerView.NO_POSITION)
                return;

            if (ImageGalleryAdapter.this.fragment.deleteMode) {
                if (this.selected) {
                    removeBorder();
                    selectedPhotos.remove(getAdapterPosition());
                } else {
                    setBorder();
                    selectedPhotos.add(getAdapterPosition());
                }
                this.selected = !this.selected;
            } else {
                GoDutchPhoto spacePhoto = photos.get(position);
                Intent intent = new Intent(context, GoDutchPhotoActivity.class);
                intent.putExtra(GoDutchPhotoActivity.EXTRA_SPACE_PHOTO, spacePhoto);
                context.startActivity(intent);
            }
        }

        @Override
        public boolean onLongClick(View view) {
            fragment.setTrashCanIcon();
            setBorder();
            ImageGalleryAdapter.this.fragment.deleteMode = !ImageGalleryAdapter.this.fragment.deleteMode;
            this.selected = true;
            selectedPhotos = new HashSet<>();
            selectedPhotos.add(getAdapterPosition());
            return true;
        }

        public void setBorder() {
            this.itemView.setBackgroundResource(R.drawable.imagview_border);
        }

        public void removeBorder() {
            this.itemView.setBackgroundResource(R.drawable.border);
        }
    }

    public ImageGalleryAdapter(Context context, Activity activity, GalleryFragment galleryFragment) {
        this.context = context;
        this.fragment = galleryFragment;
        fetchPhotos(activity.getIntent().getStringExtra("USER_ID"));
    }

    @Override
    public ImageGalleryAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View photoView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo, parent, false);
        return new ImageGalleryAdapter.MyViewHolder(photoView);
    }

    @Override
    public void onBindViewHolder(final ImageGalleryAdapter.MyViewHolder holder, int position) {
        GoDutchPhoto photo = photos.get(position);

        CircularProgressDrawable cpd = new CircularProgressDrawable(this.context);
        cpd.setStrokeWidth(5f);
        cpd.setCenterRadius(30f);
        cpd.start();

        Glide.with(this.context)
                .load(photo.getUrl())
                .placeholder(cpd)
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return photos == null ? 0 : photos.size();
    }

    protected void fetchPhotos(String id) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(String.format("%s/api/images/list/%s", Constants.SERVER_IP, id))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                call.cancel();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String jsonString = response.body().string();

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    public void run() {
                        try {
                            JSONObject data = new JSONObject(jsonString);
                            JSONArray photos = data.getJSONArray("photos");
                            ImageGalleryAdapter.this.photos = new ArrayList<>(photos.length());
                            for (int i = 0; i < photos.length(); i++) {
                                ImageGalleryAdapter.this.photos.add(
                                        new GoDutchPhoto(
                                                String.format("%s/%s", Constants.SERVER_IP, photos.getString(i)),
                                                "test"
                                        )
                                );
                            }
                            notifyDataSetChanged();
                        } catch (JSONException e) {
                            Log.e("ImageGalleryAdapter", Log.getStackTraceString(e));
                        }
                    }
                });
            }
        });
    }

    protected void fetchPhotos(final JSONArray photos) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                ImageGalleryAdapter.this.photos = new ArrayList<>(photos.length());
                for (int i = 0; i < photos.length(); i++) {
                    try {
                        ImageGalleryAdapter.this.photos.add(
                                new GoDutchPhoto(
                                        String.format("%s/%s", Constants.SERVER_IP, photos.getString(i)),
                                        "test"
                                )
                        );
                    } catch (JSONException e) {
                        Log.e("ImageGalleryAdapter", Log.getStackTraceString(e));
                    }
                }

                notifyDataSetChanged();
            }
        });
    }
}
