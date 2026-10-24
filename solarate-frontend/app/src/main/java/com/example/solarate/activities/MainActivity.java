package com.example.solarate.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.solarate.R;
import com.example.solarate.fragments.FilmsFragment;
import com.example.solarate.fragments.JournalFragment;
import com.example.solarate.fragments.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);

        // Tampilkan FilmsFragment pertama kali
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new FilmsFragment()).commit();

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_films) selectedFragment = new FilmsFragment();
            else if (id == R.id.nav_journal) selectedFragment = new JournalFragment();
            else if (id == R.id.nav_profile) selectedFragment = new ProfileFragment();

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment).commit();
            }
            return true;
        });
    }
}