package com.example.godutch.ui.godutch.transaction;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.godutch.Constants;
import com.example.godutch.R;
import com.example.godutch.ui.godutch.NewPartyActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashSet;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NewTransactionDialog extends AppCompatDialogFragment {
    private TextInputEditText title;
    private TextInputEditText amount;
    private AutoCompleteTextView buyer;
    private MaterialButton createTransaction;
    private RecyclerView members;
    private NewTransactionDialogListener listener;
    private OkHttpClient client = new OkHttpClient();
    private NewTransactionDialogAdapter adapter;
    private RadioGroup paymentMethodGroup;
    private String[] memberNames;
    private View root;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        root = inflater.inflate(R.layout.new_transaction_dialog, null);
        builder.setView(root).setTitle("New Transaction");

        Bundle bundle = getArguments();
        String partyMembersString = bundle.getString("PARTY_MEMBERS", "[]");

        title = root.findViewById(R.id.godutch_new_transaction_title);
        amount = root.findViewById(R.id.godutch_new_transaction_amount);

        buyer = root.findViewById(R.id.godutch_members_dropdown);
        buyer.setEnabled(false);

        paymentMethodGroup = root.findViewById(R.id.godutch_new_transaction_radio_group);

        members = root.findViewById(R.id.godutch_new_transaction_members);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        members.setLayoutManager(layoutManager);
        members.setHasFixedSize(true);
        adapter = new NewTransactionDialogAdapter(partyMembersString);
        members.setAdapter(adapter);

        createTransaction = root.findViewById(R.id.create_transaction_button);
        createTransaction.setEnabled(false);
        createTransaction.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.material_grey));
        createTransaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    listener.createTransaction(
                            title.getText().toString(),
                            adapter.getPartyMembers().getJSONObject(getIndexOf(buyer.getText().toString())).getString("id"),
                            amount.getText().toString(),
                            getSelectedRadioText(),
                            generateNewTransactionString()
                    );
                    NewTransactionDialog.this.dismissAllowingStateLoss();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        return builder.create();
    }

    public int getIndexOf(String name) {
        if (memberNames == null)
            return -1;

        for (int i = 0; i < memberNames.length; i++) {
            if (memberNames[i].equals(name))
                return i;
        }

        return -1;
    }

    public interface NewTransactionDialogListener {
        void createTransaction(String title, String buyer, String amount, String method, String body);
    }

    public String generateNewTransactionString() {
        HashSet<Integer> set = adapter.getSelectedUsers();
        JSONArray partyMembers = adapter.getPartyMembers();

        String body = "[ ";
        int count = 0;
        for (Integer i : set) {
            try {
                body += String.format("\"%s\"", partyMembers.getJSONObject(i).getString("id"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            count += 1;

            if (count == set.size())
                body += " ]";
            else
                body += ", ";
        }

        return body;
    }

    public String getSelectedRadioText() {
        int selectedID = paymentMethodGroup.getCheckedRadioButtonId();
        RadioButton radioButton = (RadioButton)(root.findViewById(selectedID));
        return radioButton.getText().toString();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (NewTransactionDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement RegisterDialogListener!");
        }
    }

    public class NewTransactionDialogAdapter extends RecyclerView.Adapter<NewTransactionDialogAdapter.NewTransactionDialogViewHolder> {
        private JSONArray partyMembers;
        private HashSet<Integer> selectedUsers;

        public class NewTransactionDialogViewHolder extends RecyclerView.ViewHolder {
            public ImageView profile;
            public MaterialTextView name;
            public MaterialCheckBox checkBox;
            public boolean selected = false;

            public NewTransactionDialogViewHolder(View itemView) {
                super(itemView);
                profile = itemView.findViewById(R.id.member_photo);
                name = itemView.findViewById(R.id.member_name);
                checkBox = itemView.findViewById(R.id.member_checkbox);
            }
        }

        public NewTransactionDialogAdapter(String partyMembersString) {
            try {
                fetchUserInformations(new JSONArray(partyMembersString));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            selectedUsers = new HashSet<>();
        }

        @NonNull
        @Override
        public NewTransactionDialogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View photoView = LayoutInflater.from(parent.getContext()).inflate(R.layout.godutch_party_member_row, parent, false);
            return new NewTransactionDialogViewHolder(photoView);
        }

        @Override
        public void onBindViewHolder(@NonNull final NewTransactionDialogViewHolder holder, final int position) {
            JSONObject item = null;
            try {
                item = partyMembers.getJSONObject(position);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    holder.selected = !holder.selected;
                    if (holder.selected) {
                        selectedUsers.add(position);
                        if (selectedUsers.size() == 1) {
                            createTransaction.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.material_blue));
                            createTransaction.setEnabled(true);
                        }
                    } else {
                        selectedUsers.remove(position);
                        if (selectedUsers.size() == 0) {
                            createTransaction.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.material_grey));
                            createTransaction.setEnabled(false);
                        }
                    }
                }
            };

            holder.checkBox.setOnClickListener(listener);

            try {
                Glide.with(getActivity())
                        .load(String.format("https://graph.facebook.com/%s/picture?type=large", item.getString("id")))
                        .placeholder(R.drawable.com_facebook_profile_picture_blank_portrait)
                        .into(holder.profile);
                holder.name.setText(item.getString("first_name"));
            } catch (JSONException e) {
                Log.e("NewPartyActivity", Log.getStackTraceString(e));
            }
        }

        private void fetchUserInformations(JSONArray memberIDs) {
            String queryString = "";
            for (int i = 0; i < memberIDs.length(); i++) {
                try {
                    queryString += "users=" + memberIDs.get(i);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (i != memberIDs.length() - 1)
                    queryString += "&";
            }

            Request request = new Request.Builder()
                    .url(String.format("%s/api/users/multiple?%s", Constants.SERVER_IP, queryString))
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    call.cancel();
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    final String jsonString = response.body().string();
                    try {
                        partyMembers = new JSONArray(jsonString);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            memberNames = new String[partyMembers.length()];
                            for (int i = 0; i < partyMembers.length(); i++) {
                                JSONObject element = null;
                                try {
                                    element = partyMembers.getJSONObject(i);
                                    memberNames[i] = element.getString("first_name");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            buyer.setAdapter(new ArrayAdapter<>(getContext(), R.layout.member_dropdown_item, memberNames));
                        }
                    });
                }
            });
        }

        @Override
        public int getItemCount() {
            return partyMembers == null ? 0 : partyMembers.length();
        }

        public HashSet<Integer> getSelectedUsers() {
            return this.selectedUsers;
        }

        public JSONArray getPartyMembers() {
            return this.partyMembers;
        }
    }
}
