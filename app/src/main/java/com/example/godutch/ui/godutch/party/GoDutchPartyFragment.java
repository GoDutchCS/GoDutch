package com.example.godutch.ui.godutch.party;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.godutch.R;

public class GoDutchPartyFragment extends Fragment {
    private RecyclerView parties;
    private GoDutchPartyAdapter adapter;
    private String userID;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.godutch_party_list, container, false);
        userID = getActivity().getIntent().getStringExtra("USER_ID");
        parties = root.findViewById(R.id.party_list);
        parties.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        parties.setLayoutManager(layoutManager);
        adapter = new GoDutchPartyAdapter(this, userID);
        parties.setAdapter(adapter);

        return root;
    }
}
