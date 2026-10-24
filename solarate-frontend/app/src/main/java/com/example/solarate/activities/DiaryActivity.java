package com.example.solarate.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.solarate.R;

public class DiaryActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Menghubungkan ke layout activity_diary.xml
        setContentView(R.layout.activity_diary);
    }
}