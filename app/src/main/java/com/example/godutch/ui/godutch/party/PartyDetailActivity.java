package com.example.godutch.ui.godutch.party;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.godutch.Constants;
import com.example.godutch.R;

import com.example.godutch.ui.godutch.transaction.NewTransactionDialog;
import com.example.godutch.ui.register.RegisterDialog;
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
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import static java.util.Collections.addAll;


public class PartyDetailActivity extends AppCompatActivity implements NewTransactionDialog.NewTransactionDialogListener {
    private static final int REQUEST_PARTY_RESOLVED_ACTIVITY = 1;
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private RecyclerView transactions, members;
    private String userID;
    private String partyID;
    private TransactionsAdapter transactionsAdapter;
    private MembersAdapter membersAdapter;
    private MaterialButton jeongsan;
    private MaterialTextView partyName;
    private OkHttpClient client = new OkHttpClient();
    private LinearLayout show_members;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.godutch_party_detail);

        userID = getIntent().getStringExtra("USER_ID");
        partyID = getIntent().getStringExtra("PARTY_ID");
        partyName = findViewById(R.id.party_detail_title);
        partyName.setText(partyID);

        jeongsan = findViewById(R.id.party_detail_jeongsan);
        jeongsan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PartyDetailActivity.this, PartyResolvedActivity.class);
                intent.putExtra("PARTY_ID", partyID);
                startActivityForResult(intent, REQUEST_PARTY_RESOLVED_ACTIVITY);
            }
        });

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

        show_members = findViewById(R.id.show_members_here);
    }

    public void launchNewTransactionDialog() {
        NewTransactionDialog newTransactionDialog = new NewTransactionDialog();
        Bundle bundle = new Bundle();
        bundle.putString("PARTY_MEMBERS", this.membersAdapter.getPartyMembers());
        newTransactionDialog.setArguments(bundle);
        newTransactionDialog.show(getSupportFragmentManager(), "New Transaction");
    }

    @Override
    public void createTransaction(String title, String buyer, String total, String method, String participants) {
        String postBody = "{\n" +
                "\"title\": " + "\"" + title + "\",\n" +
                "\"buyer\": " + "\"" + buyer + "\",\n" +
                "\"total\": " + total + ",\n" +
                "\"method\": " + "\"" + method + "\",\n" +
                "\"participants\": " + participants + "\n}";
        RequestBody body = RequestBody.create(postBody, JSON);
        Request request = new Request.Builder()
                .url(String.format("%s/api/parties/%s/transactions/add", Constants.SERVER_IP, partyID))
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
                if (jsonString.indexOf("true") >= 0) {
                    PartyDetailActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(PartyDetailActivity.this, "Transaction Added", Toast.LENGTH_SHORT).show();
                        }
                    });
                    transactionsAdapter.fetchTransactions();
                }
            }
        });
    }

    public class TransactionsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private String userID;
        private ArrayList<JSONObject> transactions;

        public class TransactionsViewHolder extends RecyclerView.ViewHolder {
            public MaterialTextView transactionDate;
            public RecyclerView transactionList;
            public DateTransactionsAdapter adapter;

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
            try {
                transactions.add(new JSONObject("{}"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
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
                        launchNewTransactionDialog();
                    }
                });
            } else {
                TransactionsViewHolder viewHolder = (TransactionsViewHolder) holder;
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(PartyDetailActivity.this, LinearLayoutManager.VERTICAL, false);

                JSONArray dateTransactions = null;
                String date = null;
                try {
                    dateTransactions = transactions.get(position).getJSONArray("transactions");
                    date = transactions.get(position).getString("_id");
                } catch (JSONException e) {
                    Log.e("PartyDetailActivity", Log.getStackTraceString(e));
                }
                viewHolder.transactionDate.setText(date);
                viewHolder.transactionList.setLayoutManager(layoutManager);
                viewHolder.transactionList.setHasFixedSize(true);
                viewHolder.adapter = new DateTransactionsAdapter(dateTransactions);
                viewHolder.transactionList.setAdapter(viewHolder.adapter);
            }
        }

        @Override
        public int getItemCount() {
            return transactions == null ? 0 : transactions.size();
        }

        public void fetchTransactions() {
            Request request = new Request.Builder()
                    .url(String.format("%s/api/parties/transactions/%s", Constants.SERVER_IP, partyID))
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
                                if (TransactionsAdapter.this.transactions.size() != 1) {
                                    TransactionsAdapter.this.transactions.clear();
                                    TransactionsAdapter.this.transactions.add(null);
                                }
                                JSONArray array = new JSONArray(jsonString);
                                for (int i = 0; i < array.length(); i++) {
                                    TransactionsAdapter.this.transactions.add(array.getJSONObject(i));
                                }
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

    public class DateTransactionsAdapter extends RecyclerView.Adapter<DateTransactionsAdapter.DateTransactionsViewHolder> {
        private JSONArray dateTransactions;

        public class DateTransactionsViewHolder extends RecyclerView.ViewHolder {
            MaterialTextView name;
            MaterialTextView amount;
            MaterialTextView time;

            public DateTransactionsViewHolder(View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.godutch_transaction_name);
                amount = itemView.findViewById(R.id.godutch_transaction_amount);
                time = itemView.findViewById(R.id.godutch_transaction_time);
            }
        }

        public DateTransactionsAdapter(JSONArray dateTransactions) {
            this.dateTransactions = dateTransactions;
        }

        @NonNull
        @Override
        public DateTransactionsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View dateTransactionsView = LayoutInflater.from(parent.getContext()).inflate(R.layout.godutch_party_detail_transaction_item, parent, false);
            return new DateTransactionsViewHolder(dateTransactionsView);
        }

        @Override
        public void onBindViewHolder(@NonNull DateTransactionsViewHolder holder, int position) {
            try {
                JSONObject object = dateTransactions.getJSONObject(position);
                holder.name.setText(object.getString("title"));
                holder.amount.setText(object.getString("total") + "원");
                holder.time.setText(object.getString("time").substring(0, 5));
            } catch (JSONException e) {
                Log.e("PartyDetailActivity", Log.getStackTraceString(e));
            }
        }

        @Override
        public int getItemCount() {
            return dateTransactions == null ? 0 : dateTransactions.length();
        }
    }

    public class MembersAdapter extends RecyclerView.Adapter<MembersAdapter.MembersViewHolder> {
        private JSONArray partyMembers;
        private JSONObject namesMap;

        public class MembersViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            MaterialTextView textView;

            public MembersViewHolder(View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.party_detail_member_profile);
                textView = itemView.findViewById(R.id.party_detail_member_name);
            }
        }

        public MembersAdapter() {
            fetchMembers();
        }

        public String getPartyMembers() {
            return this.partyMembers.toString();
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
                Glide.with(PartyDetailActivity.this)
                        .load(String.format("https://graph.facebook.com/%s/picture?type=large", memberID))
                        .placeholder(R.drawable.com_facebook_profile_picture_blank_portrait)
                        .into(holder.imageView);
                holder.textView.setText(namesMap.getString(memberID));
            } catch (JSONException e) {
                Log.e("PartyDetailActivity", Log.getStackTraceString(e));
            }
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
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        public void run() {
                            try {
                                JSONObject data = new JSONObject(jsonString);
                                MembersAdapter.this.partyMembers = data.getJSONArray("members");
                                namesMap = data.getJSONObject("namesMap");
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

    @Override
    public void onResume() {
        super.onResume();
        getSupportActionBar().hide();
    }

    @Override
    public void onStop() {
        super.onStop();
        getSupportActionBar().show();
    }
}

