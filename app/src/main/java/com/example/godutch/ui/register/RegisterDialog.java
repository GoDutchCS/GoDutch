package com.example.godutch.ui.register;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.appcompat.app.AppCompatDialogFragment;

import com.example.godutch.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;

public class RegisterDialog extends AppCompatDialogFragment {
    private TextInputEditText accountNumber;
    private TextInputEditText phoneNumber;
    private RegisterDialogListener listener;
    private MaterialButton registerButton;
    private AutoCompleteTextView bankType;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.register_dialog, null);
        builder.setView(view)
                .setTitle("Register");
        bankType = view.findViewById(R.id.bank_type);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.bank_type_dropdown, new String[]{ "우리", "신한", "하나", "농협", "카카오뱅크" });
        bankType.setAdapter(adapter);

        accountNumber = view.findViewById(R.id.account_number);
        phoneNumber = view.findViewById(R.id.phone_number);
        registerButton = view.findViewById(R.id.register_button);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String account = accountNumber.getText().toString();
                String phone = phoneNumber.getText().toString();
                String bank = bankType.getText().toString();

                try {
                    listener.register(bank, account, phone);
                } catch (JSONException e) {
                    Log.e("RegisterDialog", Log.getStackTraceString(e));
                }
            }
        });
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
        void register(String bankType, String accountNumber, String phoneNumber) throws JSONException;
    }
}
