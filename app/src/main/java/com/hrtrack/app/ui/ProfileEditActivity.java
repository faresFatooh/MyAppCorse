package com.hrtrack.app.ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hrtrack.app.R;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ProfileEditActivity extends AppCompatActivity {

    private ImageView ivProfile;
    private TextInputEditText etFullName, etEmail, etBirthDate, etJobTitle;
    private AutoCompleteTextView actvGender;
    private Button btnSave, btnChangePhoto;

    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        ivProfile = findViewById(R.id.ivProfile);
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etBirthDate = findViewById(R.id.etBirthDate);
        etJobTitle = findViewById(R.id.etJobTitle);
        actvGender = findViewById(R.id.actvGender);
        btnSave = findViewById(R.id.btnSave);
        btnChangePhoto = findViewById(R.id.btnChangePhoto);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        loadUserData();

        setupGenderDropdown();

        etBirthDate.setOnClickListener(v -> showDatePickerDialog());

        btnSave.setOnClickListener(v -> updateProfile());

        btnChangePhoto.setOnClickListener(v -> selectImage());
    }

    private void loadUserData() {
        if (currentUser != null) {
            db.collection("users").document(currentUser.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            etFullName.setText(documentSnapshot.getString("fullName"));
                            etEmail.setText(documentSnapshot.getString("email"));
                            etBirthDate.setText(documentSnapshot.getString("birthDate"));
                            etJobTitle.setText(documentSnapshot.getString("jobTitle"));

                            // تعيين الجنس
                            String gender = documentSnapshot.getString("gender");
                            actvGender.setText(gender, false);
                        }
                    });
        }
    }

    private void setupGenderDropdown() {
        String[] genders = new String[]{"Male", "Female", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                genders
        );
        actvGender.setAdapter(adapter);
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePicker = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    String selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth;
                    etBirthDate.setText(selectedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePicker.show();
    }

    private void updateProfile() {
        Map<String, Object> updates = new HashMap<>();
        updates.put("fullName", etFullName.getText().toString());
        updates.put("birthDate", etBirthDate.getText().toString());
        updates.put("gender", actvGender.getText().toString());
        updates.put("jobTitle", etJobTitle.getText().toString());

        db.collection("users").document(currentUser.getUid())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error updating profile", Toast.LENGTH_SHORT).show();
                });
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            ivProfile.setImageURI(imageUri);
            uploadImageToFirebase(imageUri);
        }
    }

    private void uploadImageToFirebase(Uri imageUri) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                .child("profile_images/" + currentUser.getUid() + ".jpg");

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        db.collection("users").document(currentUser.getUid())
                                .update("profileImageUrl", uri.toString());
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show();
                });
    }
}
