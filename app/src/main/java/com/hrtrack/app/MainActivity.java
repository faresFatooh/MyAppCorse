package com.hrtrack.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.hrtrack.app.adapter.ModuleAdapter;
import com.hrtrack.app.auth.LoginActivity;
import com.hrtrack.app.models.Module;
import com.hrtrack.app.ui.EditAccountActivity;
import com.hrtrack.app.ui.SettingsActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private RecyclerView rvModules;
    private FirebaseAuth mAuth;

    private List<Module> moduleList = new ArrayList<>();
    private ModuleAdapter moduleAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // إعداد Toggle لفتح وإغلاق القائمة الجانبية
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // إعداد RecyclerView مع GridLayoutManager
        rvModules = findViewById(R.id.rv_modules);
        rvModules.setLayoutManager(new GridLayoutManager(this, 2)); // 2 أعمدة

        // تهيئة قائمة الوحدات
        initializeModules();

        moduleAdapter = new ModuleAdapter(this, moduleList);
        rvModules.setAdapter(moduleAdapter);
    }

    // تهيئة الوحدات (يمكنك تعديلها وإضافة وحدات أخرى)
    private void initializeModules() {
        // افترض أن الأيقونات موجودة في res/drawable
        moduleList.add(new Module("Attendance", R.drawable.ic_attendance));
        moduleList.add(new Module("Task Manager", R.drawable.ic_task_manager));
        moduleList.add(new Module("Calendar", R.drawable.ic_calendar));
        moduleList.add(new Module("Reports", R.drawable.ic_reports));
        // يمكنك إضافة وحدات أخرى حسب احتياج التطبيق
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // التعامل مع عناصر القائمة الجانبية
        int itemId = item.getItemId();
        if (itemId == R.id.nav_home) {
            // الصفحة الرئيسية
        } else if (itemId == R.id.nav_edit_account) {
            startActivity(new Intent(MainActivity.this, EditAccountActivity.class));
        } else if (itemId == R.id.nav_settings) {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        } else if (itemId == R.id.nav_logout) {
            mAuth.signOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }
        drawerLayout.closeDrawers();
        return true;
    }
}
