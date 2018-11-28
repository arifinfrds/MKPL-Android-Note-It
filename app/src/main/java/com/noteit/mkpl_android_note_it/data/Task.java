package com.noteit.mkpl_android_note_it.data;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import static com.noteit.mkpl_android_note_it.data.DatabaseContract.getColumnInt;
import static com.noteit.mkpl_android_note_it.data.DatabaseContract.getColumnLong;
import static com.noteit.mkpl_android_note_it.data.DatabaseContract.getColumnString;


public class Task implements Parcelable {

    public Task() {
    }

    /* Constants representing missing data */
    public static final long NO_DATE = Long.MAX_VALUE;
    public static final long NO_ID = -1;

    //Unique identifier in database
    @SerializedName("_id")
    public long id;

    //Task description
    @SerializedName("description")
    public String description;

    //Marked if task is done
    @SerializedName("is_complete")
    public boolean isComplete;

    //Marked if task is priority
    @SerializedName("is_priority")
    public boolean isPriority;

    //Optional due date for the task
    @SerializedName("due_date")
    public long dueDateMillis;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public void setComplete(boolean complete) {
        isComplete = complete;
    }

    public boolean isPriority() {
        return isPriority;
    }

    public void setPriority(boolean priority) {
        isPriority = priority;
    }

    public long getDueDateMillis() {
        return dueDateMillis;
    }

    public void setDueDateMillis(long dueDateMillis) {
        this.dueDateMillis = dueDateMillis;
    }

    /**
     * Create a new Task from discrete items
     */
    public Task(String description, boolean isComplete, boolean isPriority, long dueDateMillis) {
        this.id = NO_ID; //Not set
        this.description = description;
        this.isComplete = isComplete;
        this.isPriority = isPriority;
        this.dueDateMillis = dueDateMillis;
    }

    /**
     * Create a new Task with no due date
     */
    public Task(String description, boolean isComplete, boolean isPriority) {
        this(description, isComplete, isPriority, NO_DATE);
    }

    /**
     * Create a new task from a database Cursor
     */
    public Task(Cursor cursor) {
        this.id = getColumnLong(cursor, DatabaseContract.TaskColumns._ID);
        this.description = getColumnString(cursor, DatabaseContract.TaskColumns.DESCRIPTION);
        this.isComplete = getColumnInt(cursor, DatabaseContract.TaskColumns.IS_COMPLETE) == 1;
        this.isPriority = getColumnInt(cursor, DatabaseContract.TaskColumns.IS_PRIORITY) == 1;
        this.dueDateMillis = getColumnLong(cursor, DatabaseContract.TaskColumns.DUE_DATE);
    }

    /**
     * Return true if a due date has been set on this task.
     */
    public boolean hasDueDate() {
        return this.dueDateMillis != Long.MAX_VALUE;
    }

    /**
     * Create a new Insect from a data Parcel
     */
    protected Task(Parcel in) {
        this.id = in.readLong();
        this.description = in.readString();
        this.isComplete = in.readByte() != 0;
        this.isPriority = in.readByte() != 0;
        this.dueDateMillis = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(description);
        dest.writeByte((byte) (isComplete ? 1 : 0));
        dest.writeByte((byte) (isPriority ? 1 : 0));
        dest.writeLong(dueDateMillis);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Task> CREATOR = new Creator<Task>() {
        @Override
        public Task createFromParcel(Parcel in) {
            return new Task(in);
        }

        @Override
        public Task[] newArray(int size) {
            return new Task[size];
        }
    };


}