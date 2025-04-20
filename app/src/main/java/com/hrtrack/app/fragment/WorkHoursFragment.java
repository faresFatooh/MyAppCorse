package com.hrtrack.app.fragment;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hrtrack.app.R;
import com.hrtrack.app.utils.CustomToast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class WorkHoursFragment extends Fragment {

    private RecyclerView recyclerView;
    private WorkHoursAdapter adapter;
    private List<WorkHour> workHoursList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FloatingActionButton fabRecordHours;
    private TableLayout tableSchedule;
    private String selectedStartTime, selectedEndTime;
    private ImageButton btnEditSchedule;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_work_hours, container, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        recyclerView = view.findViewById(R.id.recycler_work_hours);
        fabRecordHours = view.findViewById(R.id.fab_record_hours);
        tableSchedule = view.findViewById(R.id.table_schedule);
        btnEditSchedule = view.findViewById(R.id.btn_edit_schedule);

        workHoursList = new ArrayList<>();
        adapter = new WorkHoursAdapter(workHoursList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        fabRecordHours.setOnClickListener(v -> showTimePickerDialog());
        btnEditSchedule.setOnClickListener(v -> showEditScheduleDialog());
        loadUserSchedule();
        loadWorkHours();

        recyclerView.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fade_in));

        return view;
    }

    private void loadUserSchedule() {
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<Map<String, String>> schedule = (List<Map<String, String>>) documentSnapshot.get("workingSchedule");
                        if (schedule != null) {
                            tableSchedule.removeViews(1, tableSchedule.getChildCount() - 1);

                            for (Map<String, String> daySchedule : schedule) {
                                String day = daySchedule.get("day");
                                String startTime = daySchedule.get("startTime");
                                String endTime = daySchedule.get("endTime");

                                TableRow row = new TableRow(getContext());
                                row.setLayoutParams(new TableRow.LayoutParams(
                                        TableRow.LayoutParams.MATCH_PARENT,
                                        TableRow.LayoutParams.WRAP_CONTENT));
                                row.setBackgroundResource(R.drawable.table_row_selector);
                                row.setPadding(8, 8, 8, 8);

                                TextView tvDay = new TextView(getContext());
                                tvDay.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));
                                tvDay.setText(day);
                                tvDay.setTextSize(14);
                                tvDay.setTextColor(getResources().getColor(R.color.black));
                                tvDay.setGravity(android.view.Gravity.CENTER);
                                row.addView(tvDay);

                                TextView tvStart = new TextView(getContext());
                                tvStart.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));
                                tvStart.setText(startTime);
                                tvStart.setTextSize(14);
                                tvStart.setTextColor(getResources().getColor(R.color.black));
                                tvStart.setGravity(android.view.Gravity.CENTER);
                                row.addView(tvStart);

                                TextView tvEnd = new TextView(getContext());
                                tvEnd.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));
                                tvEnd.setText(endTime);
                                tvEnd.setTextSize(14);
                                tvEnd.setTextColor(getResources().getColor(R.color.black));
                                tvEnd.setGravity(android.view.Gravity.CENTER);
                                row.addView(tvEnd);

                                View divider = new View(getContext());
                                divider.setLayoutParams(new TableLayout.LayoutParams(
                                        TableLayout.LayoutParams.MATCH_PARENT, 1));
                                divider.setBackgroundColor(getResources().getColor(R.color.grey_300));

                                tableSchedule.addView(row);
                                tableSchedule.addView(divider);
                            }
                        }
                    }
                });
    }

    private void showEditScheduleDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_edit_schedule);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        RecyclerView recyclerDays = dialog.findViewById(R.id.recycler_days);
        Button btnSave = dialog.findViewById(R.id.btn_save);
        Button btnCancel = dialog.findViewById(R.id.btn_cancel);

        List<DaySchedule> daySchedules = new ArrayList<>();
        String[] days = {
                getString(R.string.day_sun),
                getString(R.string.day_mon),
                getString(R.string.day_tue),
                getString(R.string.day_wed),
                getString(R.string.day_thu),
                getString(R.string.day_fri),
                getString(R.string.day_sat)
        };
        for (String day : days) {
            daySchedules.add(new DaySchedule(day, "", ""));
        }

        DayScheduleAdapter adapter = new DayScheduleAdapter(daySchedules);
        recyclerDays.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerDays.setAdapter(adapter);

        String userId = mAuth.getCurrentUser().getUid();
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<Map<String, String>> schedule = (List<Map<String, String>>) documentSnapshot.get("workingSchedule");
                        if (schedule != null) {
                            for (Map<String, String> daySchedule : schedule) {
                                String day = daySchedule.get("day");
                                String startTime = daySchedule.get("startTime");
                                String endTime = daySchedule.get("endTime");
                                for (DaySchedule ds : daySchedules) {
                                    if (ds.day.equals(day)) {
                                        ds.startTime = startTime;
                                        ds.endTime = endTime;
                                        ds.isEnabled = true;
                                        break;
                                    }
                                }
                            }
                            recyclerDays.post(adapter::notifyDataSetChanged);
                        }
                    }
                });

        btnSave.setOnClickListener(v -> {
            List<Map<String, String>> newSchedule = new ArrayList<>();
            boolean hasValidDay = false;

            for (DaySchedule ds : daySchedules) {
                if (ds.isEnabled && !ds.startTime.isEmpty() && !ds.endTime.isEmpty()) {
                    Map<String, String> daySchedule = new HashMap<>();
                    daySchedule.put("day", ds.day);
                    daySchedule.put("startTime", ds.startTime);
                    daySchedule.put("endTime", ds.endTime);
                    newSchedule.add(daySchedule);
                    hasValidDay = true;
                }
            }

            if (!hasValidDay) {
                CustomToast.show(getContext(), getString(R.string.error_working_days_required));
                return;
            }

            db.collection("users").document(userId)
                    .update("workingSchedule", newSchedule)
                    .addOnSuccessListener(aVoid -> {
                        CustomToast.show(getContext(), getString(R.string.schedule_updated));
                        loadUserSchedule();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> CustomToast.show(getContext(), getString(R.string.error_store_data, e.getMessage())));
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void loadWorkHours() {
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("users").document(userId).collection("workHours")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    workHoursList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        WorkHour workHour = doc.toObject(WorkHour.class);
                        workHoursList.add(workHour);
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private void showTimePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog startTimePicker = new TimePickerDialog(requireContext(),
                (view, hourOfDay, minute) -> {
                    selectedStartTime = String.format(Locale.US, "%02d:%02d", hourOfDay, minute);
                    // Show end time picker after selecting start time
                    TimePickerDialog endTimePicker = new TimePickerDialog(requireContext(),
                            (view1, hourOfDay1, minute1) -> {
                                selectedEndTime = String.format(Locale.US, "%02d:%02d", hourOfDay1, minute1);
                                recordWorkHours();
                            },
                            calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
                    endTimePicker.show();
                },
                calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
        startTimePicker.show();
    }

    private void recordWorkHours() {
        if (selectedStartTime == null || selectedEndTime == null) {
            CustomToast.show(getContext(), getString(R.string.error_time_required));
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String currentDate = sdf.format(new Date());

        WorkHour workHour = new WorkHour(currentDate, selectedStartTime, selectedEndTime);
        db.collection("users").document(userId).collection("workHours")
                .document(currentDate)
                .set(workHour)
                .addOnSuccessListener(aVoid -> {
                    CustomToast.show(getContext(), getString(R.string.work_hours_recorded));
                    selectedStartTime = null;
                    selectedEndTime = null;
                    loadWorkHours();
                })
                .addOnFailureListener(e -> CustomToast.show(getContext(), getString(R.string.error_store_data, e.getMessage())));
    }

    private static class DaySchedule {
        String day;
        String startTime;
        String endTime;
        boolean isEnabled;

        DaySchedule(String day, String startTime, String endTime) {
            this.day = day;
            this.startTime = startTime;
            this.endTime = endTime;
            this.isEnabled = false;
        }
    }

    private class DayScheduleAdapter extends RecyclerView.Adapter<DayScheduleAdapter.ViewHolder> {
        private List<DaySchedule> daySchedules;

        DayScheduleAdapter(List<DaySchedule> daySchedules) {
            this.daySchedules = daySchedules;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_day_schedule, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            DaySchedule ds = daySchedules.get(position);
            holder.cbDay.setText(ds.day);
            holder.cbDay.setOnCheckedChangeListener(null);
            holder.cbDay.setChecked(ds.isEnabled);
            holder.tvStartTime.setText(ds.startTime.isEmpty() ? getString(R.string.hint_start_time) : ds.startTime);
            holder.tvEndTime.setText(ds.endTime.isEmpty() ? getString(R.string.hint_end_time) : ds.endTime);

            holder.tvStartTime.setEnabled(ds.isEnabled);
            holder.tvEndTime.setEnabled(ds.isEnabled);

            holder.cbDay.setOnCheckedChangeListener((buttonView, isChecked) -> {
                ds.isEnabled = isChecked;
                holder.tvStartTime.setEnabled(isChecked);
                holder.tvEndTime.setEnabled(isChecked);
                holder.itemView.post(() -> notifyItemChanged(position));
            });

            holder.tvStartTime.setOnClickListener(v -> {
                Calendar calendar = Calendar.getInstance();
                TimePickerDialog timePicker = new TimePickerDialog(requireContext(),
                        (view, hourOfDay, minute) -> {
                            ds.startTime = String.format(Locale.US, "%02d:%02d", hourOfDay, minute);
                            holder.tvStartTime.setText(ds.startTime);
                        },
                        calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
                timePicker.show();
            });

            holder.tvEndTime.setOnClickListener(v -> {
                Calendar calendar = Calendar.getInstance();
                TimePickerDialog timePicker = new TimePickerDialog(requireContext(),
                        (view, hourOfDay, minute) -> {
                            ds.endTime = String.format(Locale.US, "%02d:%02d", hourOfDay, minute);
                            holder.tvEndTime.setText(ds.endTime);
                        },
                        calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
                timePicker.show();
            });
        }

        @Override
        public int getItemCount() {
            return daySchedules.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            CheckBox cbDay;
            TextView tvStartTime, tvEndTime;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                cbDay = itemView.findViewById(R.id.cb_day);
                tvStartTime = itemView.findViewById(R.id.tv_start_time);
                tvEndTime = itemView.findViewById(R.id.tv_end_time);
            }
        }
    }

    public static class WorkHour {
        private String date;
        private String startTime;
        private String endTime;

        public WorkHour() {}

        public WorkHour(String date, String startTime, String endTime) {
            this.date = date;
            this.startTime = startTime;
            this.endTime = endTime;
        }

        public String getDate() {
            return date;
        }

        public String getStartTime() {
            return startTime;
        }

        public String getEndTime() {
            return endTime;
        }
    }

    private class WorkHoursAdapter extends RecyclerView.Adapter<WorkHoursAdapter.ViewHolder> {
        private List<WorkHour> workHours;

        public WorkHoursAdapter(List<WorkHour> workHours) {
            this.workHours = workHours;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_work_hour, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            WorkHour workHour = workHours.get(position);
            holder.tvDate.setText(workHour.getDate());
            holder.tvStartTime.setText(workHour.getStartTime());
            holder.tvEndTime.setText(workHour.getEndTime());
            holder.tvDuration.setText(calculateDuration(workHour.getStartTime(), workHour.getEndTime()));
        }

        @Override
        public int getItemCount() {
            return workHours.size();
        }

        private String calculateDuration(String startTime, String endTime) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.US);
                long start = sdf.parse(startTime).getTime();
                long end = sdf.parse(endTime).getTime();
                long diff = end - start;
                long hours = diff / (1000 * 60 * 60);
                long minutes = (diff / (1000 * 60)) % 60;
                return String.format(Locale.US, "%dh %dm", hours, minutes);
            } catch (Exception e) {
                return "N/A";
            }
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvDate, tvStartTime, tvEndTime, tvDuration;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvDate = itemView.findViewById(R.id.tv_date);
                tvStartTime = itemView.findViewById(R.id.tv_start_time);
                tvEndTime = itemView.findViewById(R.id.tv_end_time);
                tvDuration = itemView.findViewById(R.id.tv_duration);
            }
        }
    }
}