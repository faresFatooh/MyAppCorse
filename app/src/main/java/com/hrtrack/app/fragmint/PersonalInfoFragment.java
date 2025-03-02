package com.hrtrack.app.fragmint;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.hrtrack.app.R;

public class PersonalInfoFragment extends Fragment {

    private TextInputEditText etFullName, etBirthDate;
    private RadioGroup rgGender;

    public PersonalInfoFragment() {
        // مطلوب مُنشئ فارغ
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_personal_info, container, false);
        etFullName = view.findViewById(R.id.et_full_name);
        etBirthDate = view.findViewById(R.id.et_birth_date);
        rgGender = view.findViewById(R.id.rg_gender);
        return view;
    }

    // طرق لاسترجاع البيانات
    public String getFullName() {
        return etFullName.getText().toString().trim();
    }

    public String getBirthDate() {
        return etBirthDate.getText().toString().trim();
    }

    public String getGender() {
        int selectedId = rgGender.getCheckedRadioButtonId();
        if(selectedId == R.id.rb_male)
            return "Male";
        else if(selectedId == R.id.rb_female)
            return "Female";
        return "";
    }
}