package com.example.godutch.ui.godutch.party;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.godutch.R;

import org.json.JSONArray;
import org.json.JSONException;

import okhttp3.OkHttpClient;

public class GoDutchPartyRowAdapter extends RecyclerView.Adapter<GoDutchPartyRowAdapter.GoDutchPartyRowViewHolder> {
    private OkHttpClient client = new OkHttpClient();
    private String userID;
    private JSONArray partyMembers;
    private Context context;

    public class GoDutchPartyRowViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public GoDutchPartyRowViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.party_member_profile);
        }
    }

    public GoDutchPartyRowAdapter(Context context, String userID, JSONArray partyMembers) {
        this.context = context;
        this.userID = userID;
        this.partyMembers = partyMembers;
    }

    @NonNull
    @Override
    public GoDutchPartyRowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View photoView = LayoutInflater.from(parent.getContext()).inflate(R.layout.godutch_party_member, parent, false);
        return new GoDutchPartyRowViewHolder(photoView);
    }

    @Override
    public void onBindViewHolder(@NonNull final GoDutchPartyRowViewHolder holder, final int position) {
        String memberID = null;
        try {
            memberID = partyMembers.getString(position);
        } catch (JSONException e) {
            Log.e("GoDutchPartyRowAdapter", Log.getStackTraceString(e));
        }
        Log.v("Foo", String.format("graph.facebook.com/%s/picture?type=small", memberID));
        Glide.with(this.context)
                .load(String.format("https://graph.facebook.com/%s/picture?type=small", memberID))
                .placeholder(R.drawable.com_facebook_profile_picture_blank_portrait)
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return partyMembers == null ? 0 : partyMembers.length();
    }

//    public void fetchPartyMembers() {
//        Request request = new Request.Builder()
//                .url(String.format("%s/api/parties/list/%s", Constants.SERVER_IP, userID))
//                .build();
//
//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(@NotNull Call call, @NotNull IOException e) {
//                call.cancel();
//            }
//
//            @Override
//            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
//                final String jsonString = response.body().string();
//                new Handler(Looper.getMainLooper()).post(new Runnable() {
//                    public void run() {
//                        try {
//                            JSONArray result = new JSONArray(jsonString);
//                            GoDutchPartyRowAdapter.this.partyMembers = new ArrayList<>(result.length());
//                            for (int i = 0; i < result.length(); i++) {
//                                GoDutchPartyRowAdapter.this.partyMembers.add(result.getJSONObject(i));
//                            }
//                            notifyDataSetChanged();
//                        } catch (JSONException e) {
//                            Log.e("GoDutchPartyRowAdapter", Log.getStackTraceString(e));
//                        }
//                    }
//                });
//            }
//        });
//    }

}
