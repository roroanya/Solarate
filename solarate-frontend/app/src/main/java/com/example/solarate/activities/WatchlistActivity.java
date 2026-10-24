package com.example.solarate.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.solarate.R;

public class WatchlistActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Menghubungkan ke layout activity_watchlist.xml
        setContentView(R.layout.activity_watchlist);
    }
}