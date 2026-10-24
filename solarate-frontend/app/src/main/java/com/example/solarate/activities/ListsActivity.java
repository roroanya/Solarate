package com.example.solarate.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.solarate.R;
import com.example.solarate.adapters.MovieAdapter;
import com.example.solarate.models.Movie;

import java.util.ArrayList;
import java.util.List;

public class ListsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lists);

        // 1. Setup RecyclerView
        RecyclerView rvLists = findViewById(R.id.rvLists);
        // Pakai GridLayoutManager 3 kolom (sama persis seperti beranda)
        rvLists.setLayoutManager(new GridLayoutManager(this, 3));

        // 2. Siapkan Data Dummy
        List<Movie> dummyMovies = new ArrayList<>();

        // Format: new Movie(ID_TMDB, "Judul", "URL Poster")
        // Kita pakai ID asli dari TMDB agar saat diklik, halaman detailnya bisa memuat data dari backend
        dummyMovies.add(new Movie(634649, "Spider-Man: No Way Home", "https://image.tmdb.org/t/p/w500/1g0dhYtq4irTY1GPXvft6k4YLjm.jpg"));
        dummyMovies.add(new Movie(438631, "Dune", "https://image.tmdb.org/t/p/w500/d5NXSklXo0qyIYkgV94XAgMIckC.jpg"));
        dummyMovies.add(new Movie(361743, "Top Gun: Maverick", "https://image.tmdb.org/t/p/w500/62HCnUTziyWcpDaBO2i1DX17ljH.jpg"));
        dummyMovies.add(new Movie(872585, "Oppenheimer", "https://image.tmdb.org/t/p/w500/8Gxv8gSFCU0XGDykEGv7zR1n2ua.jpg"));
        dummyMovies.add(new Movie(157336, "Interstellar", "https://image.tmdb.org/t/p/w500/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg"));
        dummyMovies.add(new Movie(76600, "Avatar: The Way of Water", "https://image.tmdb.org/t/p/w500/t6HIqrRAclMCA60NsSmeqe9RmNV.jpg"));
        dummyMovies.add(new Movie(299534, "Avengers: Endgame", "https://image.tmdb.org/t/p/w500/or06FN3Dka5tukK1e9sl16pB3iy.jpg"));
        dummyMovies.add(new Movie(603, "The Matrix", "https://image.tmdb.org/t/p/w500/f89U3ADr1oiB1s9GkdPOEpXUk5H.jpg"));
        dummyMovies.add(new Movie(155, "The Dark Knight", "https://image.tmdb.org/t/p/w500/qJ2tW6WMUDux911r6m7haRef0WH.jpg"));

        // 3. Panggil MovieAdapter (Adapter yang sama dengan beranda)
        // Karena pakai adapter yang sama, fungsi klik ke halaman detail (MovieDetailActivity) akan otomatis berjalan
        MovieAdapter adapter = new MovieAdapter(dummyMovies, this);
        rvLists.setAdapter(adapter);
    }
}