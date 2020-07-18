package com.example.godutch.ui.gallery;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.godutch.Constants;
import com.example.godutch.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GalleryFragment extends Fragment {
    private GalleryViewModel galleryViewModel;
    private FloatingActionButton fab;
    private static int PICK_IMAGE_MULTIPLE = 1;
    private static int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 8;
    private final OkHttpClient client = new OkHttpClient();
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        galleryViewModel = new ViewModelProvider(this).get(GalleryViewModel.class);
        View root = inflater.inflate(R.layout.fragment_gallery, container, false);

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this.getContext(), 3);
        RecyclerView recyclerView = root.findViewById(R.id.gallery);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        ImageGalleryAdapter adapter = new ImageGalleryAdapter(this.getContext(), GoDutchPhoto.getPhotos());
        recyclerView.setAdapter(adapter);

        fab = root.findViewById(R.id.gallery_add);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestGalleryPermission();
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Images"), PICK_IMAGE_MULTIPLE);
            }
        });

        return root;
    }

    private void requestGalleryPermission() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE }, PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PICK_IMAGE_MULTIPLE && data != null) {
                if (data.getClipData() != null)
                    sendImagesToServer(data, false);
                else if (data.getData() != null)
                    sendImagesToServer(data, true);
            }
        }
    }

    public byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    private MultipartBody.Builder addImageFromUri(MultipartBody.Builder builder, Uri uri) {
        byte[] byteArray = null;
        try {
            InputStream in = getActivity().getContentResolver().openInputStream(uri);
            byteArray = getBytes(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return builder.addFormDataPart(
                "photos",
                getFileName(uri),
                RequestBody.create(byteArray, MediaType.parse("image/jpg"))
        );
    }

    private void sendImagesToServer(Intent data, boolean single) {
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("id", getActivity().getIntent().getStringExtra("USER_ID"));
        if (single) {
            Uri imageUri = data.getData();
            builder = addImageFromUri(builder, imageUri);
        } else {
            int length = data.getClipData().getItemCount();
            for (int i = 0; i < length; i++) {
                Uri imageUri = data.getClipData().getItemAt(i).getUri();
                builder = addImageFromUri(builder, imageUri);
            }
        }
        RequestBody formBody = builder.build();
        Request request = new Request.Builder()
                .url(String.format("%s/api/images/upload", Constants.SERVER_IP))
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                call.cancel();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String jsonString = response.body().string();
            }
        });
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst())
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1)
                result = result.substring(cut + 1);
        }
        return result;
    }
}