package com.example.godutch.ui.godutch;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.godutch.Constants;
import com.example.godutch.MainActivity;
import com.example.godutch.R;
import com.example.godutch.ui.godutch.party.GoDutchPartyFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class GoDutchMainFragment extends Fragment {
    private static int REQUEST_NEW_PARTY_ACTIVITY = 1;
    private static int REQUEST_TOSS_DEPOSIT = 2;
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private GoDutchViewModel goDutchViewModel;
    private MaterialButton partyGenButton;
    private MaterialTextView mainListLabel;
    private RecyclerView mainList;
    private MainListAdapter adapter;
    private OkHttpClient client = new OkHttpClient();
    private String userID;
    private JSONObject namesMap;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        goDutchViewModel = new ViewModelProvider(this).get(GoDutchViewModel.class);
        View root = inflater.inflate(R.layout.fragment_godutch, container, false);
        userID = getActivity().getIntent().getStringExtra("USER_ID");
        partyGenButton = root.findViewById(R.id.new_party_button);
        partyGenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchNewPartyActivity();
            }
        });

        mainListLabel = root.findViewById(R.id.godutch_main_header);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mainList = root.findViewById(R.id.godutch_main_list);
        mainList.setHasFixedSize(true);
        mainList.setLayoutManager(layoutManager);
        adapter = new MainListAdapter();
        mainList.setAdapter(adapter);

        return root;
    }

    public MainListAdapter getAdapter() {
        return this.adapter;
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

    public void launchNewPartyActivity() {
        Intent intent = new Intent(getActivity(), NewPartyActivity.class);
        intent.putExtra("USER_ID", getActivity().getIntent().getStringExtra("USER_ID"));
        startActivityForResult(intent, REQUEST_NEW_PARTY_ACTIVITY);
    }

    public void startTransaction(final JSONObject oweInfo) {
        Request request = null;
        try {
            request = new Request.Builder()
                    .url(String.format("%s/api/users/single/%s", Constants.SERVER_IP, oweInfo.getString("to")))
                    .build();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                call.cancel();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String jsonString = response.body().string();
                Log.v("Foo", jsonString);
                try {
                    JSONObject result = new JSONObject(jsonString);
                    Log.v("Foo result", result.toString());
                    tossRequest(oweInfo.getString("to"), result.getString("bank_type"), result.getString("account_number"), oweInfo.getInt("amount"));
                    sendTransactionFinished(oweInfo);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void sendTransactionFinished(JSONObject oweInfo) throws JSONException {
        Log.v("Foo oweinfo", oweInfo.toString());
        String postBody = "{\n" +
                "\"party_id\": " + "\"" + oweInfo.getString("id") + "\",\n" +
                "\"user_id\": " + "\"" + userID + "\",\n" +
                "\"to\": " + "\"" + oweInfo.getString("to") + "\"\n}";

        RequestBody body = RequestBody.create(postBody, JSON);
        Request request = new Request.Builder()
                .url(String.format("%s/api/parties/transactions/complete", Constants.SERVER_IP))
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
                final Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        adapter.fetchOweResult();
                    }
                }, 8000);
            }
        });
    }

    public void tossRequest(String toID, String bankName, String bankAccountNo, int amount) {
        String postBody = "{\n" +
                "\"apiKey\": " + "\"846b23f68e2b49e3ab72b6b5d32f1671\",\n" +
                "\"bankName\": " + "\"" + bankName + "\",\n" +
                "\"bankAccountNo\": " + "\"" + bankAccountNo + "\",\n" +
                "\"amount\": " + amount + ",\n" +
                "\"message\": " + "\"입금 by GoDutch\"" +
                "\n}";

        RequestBody body = RequestBody.create(postBody, JSON);
        Request request = new Request.Builder()
//                .url("https://toss.im/transfer-web/linkgen-api/link")
                .url("https://private-anon-a38be9b3eb-tossbutton.apiary-proxy.com/transfer-web/linkgen-api/link") // proxy
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
                Log.v("Foo", jsonString);
                try {
                    JSONObject result = new JSONObject(jsonString);
                    if (result.getString("resultType").equals("SUCCESS")) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(result.getJSONObject("success").getString("scheme")));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                            startActivityForResult(intent, REQUEST_TOSS_DEPOSIT);
                        } else {
                            Toast.makeText(getActivity(), "No Activity Found", Toast.LENGTH_LONG).show();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_NEW_PARTY_ACTIVITY && resultCode == Activity.RESULT_OK)  {
            String postBody = data.getStringExtra("party");
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
                            ViewPager2 viewPager = ((MainActivity)getActivity()).getViewPager();
                            GoDutchViewPagerAdapter goDutchViewPagerAdapter = (GoDutchViewPagerAdapter)viewPager.getAdapter();
                            ((GoDutchPartyFragment)(goDutchViewPagerAdapter.getFragment(1))).getAdapter().fetchParties();
                            viewPager.setCurrentItem(1);
                        }
                    });
                }
            });
        } else if (requestCode == REQUEST_TOSS_DEPOSIT) {
        }
    }

    public class MainListAdapter extends RecyclerView.Adapter<MainListAdapter.MainListViewHolder> {
        private JSONArray owePeople;

        public class MainListViewHolder extends RecyclerView.ViewHolder {
            ImageView profile;
            MaterialTextView memberName;
            MaterialTextView partyName;
            MaterialTextView amount;
            MaterialButton cheongsanButton;

            public MainListViewHolder(View itemView) {
                super(itemView);
                partyName = itemView.findViewById(R.id.godutch_owe_party_name);
                memberName = itemView.findViewById(R.id.godutch_owe_member_name);
                profile = itemView.findViewById(R.id.godutch_owe_member_photo);
                amount = itemView.findViewById(R.id.godutch_owe_member_amount);
                cheongsanButton = itemView.findViewById(R.id.godutch_owe_member_cheongsan);
            }
        }

        public MainListAdapter() {
            fetchOweResult();
        }

        @NonNull
        @Override
        public MainListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.godutch_main_owe_row, parent, false);
            return new MainListViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final MainListViewHolder holder, final int position) {
            try {
                final JSONObject oweInfo = owePeople.getJSONObject(position);
                holder.partyName.setText(oweInfo.getString("id"));
                holder.memberName.setText(namesMap.getString(oweInfo.getString("to")));
                holder.amount.setText(oweInfo.getString("amount") + "₩");
                Glide.with(getActivity())
                        .load(String.format("https://graph.facebook.com/%s/picture?type=large", oweInfo.getString("to")))
                        .placeholder(R.drawable.com_facebook_profile_picture_blank_portrait)
                        .into(holder.profile);
                holder.cheongsanButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startTransaction(oweInfo);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return owePeople == null ? 0 : owePeople.length();
        }

        public void fetchOweResult() {
            Request request = new Request.Builder()
                    .url(String.format("%s/api/users/peopleiowe/%s", Constants.SERVER_IP, userID))
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
                                owePeople = resultObject.getJSONArray("result");
                                mainListLabel.setText(String.format("%s의 빚더미", namesMap.getString(userID)));
                                notifyDataSetChanged();
                            } catch (JSONException e) {
                                Log.e("GoDutchMainFragment", Log.getStackTraceString(e));
                            }
                        }
                    });
                }
            });
        }
    }
}
