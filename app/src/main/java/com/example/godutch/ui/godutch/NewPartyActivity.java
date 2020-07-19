package com.example.godutch.ui.godutch;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.godutch.R;
import com.google.android.material.button.MaterialButton;

import org.json.JSONObject;

import java.util.ArrayList;

public class NewPartyActivity extends AppCompatActivity {
    private RecyclerView members;
    private String userID;
    private NewPartyAdapter adapter;
    private MaterialButton cancel, confirm;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.godutch_generate_party);

        userID = getIntent().getStringExtra("USER_ID");
        cancel = findViewById(R.id.new_party_cancel);
        confirm = findViewById(R.id.new_party_confirm);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        members = findViewById(R.id.party_members);
        members.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        members.setLayoutManager(layoutManager);

        adapter = new NewPartyAdapter(userID);
        members.setAdapter(adapter);
    }

    public class NewPartyAdapter extends RecyclerView.Adapter<NewPartyAdapter.NPViewHolder> {
        private String userID;
        private ArrayList<JSONObject> users;

        public class NPViewHolder extends RecyclerView.ViewHolder {
            public ImageView profile;
            public TextView number;
            public TextView name;

            public NPViewHolder(View itemView) {
                super(itemView);
                profile = itemView.findViewById(R.id.profile_photo);
                number = itemView.findViewById(R.id.profile_number);
                name = itemView.findViewById(R.id.profile_name);
            }
        }

        public NewPartyAdapter(String userID) {
            this.userID = userID;
        }

        @NonNull
        @Override
        public NPViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return null;
        }

        @Override
        public void onBindViewHolder(@NonNull NPViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return 0;
        }

        public void fetchUsers() {

        }
    }
}
