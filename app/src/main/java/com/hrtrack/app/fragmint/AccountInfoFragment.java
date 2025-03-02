package com.hrtrack.app.fragmint;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.hrtrack.app.R;

public class AccountInfoFragment extends Fragment {

    private TextInputEditText etEmail, etPassword, etConfirmPassword;
    private CheckBox cbPrivacy;

    public AccountInfoFragment() {}

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account_info, container, false);
        etEmail = view.findViewById(R.id.et_email);
        etPassword = view.findViewById(R.id.et_password);
        etConfirmPassword = view.findViewById(R.id.et_confirm_password);
        cbPrivacy = view.findViewById(R.id.cb_privacy_policy);
        return view;
    }

    public String getEmail() {
        return etEmail.getText().toString().trim();
    }

    public String getPassword() {
        return etPassword.getText().toString().trim();
    }

    public String getConfirmPassword() {
        return etConfirmPassword.getText().toString().trim();
    }

    public boolean isPrivacyAccepted() {
        return cbPrivacy.isChecked();
    }

    // يمكن إضافة دالة للتحقق من صحة البيانات هنا إن رغبت
    public boolean validate() {
        if (TextUtils.isEmpty(getEmail())) return false;
        if (TextUtils.isEmpty(getPassword())) return false;
        if (!getPassword().equals(getConfirmPassword())) return false;
        return isPrivacyAccepted();
    }
}