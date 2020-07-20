package com.example.godutch.ui.godutch.transaction;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.godutch.R;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;

public class NewTransactionDialog extends AppCompatDialogFragment {
    private TextInputEditText title;
    private TextInputEditText buyer;
    private RecyclerView members;
    private NewTransactionDialogListener listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.new_transaction_dialog, null);
        builder.setView(view).setTitle("New Transaction");
        title = view.findViewById(R.id.godutch_new_transaction_title);
        buyer = view.findViewById(R.id.godutch_new_transaction_buyer);
        members = view.findViewById(R.id.godutch_new_transaction_members);
        return builder.create();
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

    public interface NewTransactionDialogListener {
        void register(String accountNumber, String phoneNumber) throws JSONException;
    }
}
