package com.noteit.mkpl_android_note_it.data;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.noteit.mkpl_android_note_it.R;
import com.noteit.mkpl_android_note_it.views.TaskTitleView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskHolder> {

    /* Callback for list item click events */
    public interface OnItemClickListener {
        void onItemClick(View v, int position);

        void onItemToggled(boolean active, int position);
    }

    /* ViewHolder for each task item */
    public class TaskHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TaskTitleView nameView;
        public TextView dateView;
        public ImageView priorityView;
        public CheckBox checkBox;

        public TaskHolder(View itemView) {
            super(itemView);

            nameView = (TaskTitleView) itemView.findViewById(R.id.text_description);
            dateView = (TextView) itemView.findViewById(R.id.text_date);
            priorityView = (ImageView) itemView.findViewById(R.id.priority);
            checkBox = (CheckBox) itemView.findViewById(R.id.checkbox);

            itemView.setOnClickListener(this);
            checkBox.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v == checkBox) {
                completionToggled(this);
            } else {
                postItemClick(this);
            }
        }
    }

    private static final String TAG = TaskAdapter.class.getSimpleName();
    private Cursor mCursor;
    private OnItemClickListener mOnItemClickListener;
    private Context mContext;

    public TaskAdapter(Cursor cursor) {
        mCursor = cursor;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    private void completionToggled(TaskHolder holder) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemToggled(holder.checkBox.isChecked(), holder.getAdapterPosition());
        }
    }

    private void postItemClick(TaskHolder holder) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(holder.itemView, holder.getAdapterPosition());
        }
    }

    @Override
    public TaskHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View itemView = LayoutInflater.from(mContext)
                .inflate(R.layout.list_item_task, parent, false);

        return new TaskHolder(itemView);
    }

    @Override
    public void onBindViewHolder(TaskHolder holder, int position) {

        //TODO: Bind the task data to the views

        // I will Try
        if (mCursor != null && mCursor.moveToPosition(position)) {
            Task mTask = new Task(mCursor);
            holder.nameView.setText(mTask.getDescription());

            if (String.valueOf(mTask.getDueDateMillis()) != null) {

                holder.dateView.setVisibility(View.VISIBLE);

                if (System.currentTimeMillis() > mTask.getDueDateMillis() &&
                        mTask.getDueDateMillis() < System.currentTimeMillis()) {
                    CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(mTask.getDueDateMillis(),
                            System.currentTimeMillis(), DateUtils.DAY_IN_MILLIS);
                    holder.dateView.setText(String.valueOf(timeAgo));
                } else if (mTask.getDueDateMillis() == Long.MAX_VALUE) {
                    holder.dateView.setText(null);
                } else {

                    long timeMiliseconds = mTask.getDueDateMillis();
                    SimpleDateFormat formatter = new SimpleDateFormat("MMM dd");
                    String times = formatter.format(new Date(timeMiliseconds));

                    holder.dateView.setText(String.valueOf(times));
                }
            }

            if (mTask.isPriority()) {
                holder.priorityView.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_priority));
            } else {
                holder.priorityView.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_not_priority));
            }
            if (mTask.isComplete()) {
                holder.nameView.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
                holder.checkBox.setChecked(true);
            } else {
                holder.checkBox.setChecked(false);
            }
        } else {
            Log.e(TAG, "onBindViewHolder: Cursor is null.");
        }

    }

    @Override
    public int getItemCount() {

        return (mCursor != null) ? mCursor.getCount() : 0;
    }

    /**
     * Retrieve a {@link Task} for the data at the given position.
     *
     * @param position Adapter item position.
     * @return A new {@link Task} filled with the position's attributes.
     */
    public Task getItem(int position) {
        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException("Invalid item position requested");
        }
        return new Task(mCursor);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).id;
    }

    public void swapCursor(Cursor cursor) {
        if (mCursor != null) {
            mCursor.close();
        }
        mCursor = cursor;
        notifyDataSetChanged();
    }

    public List<Task> getData() {
        List<Task> dt = new ArrayList<>();
        if (mCursor != null) {
            mCursor.moveToFirst();
            do {
                dt.add(new Task(mCursor));
            } while (mCursor.moveToNext());
        }
        return dt;
    }


    public void update(List<Task> data) {
        String[] column = DatabaseContract.TaskColumns.COLUMNS;
        MatrixCursor matrixCursor = new MatrixCursor(column);
        for (Task item : data) {
            Object[] dt = new Object[column.length];
            dt[DatabaseContract.TaskColumns.POSITION_ID] = item.getId();
            dt[DatabaseContract.TaskColumns.POSITION_DESCRIPTION] = item.getDescription();
            dt[DatabaseContract.TaskColumns.POSITION_IS_PRIORITY] = item.isPriority() ? 1 : 0;
            dt[DatabaseContract.TaskColumns.POSITION_IS_COMPLETE] = item.isComplete() ? 1 : 0;
            dt[DatabaseContract.TaskColumns.POSITION_DUE_DATE] = item.getDueDateMillis();
            matrixCursor.addRow(dt);
        }
        swapCursor(matrixCursor);
        matrixCursor.close();
    }
}
