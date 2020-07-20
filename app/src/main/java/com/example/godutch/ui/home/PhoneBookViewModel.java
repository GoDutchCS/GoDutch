package com.example.godutch.ui.home;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.godutch.Constants;

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

public class PhoneBookViewModel extends ViewModel {
    private MutableLiveData<ArrayList<JsonData>> contactList;
    private ContactRepository repository;
    private Activity activity;
    private String FBid;
    private OkHttpClient client = new OkHttpClient();
    private int i = 0;

    public PhoneBookViewModel(Context context, Activity activity) {
        repository = new ContactRepository(context);
        contactList = new MutableLiveData<>();
        this.activity = activity;
        FBid = activity.getIntent().getStringExtra("USER_ID");
    }

    public LiveData<ArrayList<JsonData>> getContacts() {
        return contactList;
    }

    public void initializeContacts() {
        if(i==0) {
            contactList.setValue(repository.getContactList(FBid));//fortest
            i++;
             }

        else{

            Request request = new Request.Builder()
                    .url(String.format("%s/api/contacts/"+FBid , Constants.SERVER_IP))
                    .get()
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    call.cancel();
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    final String jsonString = response.body().string();
                    Log.d("서버에서 받은것", jsonString);

                    try {
                        final ArrayList<JsonData> contacts = new ArrayList<JsonData>();
                        JSONObject jsonObject = new JSONObject( jsonString );
                        JSONArray jsonArray = jsonObject.getJSONArray("contacts");
                        for(int i =0; i < jsonArray.length() ; i++){
                            JSONObject obj = jsonArray.getJSONObject(i);
                            String name = obj.getString("name");
                            String email = obj.getString("email");
                            String number = obj.getString("number");

                            contacts.add(new JsonData(name, number, email, null));
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }


            });
            contactList.setValue(repository.getDBcontacts(FBid));
        }
    }


//    public void filterTextAll(String edit){
//        contactList.setValue(repository.filter(edit, FBid));
//    }

}