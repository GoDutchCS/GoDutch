package com.example.godutch.ui.home;

import android.app.Activity;
import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

public class PhoneBookViewModel extends ViewModel {
    private MutableLiveData<ArrayList<JsonData>> contactList;
    private ContactRepository repository;
    private Activity activity;

    public PhoneBookViewModel(Context context, Activity activity) {
        repository = new ContactRepository(context);
        contactList = new MutableLiveData<>();
        this.activity = activity;
    }

    public LiveData<ArrayList<JsonData>> getContacts() {
        return contactList;
    }

    public void initializeContacts() {
        contactList.setValue(repository.getContactList(activity));

    }

    public void getDBdata(){
        repository.getDBcontacts(activity);
    }
}