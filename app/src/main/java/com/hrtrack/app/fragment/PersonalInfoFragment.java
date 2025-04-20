package com.hrtrack.app.fragment;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.hrtrack.app.R;
import com.hrtrack.app.utils.CustomToast;

import java.util.Calendar;
import java.util.Locale;

public class PersonalInfoFragment extends Fragment {

    private TextInputEditText etFullName, etBirthDate;
    private TextInputLayout tilFullName, tilBirthDate;
    private RadioGroup genderGroup;
    private String selectedGender;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_personal_info, container, false);

        tilFullName = view.findViewById(R.id.til_full_name);
        etFullName = view.findViewById(R.id.et_full_name);
        tilBirthDate = view.findViewById(R.id.til_birth_date);
        etBirthDate = view.findViewById(R.id.et_birth_date);
        genderGroup = view.findViewById(R.id.radio_gender);

        etBirthDate.setOnClickListener(v -> showDatePicker());

        genderGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_male) {
                selectedGender = "Male";
            } else if (checkedId == R.id.radio_female) {
                selectedGender = "Female";
            }
        });

        return view;
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(requireContext(),
                (view, year, month, dayOfMonth) -> {
                    String date = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    etBirthDate.setText(date);
                },
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    public boolean validateInputs() {
        boolean isValid = true;
        if (TextUtils.isEmpty(getFullName())) {
            tilFullName.setError(getString(R.string.error_full_name_required));
            isValid = false;
        } else {
            tilFullName.setError(null);
        }
        if (TextUtils.isEmpty(getBirthDate())) {
            tilBirthDate.setError(getString(R.string.error_birth_date_required));
            isValid = false;
        } else {
            tilBirthDate.setError(null);
        }
        if (TextUtils.isEmpty(getGender())) {
            CustomToast.show(requireContext(), getString(R.string.error_gender_required));
            isValid = false;
        }
        return isValid;
    }

    public String getFullName() {
        return etFullName.getText().toString().trim();
    }

    public String getBirthDate() {
        return etBirthDate.getText().toString().trim();
    }

    public String getGender() {
        return selectedGender != null ? selectedGender : "";
    }
}