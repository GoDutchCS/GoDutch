package com.example.godutch.ui.godutch.party;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.godutch.Constants;
import com.example.godutch.R;
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

public class GoDutchPartyAdapter extends RecyclerView.Adapter<GoDutchPartyAdapter.GoDutchPartyViewHolder> {
    private static int REQUEST_PARTY_DETAIL_ACTIVITY = 1;
    private OkHttpClient client = new OkHttpClient();
    private String userID;
    private ArrayList<JSONObject> parties;
    private GoDutchPartyFragment fragment;

    public class GoDutchPartyViewHolder extends RecyclerView.ViewHolder {
        public RecyclerView partyMembers;
        public MaterialTextView partyName;

        public GoDutchPartyViewHolder(View itemView) {
            super(itemView);
            partyMembers = itemView.findViewById(R.id.party_members_list);
            partyName = itemView.findViewById(R.id.party_id);
        }
    }

    public GoDutchPartyAdapter(GoDutchPartyFragment fragment, String userID) {
        this.fragment = fragment;
        this.userID = userID;
        fetchParties();
    }

    @NonNull
    @Override
    public GoDutchPartyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View rowView = LayoutInflater.from(parent.getContext()).inflate(R.layout.godutch_party_row, parent, false);
        return new GoDutchPartyViewHolder(rowView);
    }

    @Override
    public void onBindViewHolder(@NonNull final GoDutchPartyViewHolder holder, final int position) {
        JSONArray members = null;
        String partyID = "";
        try {
            members = parties.get(position).getJSONArray("members");
            partyID = parties.get(position).getString("id");
        } catch (JSONException e) {
            Log.e("GoDutchPartyAdapter", Log.getStackTraceString(e));
        }

        holder.partyName.setText(partyID);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(fragment.getContext(), LinearLayoutManager.HORIZONTAL, false);
        holder.partyMembers.setLayoutManager(layoutManager);
        holder.partyMembers.setHasFixedSize(true);

        GoDutchPartyRowAdapter adapter = new GoDutchPartyRowAdapter(fragment.getContext(), userID, members);
        holder.partyMembers.setAdapter(adapter);

        final String intentPartyID = partyID;
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(fragment.getActivity(), PartyDetailActivity.class);
                intent.putExtra("USER_ID", fragment.getActivity().getIntent().getStringExtra("USER_ID"));
                intent.putExtra("PARTY_ID", intentPartyID);
                fragment.startActivityForResult(intent, REQUEST_PARTY_DETAIL_ACTIVITY);
            }
        });
    }

    @Override
    public int getItemCount() {
        return parties == null ? 0 : parties.size();
    }

    public void fetchParties() {
        Request request = new Request.Builder()
                .url(String.format("%s/api/parties/list/%s", Constants.SERVER_IP, userID))
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
                            JSONArray result = new JSONArray(jsonString);
                            GoDutchPartyAdapter.this.parties = new ArrayList<>(result.length());
                            for (int i = 0; i < result.length(); i++) {
                                GoDutchPartyAdapter.this.parties.add(result.getJSONObject(i));
                            }
                            notifyDataSetChanged();
                        } catch (JSONException e) {
                            Log.e("GoDutchPartyAdapter", Log.getStackTraceString(e));
                        }
                    }
                });
            }
        });
    }
}
