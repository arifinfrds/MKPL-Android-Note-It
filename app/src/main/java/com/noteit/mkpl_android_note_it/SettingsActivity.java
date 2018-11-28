package com.noteit.mkpl_android_note_it;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.noteit.mkpl_android_note_it.views.SettingsFragment;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}