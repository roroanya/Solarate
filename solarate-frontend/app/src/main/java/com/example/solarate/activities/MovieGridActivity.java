package com.example.solarate.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.solarate.R;
import com.example.solarate.adapters.MovieAdapter;
import com.example.solarate.data.UserDataManager;
import com.example.solarate.models.Movie;

import java.util.ArrayList;
import java.util.List;

/**
 * Base activity untuk halaman grid film milik user (Watched, Watchlist).
 */
public abstract class MovieGridActivity extends AppCompatActivity {

    protected abstract String getListName();   // UserDataManager.WATCHED / WATCHLIST
    protected abstract String getScreenTitle();
    protected abstract String getEmptyMessage();

    private MovieAdapter adapter;
    private TextView tvCount, tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_grid);

        ((TextView) findViewById(R.id.tvGridTitle)).setText(getScreenTitle());
        tvCount = findViewById(R.id.tvGridCount);
        tvEmpty = findViewById(R.id.tvGridEmpty);
        tvEmpty.setText(getEmptyMessage());

        RecyclerView rv = findViewById(R.id.rvGrid);
        rv.setLayoutManager(new GridLayoutManager(this, 3));
        adapter = new MovieAdapter(new ArrayList<>(), this);
        rv.setAdapter(adapter);
    }

    // Dimuat ulang setiap kali halaman tampil (mis. setelah kembali dari halaman detail)
    @Override
    protected void onResume() {
        super.onResume();
        List<Movie> movies = UserDataManager.getMovies(this, getListName());
        adapter.setMovies(movies);
        tvCount.setText(movies.size() + (movies.size() == 1 ? " film" : " films"));
        tvEmpty.setVisibility(movies.isEmpty() ? View.VISIBLE : View.GONE);
    }
}