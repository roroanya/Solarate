package com.example.solarate.activities;

import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.solarate.R;

public class ReviewsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);

        // 1. Inisialisasi ImageView berdasarkan ID di XML
        ImageView ivReview1 = findViewById(R.id.ivReview1);
        ImageView ivReview2 = findViewById(R.id.ivReview2);

        // 2. Siapkan URL gambar dummy (Poster Film)
        String url1 = "https://image.tmdb.org/t/p/w500/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg"; // Poster Interstellar
        String url2 = "https://image.tmdb.org/t/p/w500/8Gxv8gSFCU0XGDykEGv7zR1n2ua.jpg"; // Poster Oppenheimer

        // 3. Load gambar menggunakan Glide
        Glide.with(this).load(url1).into(ivReview1);
        Glide.with(this).load(url2).into(ivReview2);
    }
}