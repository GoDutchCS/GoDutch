package com.example.godutch.ui.godutch.party;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.godutch.Constants;
import com.example.godutch.R;
import com.google.android.material.textview.MaterialTextView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PartyResolvedActivity extends AppCompatActivity {
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private String partyID;
    private RecyclerView cards;
    private ResolvedAdapter resolvedAdapter;
    private JSONObject namesMap;
    private OkHttpClient client = new OkHttpClient();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.godutch_party_resolve);

        partyID = getIntent().getStringExtra("PARTY_ID");
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        cards = findViewById(R.id.godutch_resolved_list);
        cards.setHasFixedSize(true);
        cards.setLayoutManager(layoutManager);
        resolvedAdapter = new ResolvedAdapter();
        cards.setAdapter(resolvedAdapter);
    }

    public String getName(String id) throws JSONException {
        return namesMap.getString(id);
    }

    public class ResolvedAdapter extends RecyclerView.Adapter<ResolvedAdapter.ResolvedViewHolder> {
        private ArrayList<String> usernames;
        private JSONObject resolvedInformation;

        public class ResolvedViewHolder extends RecyclerView.ViewHolder {
            public MaterialTextView personName;
            public RecyclerView peopleList;
            public CardListAdapter adapter;
            public ImageView profile;

            public ResolvedViewHolder(View itemView) {
                super(itemView);
                personName = itemView.findViewById(R.id.godutch_resolve_id);
                peopleList = itemView.findViewById(R.id.godutch_resolve_people);
                profile = itemView.findViewById(R.id.godutch_resolve_member_photo);
            }
        }

        public ResolvedAdapter() {
            fetchResolvedResult();
        }

        @NonNull
        @Override
        public ResolvedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.godutch_party_resolve_card, parent, false);
            return new ResolvedViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final ResolvedViewHolder holder, final int position) {
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(PartyResolvedActivity.this, LinearLayoutManager.VERTICAL, false);
            JSONObject personInformation = null;
            try {
                personInformation = resolvedInformation.getJSONObject(usernames.get(position));
                Glide.with(PartyResolvedActivity.this)
                        .load(String.format("https://graph.facebook.com/%s/picture?type=large", usernames.get(position)))
                        .placeholder(R.drawable.com_facebook_profile_picture_blank_portrait)
                        .into(holder.profile);
                holder.personName.setText(getName(usernames.get(position)));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            holder.peopleList.setLayoutManager(layoutManager);
            holder.peopleList.setHasFixedSize(true);
            holder.adapter = new CardListAdapter(personInformation);
            holder.peopleList.setAdapter(holder.adapter);
        }

        @Override
        public int getItemCount() {
            return usernames == null ? 0 : usernames.size();
        }

        private void fetchResolvedResult() {
            Request request = new Request.Builder()
                    .url(String.format("%s/api/parties/%s/resolve", Constants.SERVER_IP, partyID))
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
                                JSONObject resultObject = new JSONObject(jsonString);
                                namesMap = resultObject.getJSONObject("namesMap");
                                ResolvedAdapter.this.resolvedInformation = resultObject.getJSONObject("result");
                                ResolvedAdapter.this.usernames = new ArrayList<String>();
                                Iterator<String> keys = ResolvedAdapter.this.resolvedInformation.keys();
                                while (keys.hasNext()) {
                                    String key = keys.next();
                                    ResolvedAdapter.this.usernames.add(key);
                                }
                                notifyDataSetChanged();
                            } catch (JSONException e) {
                                Log.e("PartyResolvedActivity", Log.getStackTraceString(e));
                            }
                        }
                    });
                }
            });
        }
    }

    public class CardListAdapter extends RecyclerView.Adapter<CardListAdapter.CardListViewHolder> {
        private ArrayList<String> thiefIDs;
        private JSONObject thiefInformation;

        public class CardListViewHolder extends RecyclerView.ViewHolder {
            public MaterialTextView memberName;
            public MaterialTextView memberAmount;
            public ImageView profile;

            public CardListViewHolder(View itemView) {
                super(itemView);
                profile = itemView.findViewById(R.id.godutch_resolve_card_member_photo);
                memberName = itemView.findViewById(R.id.godutch_resolve_card_member_name);
                memberAmount = itemView.findViewById(R.id.godutch_resolve_card_member_money);
            }
        }

        public CardListAdapter(JSONObject thiefInformation) {
            this.thiefInformation = thiefInformation;
            thiefIDs = new ArrayList<String>();
            Iterator<String> keys = thiefInformation.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                thiefIDs.add(key);
            }
        }

        @NonNull
        @Override
        public CardListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.godutch_party_resolve_card_member, parent, false);
            return new CardListViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final CardListViewHolder holder, final int position) {
            try {
                holder.memberName.setText(getName(thiefIDs.get(position)));
                holder.memberAmount.setText(thiefInformation.getInt(thiefIDs.get(position)) + "₩");
                Glide.with(PartyResolvedActivity.this)
                        .load(String.format("https://graph.facebook.com/%s/picture?type=large", thiefIDs.get(position)))
                        .placeholder(R.drawable.com_facebook_profile_picture_blank_portrait)
                        .into(holder.profile);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return thiefIDs == null ? 0 : thiefIDs.size();
        }
    }
}
