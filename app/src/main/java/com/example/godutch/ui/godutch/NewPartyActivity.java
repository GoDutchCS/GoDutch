package com.example.godutch.ui.godutch;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.godutch.Constants;
import com.example.godutch.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

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

public class NewPartyActivity extends AppCompatActivity {
    private RecyclerView members;
    private String userID;
    private NewPartyAdapter adapter;
    private MaterialButton cancel, confirm;
    private TextInputEditText partyName;
    private OkHttpClient client = new OkHttpClient();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.godutch_generate_party);

        userID = getIntent().getStringExtra("USER_ID");
        partyName = findViewById(R.id.party_name);
        cancel = findViewById(R.id.new_party_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        });
        confirm = findViewById(R.id.new_party_confirm);
        confirm.setEnabled(false);
        confirm.setBackgroundTintList(ContextCompat.getColorStateList(NewPartyActivity.this, R.color.material_grey));
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String body = adapter.generatePartyString();
                Intent intent = new Intent();
                intent.putExtra("party", body);
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
        private HashSet<Integer> selectedUsers;

        public class NPViewHolder extends RecyclerView.ViewHolder {
            public ImageView profile;
            public TextView number;
            public TextView name;
            public ImageButton selectButton;
            public boolean selected = false;

            public NPViewHolder(View itemView) {
                super(itemView);
                profile = itemView.findViewById(R.id.profile_photo);
                number = itemView.findViewById(R.id.profile_number);
                name = itemView.findViewById(R.id.profile_name);
                selectButton = itemView.findViewById(R.id.profile_select);
            }
        }

        public NewPartyAdapter(String userID) {
            this.userID = userID;
            this.selectedUsers = new HashSet<>();
            fetchUsers();
        }

        @NonNull
        @Override
        public NPViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View photoView = LayoutInflater.from(parent.getContext()).inflate(R.layout.godutch_contact_row, parent, false);
            return new NewPartyAdapter.NPViewHolder(photoView);
        }

        @Override
        public void onBindViewHolder(@NonNull final NPViewHolder holder, final int position) {
            JSONObject item = users.get(position);
            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    holder.selected = !holder.selected;
                    holder.selectButton.setImageResource(holder.selected ? R.drawable.ic_baseline_radio_button_checked_28 : R.drawable.ic_baseline_radio_button_unchecked_28);
                    if (holder.selected) {
                        selectedUsers.add(position);
                        if (selectedUsers.size() == 1) {
                            confirm.setBackgroundTintList(ContextCompat.getColorStateList(NewPartyActivity.this, R.color.material_blue));
                            confirm.setEnabled(true);
                        }
                    } else {
                        selectedUsers.remove(position);
                        if (selectedUsers.size() == 0) {
                            confirm.setBackgroundTintList(ContextCompat.getColorStateList(NewPartyActivity.this, R.color.material_grey));
                            confirm.setEnabled(false);
                        }
                    }
                }
            };

            holder.itemView.setOnClickListener(listener);
            holder.selectButton.setOnClickListener(listener);
            try {
                Glide.with(NewPartyActivity.this)
                        .load(String.format("https://graph.facebook.com/%s/picture?type=large", item.getString("id")))
                        .placeholder(R.drawable.com_facebook_profile_picture_blank_portrait)
                        .into(holder.profile);
                holder.number.setText(item.getString("number"));
                holder.name.setText(item.getString("name"));
            } catch (JSONException e) {
                Log.e("NewPartyActivity", Log.getStackTraceString(e));
            }
        }

        @Override
        public int getItemCount() {
            return users == null ? 0 : users.size();
        }

        public void fetchUsers() {
            Request request = new Request.Builder()
                    .url(String.format("%s/api/users/list/%s", Constants.SERVER_IP, userID))
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
                                JSONArray result = data.getJSONArray("result");
                                NewPartyAdapter.this.users = new ArrayList<>(result.length());
                                for (int i = 0; i < result.length(); i++) {
                                    NewPartyAdapter.this.users.add(result.getJSONObject(i));
                                }
                                notifyDataSetChanged();
                            } catch (JSONException e) {
                                Log.e("NewPartyActivity", Log.getStackTraceString(e));
                                Toast maketoast = Toast.makeText(getApplicationContext(), "Check server", Toast.LENGTH_SHORT);
                                maketoast.show();

                            }
                        }
                    });
                }
            });
        }

        public String generatePartyString() {
            String members = "[ \"" + userID + "\",";

            int count = 0;
            for (Integer index : selectedUsers) {
                try {
                    members += String.format("\"%s\"", users.get(index).getString("id"));
                    count += 1;
                    if (count == selectedUsers.size())
                        members += " ]";
                    else
                        members += ",";
                } catch (JSONException e) {
                    Log.e("NewPartyActivity", Log.getStackTraceString(e));
                }
            }

            return String.format("{ \"id\": \"%s\", \"members\": %s }", partyName.getText(), members);
        }
    }
}
