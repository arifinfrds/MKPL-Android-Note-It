package com.noteit.mkpl_android_note_it;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.graphics.Paint;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.noteit.mkpl_android_note_it.R;
import com.noteit.mkpl_android_note_it.data.DatabaseContract;
import com.noteit.mkpl_android_note_it.data.TaskUpdateService;
import com.noteit.mkpl_android_note_it.reminders.AlarmScheduler;
import com.noteit.mkpl_android_note_it.views.DatePickerFragment;
import com.noteit.mkpl_android_note_it.views.TaskTitleView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TaskDetailActivity extends AppCompatActivity implements
        DatePickerDialog.OnDateSetListener {

    public static final String EXTRA_PARCEL = "EXTRA_PARCEL";
    public static final String EXTRA_SAVED = "EXTRA_PARCEL";
    public static String EXTRA_ID = "EXTRA_ID";
    public static String EXTRA_DESCRIPTION = "EXTRA_DESCRIPTION";
    public static String EXTRA_IS_PRIORITY = "EXTRA_IS_PRIORITY";
    public static String EXTRA_IS_COMPLETE = "EXTRA_IS_COMPLETE";
    public static String EXTRA_DUE_DATE = "EXTRA_DUE_DATE";

    private TaskTitleView taskTitleView;
    private TextView dateView;
    private ImageView priorityView;

    private String description;
    private int priority, iscompleted;
    private long id, datetime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);
        //Task must be passed to this activity as a valid provider Uri

        final Uri taskUri = getIntent().getData();

        if (taskUri != null) {
            Cursor cursor = getContentResolver().query(taskUri,
                    null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    id = DatabaseContract.getColumnLong(cursor,
                            DatabaseContract.TaskColumns.KEY_ID);
                    description = DatabaseContract.getColumnString(cursor,
                            DatabaseContract.TaskColumns.DESCRIPTION);
                    datetime = DatabaseContract.getColumnLong(cursor,
                            DatabaseContract.TaskColumns.DUE_DATE);
                    priority = DatabaseContract.getColumnInt(cursor,
                            DatabaseContract.TaskColumns.IS_PRIORITY);
                    iscompleted = DatabaseContract.getColumnInt(cursor,
                            DatabaseContract.TaskColumns.IS_COMPLETE);
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        } else {
            id = getIntent().getLongExtra(EXTRA_ID, 0);
            description = getIntent().getStringExtra(EXTRA_DESCRIPTION);
            datetime = getIntent().getLongExtra(EXTRA_DUE_DATE, 0);
            priority = getIntent().getIntExtra(EXTRA_IS_PRIORITY, 0);
            iscompleted = getIntent().getIntExtra(EXTRA_IS_COMPLETE, 0);
        }

        //TODO: Display attributes of the provided task in the UI
        taskTitleView = (TaskTitleView) findViewById(R.id.text_description);
        dateView = (TextView) findViewById(R.id.text_date);
        priorityView = (ImageView) findViewById(R.id.priority);

        taskTitleView.setText(description);

        if (iscompleted == 1) {
            taskTitleView.setPaintFlags(taskTitleView.getPaintFlags()
                    | Paint.STRIKE_THRU_TEXT_FLAG);
        }

        dateView.setVisibility(View.VISIBLE);

        if (System.currentTimeMillis() > datetime &&
                datetime < System.currentTimeMillis()) {
            CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(datetime,
                    System.currentTimeMillis(), DateUtils.DAY_IN_MILLIS);
            dateView.setText(String.valueOf(timeAgo));
        } else if (datetime == Long.MAX_VALUE) {
            dateView.setText(getString(R.string.date_empty));
        } else {
            long timeMiliSeconds = datetime;
            SimpleDateFormat formatter = new SimpleDateFormat("MMM dd");
            String times = formatter.format(new Date(timeMiliSeconds));

            dateView.setText(getString(R.string.task_date) + " \t" + times);
        }

        if (priority == 1) {
            priorityView.setImageDrawable(ContextCompat.getDrawable(this,
                    R.drawable.ic_priority));
        } else {
            priorityView.setImageDrawable(ContextCompat.getDrawable(this,
                    R.drawable.ic_not_priority));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_task_detail, menu);
        if (iscompleted == 1) {
            menu.getItem(2).setVisible(true);
        }
        if (System.currentTimeMillis() > datetime &&
                datetime < System.currentTimeMillis()) {
            menu.getItem(0).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                String[] selectionArgs = new String[]{Long.toString(id)};
                TaskUpdateService.deleteTask(this, DatabaseContract.CONTENT_URI.buildUpon()
                                .appendPath(String.valueOf(id)).build(),
                        String.format("%s = ?", DatabaseContract.TaskColumns._ID), null);
                finish();
                break;
            case R.id.action_reminder:
                DatePickerFragment dialogFragment = new DatePickerFragment();
                dialogFragment.show(getSupportFragmentManager(), "datePicker");
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        //TODO: Handle date selection from a DatePickerFragment
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, day);
        c.set(Calendar.HOUR_OF_DAY, 12);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);

//Update tanggal alarm

        ContentValues values = new ContentValues(1);
        values.put(DatabaseContract.TaskColumns.DUE_DATE, c.getTimeInMillis());

        TaskUpdateService.updateTask(this, DatabaseContract.CONTENT_URI.buildUpon()
                .appendPath(String.valueOf(id)).build(), values,
                String.format("%s = ?", DatabaseContract.TaskColumns._ID), null);



        Toast.makeText(this, "Set Alarm "
                + description + " Success", Toast.LENGTH_SHORT).show();

        AlarmScheduler.scheduleAlarm(this, c.getTimeInMillis(),
                DatabaseContract.CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build());

    }
}
