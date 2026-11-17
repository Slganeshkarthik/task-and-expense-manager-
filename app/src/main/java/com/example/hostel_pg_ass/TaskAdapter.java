package com.example.hostel_pg_ass;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import java.util.List;

public class TaskAdapter extends BaseAdapter {

    public static class Task {
        private final int id;
        private final String title;
        private final String date;
        private final String time;
        private final String priority;
        private final String status;

        public Task(int id, String title, String date, String time, String priority, String status) {
            this.id = id;
            this.title = title;
            this.date = date;
            this.time = time;
            this.priority = priority;
            this.status = status;
        }

        public int getId() { return id; }
        public String getTitle() { return title; }
        public String getDate() { return date; }
        public String getTime() { return time; }
        public String getPriority() { return priority; }
        public String getStatus() { return status; }
    }

    public interface OnDeleteListener {
        void onDelete(Task task);
    }

    private final Context context;
    private final List<Task> dataSource;
    private final OnDeleteListener onDelete;
    private final LayoutInflater inflater;

    public TaskAdapter(Context context, List<Task> dataSource, OnDeleteListener onDelete) {
        this.context = context;
        this.dataSource = dataSource;
        this.onDelete = onDelete;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return dataSource.size();
    }

    @Override
    public Object getItem(int position) {
        return dataSource.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        if (rowView == null) {
            rowView = inflater.inflate(R.layout.item_task, parent, false);
        }

        Task task = (Task) getItem(position);

        TextView tvTitle = rowView.findViewById(R.id.tvTaskTitle);
        TextView tvDetails = rowView.findViewById(R.id.tvTaskDetails);
        CheckBox cbStatus = rowView.findViewById(R.id.cbTaskStatus);
        ImageButton btnDelete = rowView.findViewById(R.id.btnDeleteTask);

        tvTitle.setText(task.getTitle());
        tvDetails.setText(task.getDate() + " - " + task.getTime() + " - " + task.getPriority());

        cbStatus.setOnCheckedChangeListener(null); // Prevent unwanted triggers
        cbStatus.setChecked("Completed".equals(task.getStatus()));

        if (cbStatus.isChecked()) {
            tvTitle.setPaintFlags(tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            tvTitle.setPaintFlags(tvTitle.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
        }

        cbStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String newStatus = isChecked ? "Completed" : "Pending";
            if (context instanceof TaskActivity) {
                ((TaskActivity) context).updateTaskStatus(task.getId(), newStatus);

                if (isChecked) {
                    tvTitle.setPaintFlags(tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                } else {
                    tvTitle.setPaintFlags(tvTitle.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                }
            }
        });

        btnDelete.setOnClickListener(v -> {
            if (onDelete != null) {
                onDelete.onDelete(task);
            }
        });

        return rowView;
    }
}
