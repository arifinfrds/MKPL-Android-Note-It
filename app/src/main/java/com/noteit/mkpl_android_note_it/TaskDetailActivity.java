package com.noteit.mkpl_android_note_it;

import android.app.DatePickerDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;

public class TaskDetailActivity extends AppCompatActivity implements
        DatePickerDialog.OnDateSetListener  {

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
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

    }
}
