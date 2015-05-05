package com.teamtreehouse.mememaker;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


public class MemeMakerApplicationSettings {
    SharedPreferences mSharedPreferences;

    public MemeMakerApplicationSettings(Context context){
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

    }

    public String getStoragePreference(){
        // Reading Preferences
        return mSharedPreferences.getString("STORAGE", "");
    }

    public void setSharedPreferences(String storageType){
        //commit() is synchronous, apply() is asynchronous
        mSharedPreferences.edit()
                          .putString("STORAGE", storageType)
                          .apply();
    }
}
