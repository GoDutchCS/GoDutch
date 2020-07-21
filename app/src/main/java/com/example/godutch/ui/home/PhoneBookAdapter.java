package com.example.godutch.ui.home;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;


import com.example.godutch.R;

import java.util.ArrayList;

public class PhoneBookAdapter extends RecyclerView.Adapter<PhoneBookAdapter.PhoneBookViewHolder> {
    private ArrayList<JsonData> listViewItemList;
    private Context context;

    public PhoneBookAdapter(ArrayList<JsonData> items, Context context) {
        this.listViewItemList = items;
        this.context = context;
    }
    public ArrayList<JsonData> getListViewItemList() {
        return listViewItemList;
    }
    public class PhoneBookViewHolder extends RecyclerView.ViewHolder {
        private ImageView photo;
        private TextView name;
        private TextView number;
        private TextView email;
        //private ImageButton callButton;
        //private ImageButton smsButton;
        //private View expandableList;

        public PhoneBookViewHolder(View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.name);
            number = itemView.findViewById(R.id.number);
            email = itemView.findViewById(R.id.email);
            photo = itemView.findViewById(R.id.photo);
            //expandableList = itemView.findViewById(R.id.expandable_list);
            //callButton = itemView.findViewById(R.id.call_button);
            //smsButton = itemView.findViewById(R.id.sms_button);
        }

        public void bind(final JsonData item) {
            boolean expanded = item.getExpanded();

            //expandableList.setVisibility(expanded ? View.VISIBLE : View.GONE);

            name.setText(item.getName());
            number.setText(item.getNumber());
            email.setText(item.getEmail());
            photo.setImageURI(item.getPhoto());
            if (photo.getDrawable() == null)
                photo.setImageResource(R.drawable.com_facebook_profile_picture_blank_portrait);

//            callButton.setOnClickListener(new View.OnClickListener() {
//                public void onClick(View v) {
//                    if (ActivityCompat.checkSelfPermission(PhoneBookAdapter.this.context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)
//                        ActivityCompat.requestPermissions((Activity)PhoneBookAdapter.this.context, new String[]{ Manifest.permission.CALL_PHONE }, PhoneBookFragment.PERMISSIONS_CALL_PHONE);
//                    else {
//                        Intent call = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + item.getNumber()));
//                        context.startActivity(call);
//                    }
//                }
//            });
//
//            smsButton.setOnClickListener(new View.OnClickListener() {
//                public void onClick(View v) {
//                    if (ActivityCompat.checkSelfPermission(PhoneBookAdapter.this.context, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED)
//                        ActivityCompat.requestPermissions((Activity)PhoneBookAdapter.this.context, new String[]{ Manifest.permission.SEND_SMS }, PhoneBookFragment.PERMISSIONS_REQUEST_SEND_SMS);
//                    else {
//                        Intent send = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + item.getNumber()));
//                        context.startActivity(send);
//                    }
//                }
//            });
        }
    }

    @Override
    public PhoneBookAdapter.PhoneBookViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.listview_item, parent, false);
        return new PhoneBookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PhoneBookAdapter.PhoneBookViewHolder holder, final int position) {
        final JsonData item = listViewItemList.get(position);
        holder.bind(item);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                item.setExpanded(!item.getExpanded());
                notifyItemChanged(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listViewItemList.size();
    }

    public void updateItems(ArrayList<JsonData> items) {
        listViewItemList.clear();
        if (items != null)
            listViewItemList.addAll(items);
        notifyDataSetChanged();
    }


    public void fillter(String searchText, ArrayList<JsonData> backupList){

        listViewItemList.clear();
        Log.d("검색 backup list", backupList.toString());

        for( JsonData item : backupList)
        {

            if(item.getName().toUpperCase().contains(searchText.toUpperCase()))
            {
                listViewItemList.add(item);
            }
        }

        notifyDataSetChanged();

    }
}