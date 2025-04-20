package com.hrtrack.app.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hrtrack.app.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ReportsFragment extends Fragment {

    private TextView tvWeeklyHours, tvMonthlyHours, tvTotalHolidays;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reports, container, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        tvWeeklyHours = view.findViewById(R.id.tv_weekly_hours);
        tvMonthlyHours = view.findViewById(R.id.tv_monthly_hours);
        tvTotalHolidays = view.findViewById(R.id.tv_total_holidays);

        loadReports();

        return view;
    }

    private void loadReports() {
        String userId = mAuth.getCurrentUser().getUid();

        // Calculate weekly and monthly hours
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        // Weekly range
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        String weekStart = sdf.format(calendar.getTime());
        calendar.add(Calendar.DAY_OF_WEEK, 6);
        String weekEnd = sdf.format(calendar.getTime());

        // Monthly range
        calendar.setTime(new Date());
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        String monthStart = sdf.format(calendar.getTime());
        calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        String monthEnd = sdf.format(calendar.getTime());

        // Load work hours
        db.collection("users").document(userId).collection("workHours")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int weeklyHours = 0;
                    int monthlyHours = 0;
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        WorkHoursFragment.WorkHour workHour = doc.toObject(WorkHoursFragment.WorkHour.class);
                        String date = workHour.getDate();
                        if (date.compareTo(weekStart) >= 0 && date.compareTo(weekEnd) <= 0) {
                            weeklyHours += calculateHours(workHour);
                        }
                        if (date.compareTo(monthStart) >= 0 && date.compareTo(monthEnd) <= 0) {
                            monthlyHours += calculateHours(workHour);
                        }
                    }
                    tvWeeklyHours.setText(getString(R.string.weekly_hours, weeklyHours));
                    tvMonthlyHours.setText(getString(R.string.monthly_hours, monthlyHours));
                });

        // Load holidays
        db.collection("users").document(userId).collection("holidays")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalHolidays = queryDocumentSnapshots.size();
                    tvTotalHolidays.setText(getString(R.string.total_holidays, totalHolidays));
                });
    }

    private int calculateHours(WorkHoursFragment.WorkHour workHour) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.US);
            Date start = sdf.parse(workHour.getStartTime());
            Date end = sdf.parse(workHour.getEndTime());
            long diff = end.getTime() - start.getTime();
            return (int) (diff / (1000 * 60 * 60));
        } catch (Exception e) {
            return 0;
        }
    }
}