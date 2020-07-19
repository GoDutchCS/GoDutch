package com.example.godutch.ui.godutch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.godutch.Constants;
import com.example.godutch.MainActivity;
import com.example.godutch.R;
import com.example.godutch.ui.godutch.party.GoDutchPartyFragment;
import com.google.android.material.button.MaterialButton;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class GoDutchMainFragment extends Fragment {
    private static int REQUEST_NEW_PARTY_DIALOG = 1;
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private GoDutchViewModel goDutchViewModel;
    private MaterialButton partyGenButton;
    private OkHttpClient client = new OkHttpClient();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        goDutchViewModel = new ViewModelProvider(this).get(GoDutchViewModel.class);
        View root = inflater.inflate(R.layout.fragment_godutch, container, false);
        partyGenButton = root.findViewById(R.id.new_party_button);
        partyGenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchNewPartyDialog();
            }
        });
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity)getActivity()).getSupportActionBar().hide();
    }

    @Override
    public void onStop() {
        super.onStop();
        ((AppCompatActivity)getActivity()).getSupportActionBar().show();
    }

    public void launchNewPartyDialog() {
        Intent intent = new Intent(getActivity(), NewPartyActivity.class);
        intent.putExtra("USER_ID", getActivity().getIntent().getStringExtra("USER_ID"));
        startActivityForResult(intent, REQUEST_NEW_PARTY_DIALOG);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_NEW_PARTY_DIALOG && resultCode == Activity.RESULT_OK)  {
            String postBody = data.getStringExtra("party");
            Log.v("Foo", postBody);
            RequestBody body = RequestBody.create(postBody, JSON);
            Request request = new Request.Builder()
                    .url(String.format("%s/api/parties/add", Constants.SERVER_IP))
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    call.cancel();
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    final String jsonString = response.body().string();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((MainActivity)getActivity()).getViewPager().setCurrentItem(1);
                        }
                    });
                }
            });
        }
    }
}