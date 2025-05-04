package com.hrtrack.app.ui;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.hrtrack.app.R;
import com.hrtrack.app.adapters.TaskAdapter;
import com.hrtrack.app.models.Task;
import com.hrtrack.app.utils.NotificationReceiver;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class AddTaskActivity extends AppCompatActivity
        implements TaskAdapter.OnTaskActionListener {

    private RecyclerView rv;
    private FloatingActionButton fab;
    private ProgressBar progressBar;
    private TaskAdapter adapter;
    private List<Task> list = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String userId;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_add_task);

        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, R.string.error_not_logged_in, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        userId = user.getUid();

        db = FirebaseFirestore.getInstance();
        rv = findViewById(R.id.rvTasks);
        fab = findViewById(R.id.fabAdd);
        progressBar = findViewById(R.id.progress_bar);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TaskAdapter(list, this);
        rv.setAdapter(adapter);

        fab.setOnClickListener(v -> showTaskDialog(null));

        loadAndSchedule();
        createNotificationChannel();
    }

    private void loadAndSchedule() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("tasks")
                .document(userId)
                .collection("user_tasks")
                .orderBy("dueDate", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(qs -> {
                    list.clear();
                    for (QueryDocumentSnapshot d : qs) {
                        Task t = d.toObject(Task.class);
                        t.setId(d.getId());
                        list.add(t);
                        scheduleNotification(this, t);
                    }
                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, getString(R.string.error_load_data, e.getMessage()), Toast.LENGTH_SHORT).show();
                });
    }

    private void showTaskDialog(final Task editing) {
        boolean isEdit = editing != null;
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        View v = getLayoutInflater().inflate(R.layout.dialog_add_task, null);
        b.setView(v);

        TextView tvDialogTitle = v.findViewById(R.id.tv_dialog_title);
        TextInputEditText etTitle = v.findViewById(R.id.etTitle);
        TextInputEditText etDesc = v.findViewById(R.id.etDesc);
        TextInputEditText etDate = v.findViewById(R.id.etDate);
        TextInputEditText etTime = v.findViewById(R.id.etTime);
        com.google.android.material.button.MaterialButton btnSave = v.findViewById(R.id.btnSave);
        com.google.android.material.button.MaterialButton btnCancel = v.findViewById(R.id.btnCancel);

        if (isEdit) {
            tvDialogTitle.setText(R.string.title_edit_task);
            etTitle.setText(editing.getTitle());
            etDesc.setText(editing.getDescription());
            etDate.setText(editing.getDueDate());
            etTime.setText(editing.getDueTime());
            btnSave.setText(R.string.label_update);
        } else {
            tvDialogTitle.setText(R.string.title_add_task);
            btnSave.setText(R.string.label_save);
        }

        etDate.setOnClickListener(u -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this, (dp, y, m, d) ->
                    etDate.setText(String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d)),
                    c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });
        etTime.setOnClickListener(u -> {
            Calendar c = Calendar.getInstance();
            new TimePickerDialog(this, (tp, h, mi) ->
                    etTime.setText(String.format(Locale.US, "%02d:%02d", h, mi)),
                    c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
        });

        AlertDialog dialog = b.create();
        btnSave.setOnClickListener(u -> {
            String title = etTitle.getText().toString().trim();
            String desc = etDesc.getText().toString().trim();
            String date = etDate.getText().toString().trim();
            String time = etTime.getText().toString().trim();
            if (title.isEmpty() || date.isEmpty() || time.isEmpty()) {
                Toast.makeText(this, R.string.error_required_fields, Toast.LENGTH_SHORT).show();
                return;
            }
            progressBar.setVisibility(View.VISIBLE);
            int nid = isEdit ? editing.getNotificationId() : new Random().nextInt(100000);
            Task t = new Task(title, desc, date, time, nid);

            CollectionReference userTasks = db.collection("tasks")
                    .document(userId)
                    .collection("user_tasks");

            if (isEdit) {
                userTasks.document(editing.getId())
                        .set(t)
                        .addOnSuccessListener(a -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(this, R.string.task_updated, Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            loadAndSchedule();
                        })
                        .addOnFailureListener(e -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(this, getString(R.string.error_store_data, e.getMessage()), Toast.LENGTH_SHORT).show();
                        });
            } else {
                userTasks.document(String.valueOf(nid))
                        .set(t)
                        .addOnSuccessListener(a -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(this, R.string.task_added, Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            loadAndSchedule();
                        })
                        .addOnFailureListener(e -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(this, getString(R.string.error_store_data, e.getMessage()), Toast.LENGTH_SHORT).show();
                        });
            }
        });

        btnCancel.setOnClickListener(u -> dialog.dismiss());

        dialog.show();
    }

    @Override
    public void onEdit(Task t) {
        showTaskDialog(t);
    }

    @Override
    public void onDelete(Task t) {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("tasks")
                .document(userId)
                .collection("user_tasks")
                .document(t.getId())
                .delete()
                .addOnSuccessListener(a -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, R.string.task_deleted, Toast.LENGTH_SHORT).show();
                    loadAndSchedule();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, getString(R.string.error_store_data, e.getMessage()), Toast.LENGTH_SHORT).show();
                });
    }

    @SuppressLint("ScheduleExactAlarm")
    private void scheduleNotification(Context c, Task t) {
        try {
            Date dt = sdf.parse(t.getDueDate() + " " + t.getDueTime());
            long when = dt.getTime();
            Intent i = new Intent(c, NotificationReceiver.class);
            i.putExtra("title", t.getTitle());
            i.putExtra("desc", t.getDescription());
            i.putExtra("nid", t.getNotificationId());

            PendingIntent pi = PendingIntent.getBroadcast(
                    c, t.getNotificationId(), i,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
            am.setExact(AlarmManager.RTC_WAKEUP, when, pi);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                    "task_chan", "Task Reminders", NotificationManager.IMPORTANCE_HIGH);
            ch.setDescription("Remind about due tasks");
            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
                    .createNotificationChannel(ch);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}