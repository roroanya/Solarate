package com.example.solarate.activities;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.solarate.R;
import com.example.solarate.models.Movie;
import com.example.solarate.network.ApiClient;
import com.example.solarate.network.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovieDetailActivity extends AppCompatActivity {

    // Deklarasi komponen UI
    private TextView tvTitle, tvRating, tvOverview;
    private ImageView ivBackdrop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        // 1. Inisialisasi komponen UI berdasarkan ID di activity_movie_detail.xml
        tvTitle = findViewById(R.id.tvDetailTitle);
        tvRating = findViewById(R.id.tvDetailRating);
        tvOverview = findViewById(R.id.tvDetailOverview);
        ivBackdrop = findViewById(R.id.ivDetailBackdrop);

        // 2. MENERIMA BUNDLE DARI MOVIEADAPTER
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            // Ambil data yang dikirim dari halaman Films
            String movieIdStr = extras.getString("MOVIE_ID");
            String movieTitle = extras.getString("MOVIE_TITLE");

            if (movieIdStr != null && movieTitle != null) {
                int movieId = Integer.parseInt(movieIdStr);

                // Set judul sementara sebelum API selesai loading
                tvTitle.setText(movieTitle);

                // 3. PANGGIL API UNTUK MENDAPATKAN DETAIL LENGKAP
                loadMovieDetail(movieId);
            }
        }
    }

    private void loadMovieDetail(int movieId) {
        ApiService api = ApiClient.getClient().create(ApiService.class);

        // Memanggil API secara Asinkron menggunakan enqueue (Threading)
        api.getMovieDetail(movieId).enqueue(new Callback<Movie>() {
            @Override
            public void onResponse(Call<Movie> call, Response<Movie> response) {
                // Jika request sukses (Kode 200)
                if (response.isSuccessful() && response.body() != null) {
                    Movie movie = response.body();

                    // Update UI dengan data lengkap dari API
                    tvTitle.setText(movie.getTitle());
                    tvRating.setText("★ " + movie.getRating());
                    tvOverview.setText(movie.getOverview());

                    // Load gambar backdrop dari URL backend
                    // Kita pakai backdrop_url karena ukurannya lebih lebar (cocok untuk header)
                    String imageUrl = movie.getBackdropUrl();

                    // Cek jika backdrop_url kosong, fallback ke poster_url
                    if (imageUrl == null || imageUrl.isEmpty()) {
                        imageUrl = movie.getPosterUrl();
                    }

                    Glide.with(MovieDetailActivity.this)
                            .load(imageUrl)
                            .placeholder(R.color.lb_dark) // Warna saat loading
                            .error(R.color.lb_black)      // Warna jika gagal load
                            .into(ivBackdrop);

                } else {
                    Toast.makeText(MovieDetailActivity.this, "Gagal memuat detail film", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Movie> call, Throwable t) {
                // Jika koneksi terputus atau server mati
                Toast.makeText(MovieDetailActivity.this, "Error koneksi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}