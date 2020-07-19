package com.example.godutch.ui.godutch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.godutch.R;
import com.google.android.material.button.MaterialButton;


public class GoDutchFragment extends Fragment {
    private GoDutchViewModel goDutchViewModel;
    private MaterialButton partyGenButton;
    private Bundle bundle;
    private static int REQUEST_NEW_PARTY_DIALOG = 1;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        goDutchViewModel = new ViewModelProvider(this).get(GoDutchViewModel.class);
        View root = inflater.inflate(R.layout.fragment_godutch, container, false);
        partyGenButton = root.findViewById(R.id.new_party_button);
        partyGenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchNewPartyDialog();
            }
        });
        return root;
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

    public void launchNewPartyDialog() {
        Intent intent = new Intent(getActivity(), NewPartyActivity.class);
        intent.putExtra("USER_ID", getActivity().getIntent().getStringExtra("USER_ID"));
        startActivityForResult(intent, REQUEST_NEW_PARTY_DIALOG);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_NEW_PARTY_DIALOG && resultCode == Activity.RESULT_OK)  {
        }
    }
}