package com.hrtrack.app.auth;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hrtrack.app.MainActivity;
import com.hrtrack.app.R;
import com.hrtrack.app.fragment.AccountInfoFragment;
import com.hrtrack.app.fragment.PersonalInfoFragment;
import com.hrtrack.app.fragment.WorkInfoFragment;
import com.hrtrack.app.utils.CustomToast;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private int currentStep = 0;
    private Fragment[] fragments;
    private Button btnPrev, btnNext;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private ProgressDialog progressDialog;

    private PersonalInfoFragment personalInfoFragment;
    private WorkInfoFragment workInfoFragment;
    private AccountInfoFragment accountInfoFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        btnPrev = findViewById(R.id.btn_prev);
        btnNext = findViewById(R.id.btn_next);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Processing...");
        progressDialog.setCancelable(false);

        personalInfoFragment = new PersonalInfoFragment();
        workInfoFragment = new WorkInfoFragment();
        accountInfoFragment = new AccountInfoFragment();

        fragments = new Fragment[]{ personalInfoFragment, workInfoFragment, accountInfoFragment };
        loadFragment(fragments[currentStep]);
        updateNavigation();

        btnPrev.setOnClickListener(v -> {
            if (currentStep > 0) {
                currentStep--;
                loadFragment(fragments[currentStep]);
                updateNavigation();
            }
        });

        btnNext.setOnClickListener(v -> {
            if (currentStep == 1 && !workInfoFragment.validateInputs()) {
                return;
            }
            if (currentStep < fragments.length - 1) {
                currentStep++;
                loadFragment(fragments[currentStep]);
                updateNavigation();
            } else {
                submitData();
            }
        });
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    private void updateNavigation() {
        btnPrev.setVisibility(currentStep == 0 ? View.GONE : View.VISIBLE);
        btnNext.setText(currentStep == fragments.length - 1 ? "Submit" : "Next");
    }

    private void submitData() {
        String fullName = personalInfoFragment.getFullName();
        String birthDate = personalInfoFragment.getBirthDate();
        String gender = personalInfoFragment.getGender();
        String jobTitle = workInfoFragment.getJobTitle();
        List<Map<String, String>> workingSchedule = workInfoFragment.getWorkingSchedule();
        String email = accountInfoFragment.getEmail();
        String password = accountInfoFragment.getPassword();
        String confirmPassword = accountInfoFragment.getConfirmPassword();

        if (!validateInputs(fullName, birthDate, gender, jobTitle, workingSchedule, email, password, confirmPassword)) {
            return;
        }

        Map<String, Object> userData = new HashMap<>();
        userData.put("fullName", fullName);
        userData.put("birthDate", birthDate);
        userData.put("gender", gender);
        userData.put("jobTitle", jobTitle);
        userData.put("workingSchedule", workingSchedule);
        userData.put("email", email);
        userData.put("profileImageUrl", "profileImageUrl");

        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        String userId = mAuth.getCurrentUser().getUid();
                        firestore.collection("users").document(userId)
                                .set(userData)
                                .addOnCompleteListener(storeTask -> {
                                    if (storeTask.isSuccessful()) {
                                        CustomToast.show(this, "Registration Successful!");
                                        updateUI(mAuth.getCurrentUser());
                                    } else {
                                        CustomToast.show(this, "Error: " + storeTask.getException().getMessage());
                                    }
                                });
                    } else {
                        CustomToast.show(this, "Registration Failed: " + task.getException().getMessage());
                    }
                });
    }

    private boolean validateInputs(String fullName, String birthDate, String gender, String jobTitle,
                                   List<Map<String, String>> workingSchedule, String email, String password, String confirmPassword) {
        if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(birthDate) || TextUtils.isEmpty(gender)) {
            CustomToast.show(this, "Please complete personal information");
            return false;
        }
        if (TextUtils.isEmpty(jobTitle)) {
            CustomToast.show(this, "Please enter your job title");
            return false;
        }
        if (workingSchedule.isEmpty()) {
            CustomToast.show(this, "Please select at least one working day with valid times");
            return false;
        }
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            CustomToast.show(this, "Please complete account information");
            return false;
        }
        if (!password.equals(confirmPassword)) {
            CustomToast.show(this, "Passwords do not match");
            return false;
        }
        if (!accountInfoFragment.isPrivacyAccepted()) {
            CustomToast.show(this, "You must accept the Privacy Policy");
            return false;
        }
        return true;
    }

    private void updateUI(FirebaseUser user) {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}