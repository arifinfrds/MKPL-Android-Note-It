package com.noteit.mkpl_android_note_it.data;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.noteit.mkpl_android_note_it.reminders.ReminderAlarmService;

import java.util.Arrays;

/* Process DB actions on a background thread */
public class TaskUpdateService extends IntentService {
    private static final String TAG = TaskUpdateService.class.getSimpleName();
    //Intent actions
    public static final String ACTION_INSERT = TAG + ".INSERT";
    public static final String ACTION_UPDATE = TAG + ".UPDATE";
    public static final String ACTION_DELETE = TAG + ".DELETE";

    public static final String EXTRA_VALUES = TAG + ".ContentValues";

    public static final String EXTRA_SELECTION = TAG + ".Selection";
    public static final String EXTRA_ARGS = TAG + ".SelectionArgs";

    public static void insertNewTask(Context context, ContentValues values) {
        Intent intent = new Intent(context, TaskUpdateService.class);
        intent.setAction(ACTION_INSERT);
        intent.putExtra(EXTRA_VALUES, values);
        context.startService(intent);
    }

    public static void updateTask(Context context, Uri uri, ContentValues values,
                                  String selection, String[] selectionArgs) {
        Intent intent = new Intent(context, TaskUpdateService.class);
        intent.setAction(ACTION_UPDATE);
        intent.setData(uri);
        intent.putExtra(EXTRA_VALUES, values);
        intent.putExtra(EXTRA_SELECTION, selection);
        intent.putExtra(EXTRA_ARGS, selectionArgs);
        context.startService(intent);
    }

    public static void deleteTask(Context context, Uri uri, String selection,
                                  String[] selectionArgs) {
        Intent intent = new Intent(context, TaskUpdateService.class);
        intent.setAction(ACTION_DELETE);
        intent.setData(uri);
        intent.putExtra(EXTRA_SELECTION, selection);
        intent.putExtra(EXTRA_ARGS, selectionArgs);
        context.startService(intent);
    }

    public TaskUpdateService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (ACTION_INSERT.equals(intent.getAction())) {
            ContentValues values = intent.getParcelableExtra(EXTRA_VALUES);
            performInsert(values);
        } else if (ACTION_UPDATE.equals(intent.getAction())) {
            ContentValues values = intent.getParcelableExtra(EXTRA_VALUES);
            String whereSelect = intent.getStringExtra(EXTRA_SELECTION);
            String[] Args = intent.getStringArrayExtra(EXTRA_ARGS);
            Log.d("test_values", String.valueOf(intent.getParcelableExtra(EXTRA_VALUES)));
            Log.d("test_args", Arrays.toString(intent.getStringArrayExtra(EXTRA_ARGS)));
            performUpdate(intent.getData(), values, whereSelect, Args);
        } else if (ACTION_DELETE.equals(intent.getAction())) {
            String whereSelect = intent.getStringExtra(EXTRA_SELECTION);
            String[] Args = intent.getStringArrayExtra(EXTRA_ARGS);
            Log.d("test_args", Arrays.toString(intent.getStringArrayExtra(EXTRA_ARGS)));
            performDelete(intent.getData(), whereSelect, Args);
        }
    }

    private void performInsert(ContentValues values) {
        if (getContentResolver().insert(DatabaseContract.CONTENT_URI, values) != null) {
            Log.d(TAG, "Inserted new task");
        } else {
            Log.w(TAG, "Error inserting new task");
        }
    }

    private void performUpdate(Uri uri, ContentValues values, String selection,
                               String[] selectionArgs) {
        int count = getContentResolver().update(uri, values, selection, selectionArgs);
        Log.d(TAG, "Updated " + count + " task items");
    }

    private void performDelete(Uri uri, String selection,
                               String[] selectionArgs) {
        int count = getContentResolver().delete(uri, selection, selectionArgs);

        //Cancel any reminders that might be set for this item
        PendingIntent operation =
                ReminderAlarmService.getReminderPendingIntent(this, uri);
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        manager.cancel(operation);

        Log.d(TAG, "Deleted " + count + " tasks");
    }
}
