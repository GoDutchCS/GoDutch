package com.example.godutch.ui.register;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatDialogFragment;

import com.example.godutch.R;

import org.json.JSONException;

public class RegisterDialog extends AppCompatDialogFragment {
    private EditText accountNumber;
    private RegisterDialogListener listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.register_dialog, null);
        builder.setView(view)
                .setTitle("Register")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setPositiveButton("Register", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String account = accountNumber.getText().toString();
                        try {
                            listener.register(account);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
        accountNumber = view.findViewById(R.id.account_number);
        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (RegisterDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement RegisterDialogListener!");
        }
    }

    public interface RegisterDialogListener {
        void register(String accountNumber) throws JSONException;
    }
}
