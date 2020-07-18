package com.example.godutch.ui.gallery;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;

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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GalleryFragment extends Fragment implements View.OnClickListener {
    private GalleryViewModel galleryViewModel;
    private FloatingActionButton options, galleryAdd, cameraAdd;
    private final OkHttpClient client = new OkHttpClient();
    private ImageGalleryAdapter adapter;
    private Animation fab_open, fab_close;
    private Uri currentImageUri;
    private boolean isFabOpen = false;
    private static int PICK_IMAGE_MULTIPLE = 1;
    private static int PICTURE_RESULT = 2;
    private static String[] requiredPermissions = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };
    private static int PERMISSIONS_REQUEST_ALL = 8;
    private static int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 9;
    private static int PERMISSIONS_REQUEST_IMAGE_CAPTURE = 10;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        galleryViewModel = new ViewModelProvider(this).get(GalleryViewModel.class);
        View root = inflater.inflate(R.layout.fragment_gallery, container, false);

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this.getContext(), 3);
        RecyclerView recyclerView = root.findViewById(R.id.gallery);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        requestRequiredPermissions();
        adapter = new ImageGalleryAdapter(this.getContext(), this.getActivity());
        recyclerView.setAdapter(adapter);

        options = root.findViewById(R.id.add_options);
        options.setOnClickListener(this);
        galleryAdd = root.findViewById(R.id.gallery_add);
        galleryAdd.setOnClickListener(this);
        cameraAdd = root.findViewById(R.id.camera_add);
        cameraAdd.setOnClickListener(this);

        fab_open = AnimationUtils.loadAnimation(getContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getContext(), R.anim.fab_close);

        return root;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        animate();
        switch (id) {
            case R.id.gallery_add:
                requestRequiredPermissions();
                launchGallery();
                break;
            case R.id.camera_add:
                requestRequiredPermissions();
                launchCamera();
                break;
            case R.id.add_options:
                options
                    .animate()
                    .rotation(isFabOpen ? 45 : 0)
                    .setInterpolator(new LinearInterpolator())
                    .setDuration(300);
                break;
        }
    }

    private void animate() {
        if (isFabOpen) {
            galleryAdd.startAnimation(fab_close);
            cameraAdd.startAnimation(fab_close);
            galleryAdd.setVisibility(View.GONE);
            cameraAdd.setVisibility(View.GONE);
            galleryAdd.setClickable(false);
            cameraAdd.setClickable(false);
            isFabOpen = false;
        } else {
            galleryAdd.startAnimation(fab_open);
            cameraAdd.startAnimation(fab_open);
            galleryAdd.setVisibility(View.VISIBLE);
            cameraAdd.setVisibility(View.VISIBLE);
            galleryAdd.setClickable(true);
            cameraAdd.setClickable(true);
            isFabOpen = true;
        }
    }

    private void requestRequiredPermissions() {
        boolean allGranted = true;
        for (String permission : GalleryFragment.requiredPermissions) {
            boolean granted = ActivityCompat.checkSelfPermission(getContext(), permission) == PackageManager.PERMISSION_GRANTED;
            allGranted = allGranted & granted;
        }

        if (!allGranted)
            requestPermissions(requiredPermissions, PERMISSIONS_REQUEST_ALL);
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
        if (resultCode != Activity.RESULT_OK)
            return;

        if (requestCode == PICK_IMAGE_MULTIPLE && data != null) {
            if (data.getClipData() != null)
                sendImagesToServer(data, false);
            else if (data.getData() != null)
                sendImagesToServer(data, true);
        } else if (requestCode == PICTURE_RESULT) {
            sendTakenPhotoToServer();
        }
    }

    private void sendTakenPhotoToServer() {
        RequestBody formBody = addImageFromUri(
                new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("id", getActivity().getIntent().getStringExtra("USER_ID")),
                currentImageUri)
                .build();
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
                adapter.fetchPhotos(getActivity().getIntent().getStringExtra("USER_ID"));
            }
        });
    }

    private byte[] getBytes(InputStream inputStream) throws IOException {
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
            Log.e("GalleryFragment", Log.getStackTraceString(e));
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
                adapter.fetchPhotos(getActivity().getIntent().getStringExtra("USER_ID"));
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

    private void launchGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Images"), PICK_IMAGE_MULTIPLE);
    }

    private void launchCamera() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, timeStamp);
        values.put(MediaStore.Images.Media.DESCRIPTION, timeStamp);
        currentImageUri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, currentImageUri);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null)
            startActivityForResult(intent, PICTURE_RESULT);
    }
}