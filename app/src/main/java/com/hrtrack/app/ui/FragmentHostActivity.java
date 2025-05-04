package com.hrtrack.app.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.hrtrack.app.R;
import com.hrtrack.app.fragment.HolidaysFragment;
import com.hrtrack.app.fragment.NotificationsFragment;
import com.hrtrack.app.fragment.ReportsFragment;
import com.hrtrack.app.fragment.WorkHoursFragment;

public class FragmentHostActivity extends AppCompatActivity {

    public static final String EXTRA_FRAGMENT = "fragment_name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_host_activity);

        String fragmentName = getIntent().getStringExtra(EXTRA_FRAGMENT);
        Fragment fragment = null;

        if ("workHoursFragment".equals(fragmentName)) {
            fragment = new WorkHoursFragment();
        } else if ("holidaysFragment".equals(fragmentName)) {
            fragment = new HolidaysFragment();
        } else if ("reportsFragment".equals(fragmentName)) {
            fragment = new ReportsFragment();
        } else if ("notificationsFragment".equals(fragmentName)) {
            fragment = new NotificationsFragment();
        }

        if (fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
    }
}
