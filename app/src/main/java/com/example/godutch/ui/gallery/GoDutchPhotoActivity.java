package com.example.godutch.ui.gallery;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.godutch.R;

public class GoDutchPhotoActivity extends AppCompatActivity {
    public static final String EXTRA_SPACE_PHOTO = "GoDutchPhotoActivity.SPACE_PHOTO";
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_detail);

        imageView = findViewById(R.id.detail_image);
        GoDutchPhoto photo = getIntent().getParcelableExtra(EXTRA_SPACE_PHOTO);

        Glide.with(this)
                .asBitmap()
                .load(photo.getUrl())
                .error(R.drawable.ic_notifications_black_24dp)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .into(imageView);
    }
}
