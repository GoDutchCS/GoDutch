package com.example.godutch.ui.godutch.party;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.godutch.Constants;
import com.example.godutch.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PartyDetailActivity extends AppCompatActivity {
    private RecyclerView transactions, members;
    private String userID;
    private String partyID;
    private TransactionsAdapter transactionsAdapter;
    private MembersAdapter membersAdapter;
    private MaterialButton jeongsan;
    private MaterialTextView partyName;
    private OkHttpClient client = new OkHttpClient();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.godutch_party_detail);

        userID = getIntent().getStringExtra("USER_ID");
        partyID = getIntent().getStringExtra("PARTY_ID");
        partyName = findViewById(R.id.party_detail_title);
        partyName.setText(partyID);

        jeongsan = findViewById(R.id.party_detail_jeongsan);
//        confirm.setEnabled(false);
//        confirm.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.material_grey));

        RecyclerView.LayoutManager tLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);

        members = findViewById(R.id.party_detail_member_list);
        members.setHasFixedSize(true);
        members.setLayoutManager(mLayoutManager);
        membersAdapter = new MembersAdapter();
        members.setAdapter(membersAdapter);

        transactions = findViewById(R.id.party_detail_transactions);
        transactions.setHasFixedSize(true);
        transactions.setLayoutManager(tLayoutManager);
        transactionsAdapter = new TransactionsAdapter(userID);
        transactions.setAdapter(transactionsAdapter);
    }

    public class TransactionsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private String userID;
        private ArrayList<JSONObject> transactions;

        public class TransactionsViewHolder extends RecyclerView.ViewHolder {
            public MaterialTextView transactionDate;
            public RecyclerView transactionList;

            public TransactionsViewHolder(View itemView) {
                super(itemView);
                transactionDate = itemView.findViewById(R.id.godutch_transaction_date);
                transactionList = itemView.findViewById(R.id.godutch_transaction_list);
            }
        }

        public class AddTransactionViewHolder extends RecyclerView.ViewHolder {
            public AddTransactionViewHolder(View itemView) {
                super(itemView);
            }
        }

        public TransactionsAdapter(String userID) {
            this.userID = userID;
            transactions = new ArrayList<>();
            transactions.add(null);
            fetchTransactions();
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == 0) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.godutch_party_detail_transaction_new, parent, false);
                return new AddTransactionViewHolder(view);
            } else {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.godutch_party_detail_transaction_sublist, parent, false);
                return new TransactionsViewHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
            if (holder.getItemViewType() == 0) {
                AddTransactionViewHolder viewHolder = (AddTransactionViewHolder) holder;
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.v("foo", "bar");
                    }
                });
            } else {
                JSONObject dateTransactions = transactions.get(position);
            }
        }

        @Override
        public int getItemCount() {
            return transactions == null ? 0 : transactions.size();
        }

        public void fetchTransactions() {
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
//                            try {
//                                JSONObject data = new JSONObject(jsonString);
//                                JSONArray result = data.getJSONArray("result");
//                                TransactionsAdapter.this.users = new ArrayList<>(result.length());
//                                for (int i = 0; i < result.length(); i++) {
//                                    TransactionsAdapter.this.users.add(result.getJSONObject(i));
//                                }
//                                notifyDataSetChanged();
//                            } catch (JSONException e) {
//                                Log.e("ImageGalleryAdapter", Log.getStackTraceString(e));
//                            }
                        }
                    });
                }
            });
        }
    }

    public class MembersAdapter extends RecyclerView.Adapter<MembersAdapter.MembersViewHolder> {
        private JSONArray partyMembers;

        public class MembersViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;

            public MembersViewHolder(View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.party_detail_member_profile);
            }
        }

        public MembersAdapter() {
            fetchMembers();
        }

        @NonNull
        @Override
        public MembersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View photoView = LayoutInflater.from(parent.getContext()).inflate(R.layout.godutch_party_detail_member, parent, false);
            return new MembersViewHolder(photoView);
        }

        @Override
        public void onBindViewHolder(@NonNull final MembersAdapter.MembersViewHolder holder, final int position) {
            String memberID = null;
            try {
                memberID = partyMembers.getString(position);
            } catch (JSONException e) {
                Log.e("GoDutchPartyRowAdapter", Log.getStackTraceString(e));
            }
            Glide.with(PartyDetailActivity.this)
                    .load(String.format("https://graph.facebook.com/%s/picture?type=large", memberID))
                    .placeholder(R.drawable.com_facebook_profile_picture_blank_portrait)
                    .into(holder.imageView);
        }

        @Override
        public int getItemCount() {
            return partyMembers == null ? 0 : partyMembers.length();
        }

        private void fetchMembers() {
            Request request = new Request.Builder()
                    .url(String.format("%s/api/parties/single/%s", Constants.SERVER_IP, partyID))
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    call.cancel();
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    final String jsonString = response.body().string();
                    Log.v("Foo", jsonString);
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        public void run() {
                            try {
                                JSONObject data = new JSONObject(jsonString);
                                MembersAdapter.this.partyMembers = data.getJSONArray("members");
                                notifyDataSetChanged();
                            } catch (JSONException e) {
                                Log.e("PartyDetailActivity", Log.getStackTraceString(e));
                            }
                        }
                    });
                }
            });
        }
    }
}

