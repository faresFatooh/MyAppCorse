package com.hrtrack.app.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.hrtrack.app.R;
import com.hrtrack.app.utils.CustomToast;

public class AccountInfoFragment extends Fragment {

    private TextInputEditText etEmail, etPassword, etConfirmPassword;
    private TextInputLayout tilEmail, tilPassword, tilConfirmPassword;
    private MaterialCheckBox checkboxPrivacy;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account_info, container, false);

        tilEmail = view.findViewById(R.id.til_email);
        etEmail = view.findViewById(R.id.et_email);
        tilPassword = view.findViewById(R.id.til_password);
        etPassword = view.findViewById(R.id.et_password);
        tilConfirmPassword = view.findViewById(R.id.til_confirm_password);
        etConfirmPassword = view.findViewById(R.id.et_confirm_password);
        checkboxPrivacy = view.findViewById(R.id.checkbox_privacy);

        return view;
    }

    public boolean validateInputs() {
        boolean isValid = true;
        String email = getEmail();
        String password = getPassword();
        String confirmPassword = getConfirmPassword();

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError(getString(R.string.error_invalid_email));
            isValid = false;
        } else {
            tilEmail.setError(null);
        }
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            tilPassword.setError(getString(R.string.error_password_too_short));
            isValid = false;
        } else {
            tilPassword.setError(null);
        }
        if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError(getString(R.string.error_passwords_not_match));
            isValid = false;
        } else {
            tilConfirmPassword.setError(null);
        }
        if (!isPrivacyAccepted()) {
            CustomToast.show(requireContext(), getString(R.string.error_privacy_required));
            isValid = false;
        }
        return isValid;
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
        return checkboxPrivacy.isChecked();
    }
}