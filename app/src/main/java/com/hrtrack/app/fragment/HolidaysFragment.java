package com.hrtrack.app.fragment;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hrtrack.app.R;
import com.hrtrack.app.utils.CustomToast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HolidaysFragment extends Fragment {

    private RecyclerView recyclerView;
    private HolidayAdapter adapter;
    private List<Holiday> holidayList;
    private TextInputEditText etHolidayDate, etHolidayReason;
    private Button btnAddHoliday;
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_holidays, container, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        recyclerView = view.findViewById(R.id.recycler_holidays);
        etHolidayDate = view.findViewById(R.id.et_holiday_date);
        etHolidayReason = view.findViewById(R.id.et_holiday_reason);
        btnAddHoliday = view.findViewById(R.id.btn_add_holiday);
        progressBar = view.findViewById(R.id.progress_bar);

        holidayList = new ArrayList<>();
        adapter = new HolidayAdapter(holidayList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        etHolidayDate.setOnClickListener(v -> showDatePicker());
        btnAddHoliday.setOnClickListener(v -> addHoliday());
        loadHolidays();

        return view;
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(requireContext(),
                (view, year, month, dayOfMonth) -> {
                    String date = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    etHolidayDate.setText(date);
                },
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void addHoliday() {
        String date = etHolidayDate.getText().toString().trim();
        String reason = etHolidayReason.getText().toString().trim();
        if (date.isEmpty() || reason.isEmpty()) {
            CustomToast.show(getContext(), getString(R.string.error_holiday_required));
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        String userId = mAuth.getCurrentUser().getUid();
        Holiday holiday = new Holiday(date, reason);
        db.collection("users").document(userId).collection("holidays")
                .document(date)
                .set(holiday)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    CustomToast.show(getContext(), getString(R.string.holiday_added));
                    etHolidayDate.setText("");
                    etHolidayReason.setText("");
                    loadHolidays();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    CustomToast.show(getContext(), getString(R.string.error_store_data, e.getMessage()));
                });
    }

    private void loadHolidays() {
        progressBar.setVisibility(View.VISIBLE);
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("users").document(userId).collection("holidays")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    holidayList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Holiday holiday = doc.toObject(Holiday.class);
                        holidayList.add(holiday);
                    }
                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    CustomToast.show(getContext(), getString(R.string.error_load_data, e.getMessage()));
                });
    }

    public static class Holiday {
        private String date;
        private String reason;

        public Holiday() {}

        public Holiday(String date, String reason) {
            this.date = date;
            this.reason = reason;
        }

        public String getDate() {
            return date;
        }

        public String getReason() {
            return reason;
        }
    }

    private class HolidayAdapter extends RecyclerView.Adapter<HolidayAdapter.ViewHolder> {
        private List<Holiday> holidays;

        public HolidayAdapter(List<Holiday> holidays) {
            this.holidays = holidays;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_holiday, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Holiday holiday = holidays.get(position);
            holder.tvDate.setText(holiday.getDate());
            holder.tvReason.setText(holiday.getReason());
            holder.btnDelete.setOnClickListener(v -> deleteHoliday(holiday.getDate()));
        }

        @Override
        public int getItemCount() {
            return holidays.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvDate, tvReason;
            Button btnDelete;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvDate = itemView.findViewById(R.id.tv_date);
                tvReason = itemView.findViewById(R.id.tv_reason);
                btnDelete = itemView.findViewById(R.id.btn_delete);
            }
        }

        private void deleteHoliday(String date) {
            progressBar.setVisibility(View.VISIBLE);
            String userId = mAuth.getCurrentUser().getUid();
            db.collection("users").document(userId).collection("holidays")
                    .document(date)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        progressBar.setVisibility(View.GONE);
                        CustomToast.show(getContext(), getString(R.string.holiday_deleted));
                        loadHolidays();
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        CustomToast.show(getContext(), getString(R.string.error_store_data, e.getMessage()));
                    });
        }
    }
}