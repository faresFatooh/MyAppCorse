package com.hrtrack.app.fragment;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.hrtrack.app.R;
import com.hrtrack.app.utils.CustomToast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class WorkInfoFragment extends Fragment {

    private TextInputEditText etJobTitle;
    private TextInputLayout tilJobTitle;
    private ChipGroup chipGroupDays;
    private RecyclerView recyclerWorkingHours;
    private DayScheduleAdapter adapter;
    private List<DaySchedule> daySchedules;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_work_info, container, false);

        tilJobTitle = view.findViewById(R.id.til_job_title);
        etJobTitle = view.findViewById(R.id.et_job_title);
        chipGroupDays = view.findViewById(R.id.chip_group_days);
        recyclerWorkingHours = view.findViewById(R.id.recycler_working_hours);

        daySchedules = new ArrayList<>();
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

        adapter = new DayScheduleAdapter(daySchedules);
        recyclerWorkingHours.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerWorkingHours.setAdapter(adapter);

        recyclerWorkingHours.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fade_in));

        chipGroupDays.setOnCheckedStateChangeListener((group, checkedIds) -> {
            List<String> selectedDays = getSelectedDays();
            for (DaySchedule ds : daySchedules) {
                ds.isEnabled = selectedDays.contains(ds.day);
            }
            adapter.notifyDataSetChanged();
        });

        return view;
    }

    public boolean validateInputs() {
        boolean isValid = true;

        if (TextUtils.isEmpty(getJobTitle())) {
            tilJobTitle.setError(getString(R.string.error_job_title_required));
            isValid = false;
        } else {
            tilJobTitle.setError(null);
        }

        List<Map<String, String>> schedule = getWorkingSchedule();
        if (schedule.isEmpty()) {
            CustomToast.show(requireContext(), getString(R.string.error_working_days_required));
            isValid = false;
        } else {
            for (Map<String, String> daySchedule : schedule) {
                String startTime = daySchedule.get("startTime");
                String endTime = daySchedule.get("endTime");
                if (!isValidTimeRange(startTime, endTime)) {
                    CustomToast.show(requireContext(), getString(R.string.error_invalid_time_range, daySchedule.get("day")));
                    isValid = false;
                }
            }
        }

        return isValid;
    }

    public String getJobTitle() {
        return etJobTitle.getText() != null ? etJobTitle.getText().toString().trim() : "";
    }

    public List<Map<String, String>> getWorkingSchedule() {
        List<Map<String, String>> schedule = new ArrayList<>();
        for (DaySchedule ds : daySchedules) {
            if (ds.isEnabled && !TextUtils.isEmpty(ds.startTime) && !TextUtils.isEmpty(ds.endTime)) {
                Map<String, String> daySchedule = new HashMap<>();
                daySchedule.put("day", ds.day);
                daySchedule.put("startTime", ds.startTime);
                daySchedule.put("endTime", ds.endTime);
                schedule.add(daySchedule);
            }
        }
        return schedule;
    }

    private List<String> getSelectedDays() {
        List<String> selectedDays = new ArrayList<>();
        for (int i = 0; i < chipGroupDays.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupDays.getChildAt(i);
            if (chip.isChecked()) {
                selectedDays.add(chip.getText().toString());
            }
        }
        return selectedDays;
    }

    private boolean isValidTimeRange(String startTime, String endTime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.US);
            long start = sdf.parse(startTime).getTime();
            long end = sdf.parse(endTime).getTime();
            return end > start;
        } catch (ParseException e) {
            return false;
        }
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

            if (!ds.isEnabled) {
                holder.itemView.setVisibility(View.GONE);
                holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
                return;
            } else {
                holder.itemView.setVisibility(View.VISIBLE);
                holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }

            holder.cbDay.setText(ds.day);
            holder.cbDay.setChecked(ds.isEnabled);
            holder.tvStartTime.setText(ds.startTime.isEmpty() ? getString(R.string.hint_start_time) : ds.startTime);
            holder.tvEndTime.setText(ds.endTime.isEmpty() ? getString(R.string.hint_end_time) : ds.endTime);

            holder.tvStartTime.setEnabled(ds.isEnabled);
            holder.tvEndTime.setEnabled(ds.isEnabled);

            holder.cbDay.setOnCheckedChangeListener((buttonView, isChecked) -> {
                ds.isEnabled = isChecked;
                holder.tvStartTime.setEnabled(isChecked);
                holder.tvEndTime.setEnabled(isChecked);
                notifyItemChanged(position);

                for (int i = 0; i < chipGroupDays.getChildCount(); i++) {
                    Chip chip = (Chip) chipGroupDays.getChildAt(i);
                    if (chip.getText().toString().equals(ds.day)) {
                        chip.setChecked(isChecked);
                        break;
                    }
                }
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
}