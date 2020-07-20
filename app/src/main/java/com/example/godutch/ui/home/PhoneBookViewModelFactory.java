package com.example.godutch.ui.home;


import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class PhoneBookViewModelFactory implements ViewModelProvider.Factory {
    private Context context;
    private Activity activity;
    public PhoneBookViewModelFactory(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(PhoneBookViewModel.class)) {
            return (T) new PhoneBookViewModel(context, activity);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}