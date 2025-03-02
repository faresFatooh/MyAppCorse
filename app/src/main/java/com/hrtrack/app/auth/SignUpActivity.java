package com.hrtrack.app.auth;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hrtrack.app.MainActivity;
import com.hrtrack.app.R;
import com.hrtrack.app.fragmint.AccountInfoFragment;
import com.hrtrack.app.fragmint.PersonalInfoFragment;
import com.hrtrack.app.fragmint.WorkInfoFragment;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private int currentStep = 0;
    private Fragment[] fragments;
    private Button btnPrev, btnNext;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private ProgressDialog progressDialog;

    // لتجميع البيانات من الفِراغمنتس
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

        // إنشاء الفِراغمنتس
        personalInfoFragment = new PersonalInfoFragment();
        workInfoFragment = new WorkInfoFragment();
        accountInfoFragment = new AccountInfoFragment();

        fragments = new Fragment[]{ personalInfoFragment, workInfoFragment, accountInfoFragment };

        // عرض الفِراغمنت الأول
        loadFragment(fragments[currentStep]);

        // إعداد أزرار التنقل
        btnPrev.setOnClickListener(v -> {
            if (currentStep > 0) {
                currentStep--;
                loadFragment(fragments[currentStep]);
                updateNavigation();
            }
        });

        btnNext.setOnClickListener(v -> {
            if (currentStep < fragments.length - 1) {
                currentStep++;
                loadFragment(fragments[currentStep]);
                updateNavigation();
            } else {
                // آخر خطوة: تقديم البيانات
                submitData();
            }
        });

        updateNavigation();
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
        // جمع بيانات الفِراغمنتس
        String fullName = personalInfoFragment.getFullName();
        String birthDate = personalInfoFragment.getBirthDate();
        String gender = personalInfoFragment.getGender();

        String jobTitle = workInfoFragment.getJobTitle();
        Map<String, Object> workingSchedule = workInfoFragment.getWorkingSchedule();

        String email = accountInfoFragment.getEmail();
        String password = accountInfoFragment.getPassword();
        String confirmPassword = accountInfoFragment.getConfirmPassword();

        // التحقق الأساسي
        if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(birthDate) || TextUtils.isEmpty(gender)) {
            Toast.makeText(this, "Please complete personal information", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(jobTitle)) {
            Toast.makeText(this, "Please enter your job title", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "Please complete account information", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!accountInfoFragment.isPrivacyAccepted()) {
            Toast.makeText(this, "You must accept the Privacy Policy", Toast.LENGTH_SHORT).show();
            return;
        }

        // تجميع بيانات المستخدم
        Map<String, Object> userData = new HashMap<>();
        userData.put("fullName", fullName);
        userData.put("birthDate", birthDate);
        userData.put("gender", gender);
        userData.put("jobTitle", jobTitle);
        userData.put("workingSchedule", workingSchedule);
        userData.put("email", email);

        progressDialog.show();

        // إنشاء حساب المستخدم باستخدام FirebaseAuth
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            String userId = mAuth.getCurrentUser().getUid();
                            // تخزين بيانات المستخدم في Firestore
                            firestore.collection("users").document(userId)
                                    .set(userData)
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            Toast.makeText(SignUpActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                                            updateUI(mAuth.getCurrentUser());
                                        } else {
                                            Toast.makeText(SignUpActivity.this, "Error: " + task1.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            Toast.makeText(SignUpActivity.this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    private void updateUI(FirebaseUser user) {
        // الانتقال إلى MainActivity في حال تسجيل الدخول بنجاح
        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}

