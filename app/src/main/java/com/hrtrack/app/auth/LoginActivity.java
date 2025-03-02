package com.hrtrack.app.auth;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.hrtrack.app.MainActivity;
import com.hrtrack.app.R;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText email, password;
    private Button loginBtn;
    private TextView signUp;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        email = findViewById(R.id.login_ed_email);
        password = findViewById(R.id.login_ed_password);
        loginBtn = findViewById(R.id.login_btn);
        signUp = findViewById(R.id.tv_signUp);

        // إعداد ProgressDialog لإظهار مؤشر التحميل أثناء تسجيل الدخول
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateAndLogin();
            }
        });

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToSignUp();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // التحقق مما إذا كان المستخدم مسجل دخول بالفعل
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            updateUI(currentUser);
        }
    }

    /**
     * التحقق من المدخلات ثم بدء عملية تسجيل الدخول
     */
    private void validateAndLogin() {
        String emailInput = email.getText().toString().trim();
        String passwordInput = password.getText().toString().trim();

        // التحقق من المدخلات
        if (emailInput.isEmpty()) {
            showCustomToast("Please enter your email.");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
            showCustomToast("Please enter a valid email.");
            return;
        }
        if (passwordInput.isEmpty()) {
            showCustomToast("Please enter your password.");
            return;
        }
        if (passwordInput.length() < 6) {
            showCustomToast("Password should be at least 6 characters.");
            return;
        }

        // تعطيل الزر أثناء عملية تسجيل الدخول
        loginBtn.setEnabled(false);
        progressDialog.show();

        // محاولة تسجيل الدخول باستخدام Firebase
        mAuth.signInWithEmailAndPassword(emailInput, passwordInput)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // إعادة تمكين الزر وإخفاء مؤشر التحميل
                        loginBtn.setEnabled(true);
                        progressDialog.dismiss();

                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            showCustomToast("Authentication failed. " +
                                    (task.getException() != null ? task.getException().getMessage() : ""));
                        }
                    }
                });
    }

    /**
     * عرض رسالة خطأ باستخدام Custom Toast
     */
    private void showCustomToast(String message) {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast, findViewById(R.id.custom_toast_container));
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView text = layout.findViewById(R.id.custom_toast_text);
        text.setText(message);
        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }

    private void updateUI(FirebaseUser user) {
        // الانتقال إلى MainActivity في حال تسجيل الدخول بنجاح
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void goToSignUp() {
        Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
        startActivity(intent);
        finish();
    }
}
