package com.noteit.mkpl_android_note_it;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.noteit.mkpl_android_note_it.data.DatabaseContract;
import com.noteit.mkpl_android_note_it.data.Task;
import com.noteit.mkpl_android_note_it.data.TaskAdapter;
import com.noteit.mkpl_android_note_it.data.TaskProvider;
import com.noteit.mkpl_android_note_it.data.TaskUpdateService;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        TaskAdapter.OnItemClickListener,
        View.OnClickListener, SharedPreferences.OnSharedPreferenceChangeListener {
    private TaskAdapter mAdapter;
    private FloatingActionButton fab;
    private TaskProvider taskProvider;
    private static final String PREFERENCES_NAME = "main_preferences";
    private static final String PREFERENCES_KEY = "sort_by";
    private static final String EXTRA_DATA = "DATA";
    private static final String EXTRA_SORT = "SORT";
    private String mSort;

    SharedPreferences mSharedPreferences;

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);

        mSharedPreferences = PreferenceManager.
                getDefaultSharedPreferences(this);
        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);

        setupSharedPreference(mSharedPreferences);

        mAdapter = new TaskAdapter(null);
        mAdapter.setOnItemClickListener(this);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        if (savedInstanceState == null) {
            loadData();
        } else {
            loadSavedIDataInstance(savedInstanceState);
        }
    }

    private void loadSavedIDataInstance(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            List<Task> mData = savedInstanceState.getParcelableArrayList(EXTRA_DATA);
            if (mData != null) {
                mAdapter.update(mData);
            }
        }
    }

    private void setupSharedPreference(SharedPreferences sp) {
        String sortByKey = getString(R.string.pref_sortBy_key);
        String sortDefault = getString(R.string.pref_sortBy_default);
        String sortDate = getString(R.string.pref_sortBy_due);
        String getSort = sp.getString(sortByKey, sortDefault);
        if (getSort.equals(sortDate)) {
            mSort = sp.getString(EXTRA_SORT,
                    DatabaseContract.TaskColumns.DUE_DATE);
        } else {
            mSort = sp.getString(EXTRA_SORT,
                    DatabaseContract.TaskColumns.KEY_ID);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_sortBy_key))) {
            setupSharedPreference(sharedPreferences);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mAdapter != null) {
            outState.putParcelableArrayList(EXTRA_DATA, new ArrayList<Parcelable>
                    (mAdapter.getData()));
        }
        outState.putString(EXTRA_SORT, mSort);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        loadData();
    }

    private void loadData() {
        mAdapter.swapCursor(getContentResolver().query(DatabaseContract.CONTENT_URI, null,
                null, null, mSort));
        mAdapter.notifyDataSetChanged();
        Log.d(TAG, "Count Data: " + mAdapter.getItemCount());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /* Click events in Floating Action Button */
    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, AddTaskActivity.class);
        startActivity(intent);
    }

    /* Click events in RecyclerView items */
    @Override
    public void onItemClick(View v, int position) {
        List<Task> mData = mAdapter.getData();

        Intent intent = new Intent(this,
                TaskDetailActivity.class);
        intent.putExtra(TaskDetailActivity.EXTRA_ID, mData.get(position).getId());
        intent.putExtra(TaskDetailActivity.EXTRA_DESCRIPTION,
                mData.get(position).getDescription());
        intent.putExtra(TaskDetailActivity.EXTRA_IS_PRIORITY,
                mData.get(position).isPriority() ? 1 : 0);
        intent.putExtra(TaskDetailActivity.EXTRA_IS_COMPLETE,
                mData.get(position).isComplete() ? 1 : 0);
        intent.putExtra(TaskDetailActivity.EXTRA_DUE_DATE,
                mData.get(position).getDueDateMillis());
        startActivity(intent);
    }

    /* Click events on RecyclerView item checkboxes */
    @Override
    public void onItemToggled(boolean active, int position) {

        List<Task> mData = mAdapter.getData();
        String[] selectionArgs = new String[]{Long.toString(mData.get(position).getId())};
        ContentValues values = new ContentValues();
        if (active == true && mData.get(position).isComplete()) {
            values.put(DatabaseContract.TaskColumns.IS_COMPLETE,
                    mData.get(position).isComplete() ? 1 : 0);
        } else {
            values.put(DatabaseContract.TaskColumns.IS_COMPLETE,
                    mData.get(position).isComplete() ? 0 : 1);
        }

        TaskUpdateService.updateTask(this, DatabaseContract.CONTENT_URI, values,
                String.format("%s = ?", DatabaseContract.TaskColumns._ID), selectionArgs);
        Toast.makeText(this, "Update Task " + mData.get(position).getDescription() + " Completed!",
                Toast.LENGTH_SHORT).show();
    }
}
