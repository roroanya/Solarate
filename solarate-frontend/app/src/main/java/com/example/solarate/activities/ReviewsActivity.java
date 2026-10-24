package com.example.solarate.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.solarate.R;
import com.example.solarate.adapters.ReviewAdapter;
import com.example.solarate.data.UserDataManager;
import com.example.solarate.models.Review;

import java.util.List;

/** Daftar review milik user. Terisi dari tombol "Review" di halaman detail film. */
public class ReviewsActivity extends AppCompatActivity {

    private ReviewAdapter adapter;
    private TextView tvCount, tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);

        tvCount = findViewById(R.id.tvReviewsCount);
        tvEmpty = findViewById(R.id.tvReviewsEmpty);

        RecyclerView rv = findViewById(R.id.rvReviews);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReviewAdapter(this);
        rv.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        List<Review> reviews = UserDataManager.getReviews(this);
        adapter.setReviews(reviews);
        tvCount.setText(reviews.size() + (reviews.size() == 1 ? " review" : " reviews"));
        tvEmpty.setVisibility(reviews.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
