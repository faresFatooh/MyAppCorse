package com.hrtrack.app.fragmint;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.hrtrack.app.R;

import java.util.HashMap;
import java.util.Map;

public class WorkInfoFragment extends Fragment {

    private TextInputEditText etJobTitle;
    // CheckBoxes
    private CheckBox cbMonday, cbTuesday, cbWednesday, cbThursday, cbFriday, cbSaturday, cbSunday;
    // Time fields
    private TextInputEditText etMonStart, etMonEnd, etTueStart, etTueEnd,
            etWedStart, etWedEnd, etThuStart, etThuEnd,
            etFriStart, etFriEnd, etSatStart, etSatEnd,
            etSunStart, etSunEnd;

    public WorkInfoFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_work_info, container, false);
        etJobTitle = view.findViewById(R.id.et_job_title);

        cbMonday = view.findViewById(R.id.cb_monday);
        cbTuesday = view.findViewById(R.id.cb_tuesday);
        cbWednesday = view.findViewById(R.id.cb_wednesday);
        cbThursday = view.findViewById(R.id.cb_thursday);
        cbFriday = view.findViewById(R.id.cb_friday);
        cbSaturday = view.findViewById(R.id.cb_saturday);
        cbSunday = view.findViewById(R.id.cb_sunday);

        etMonStart = view.findViewById(R.id.et_mon_start);
        etMonEnd = view.findViewById(R.id.et_mon_end);
        etTueStart = view.findViewById(R.id.et_tue_start);
        etTueEnd = view.findViewById(R.id.et_tue_end);
        etWedStart = view.findViewById(R.id.et_wed_start);
        etWedEnd = view.findViewById(R.id.et_wed_end);
        etThuStart = view.findViewById(R.id.et_thu_start);
        etThuEnd = view.findViewById(R.id.et_thu_end);
        etFriStart = view.findViewById(R.id.et_fri_start);
        etFriEnd = view.findViewById(R.id.et_fri_end);
        etSatStart = view.findViewById(R.id.et_sat_start);
        etSatEnd = view.findViewById(R.id.et_sat_end);
        etSunStart = view.findViewById(R.id.et_sun_start);
        etSunEnd = view.findViewById(R.id.et_sun_end);

        return view;
    }

    public String getJobTitle() {
        return etJobTitle.getText().toString().trim();
    }

    // استرجاع بيانات الجدول الزمني كـ Map
    public Map<String, Object> getWorkingSchedule() {
        Map<String, Object> schedule = new HashMap<>();

        if(cbMonday.isChecked()){
            Map<String,String> mon = new HashMap<>();
            mon.put("start", etMonStart.getText().toString().trim());
            mon.put("end", etMonEnd.getText().toString().trim());
            schedule.put("Monday", mon);
        }
        if(cbTuesday.isChecked()){
            Map<String,String> tue = new HashMap<>();
            tue.put("start", etTueStart.getText().toString().trim());
            tue.put("end", etTueEnd.getText().toString().trim());
            schedule.put("Tuesday", tue);
        }
        if(cbWednesday.isChecked()){
            Map<String,String> wed = new HashMap<>();
            wed.put("start", etWedStart.getText().toString().trim());
            wed.put("end", etWedEnd.getText().toString().trim());
            schedule.put("Wednesday", wed);
        }
        if(cbThursday.isChecked()){
            Map<String,String> thu = new HashMap<>();
            thu.put("start", etThuStart.getText().toString().trim());
            thu.put("end", etThuEnd.getText().toString().trim());
            schedule.put("Thursday", thu);
        }
        if(cbFriday.isChecked()){
            Map<String,String> fri = new HashMap<>();
            fri.put("start", etFriStart.getText().toString().trim());
            fri.put("end", etFriEnd.getText().toString().trim());
            schedule.put("Friday", fri);
        }
        if(cbSaturday.isChecked()){
            Map<String,String> sat = new HashMap<>();
            sat.put("start", etSatStart.getText().toString().trim());
            sat.put("end", etSatEnd.getText().toString().trim());
            schedule.put("Saturday", sat);
        }
        if(cbSunday.isChecked()){
            Map<String,String> sun = new HashMap<>();
            sun.put("start", etSunStart.getText().toString().trim());
            sun.put("end", etSunEnd.getText().toString().trim());
            schedule.put("Sunday", sun);
        }
        return schedule;
    }
}