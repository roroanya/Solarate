package com.example.solarate.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.solarate.R;
import com.example.solarate.data.UserDataManager;
import com.example.solarate.models.Movie;
import com.example.solarate.models.Review;
import com.example.solarate.network.ApiClient;
import com.example.solarate.network.ApiService;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovieDetailActivity extends AppCompatActivity {

    // Key Bundle
    public static final String EXTRA_MOVIE_ID = "MOVIE_ID";
    public static final String EXTRA_MOVIE_TITLE = "MOVIE_TITLE";
    public static final String EXTRA_MOVIE_POSTER = "MOVIE_POSTER";

    // Komponen UI
    private ImageView ivBackdrop, ivPoster, ivBack;
    private TextView tvTitle, tvYearRuntime, tvDirector, tvProducer, tvRating, tvOverview;
    private RatingBar ratingBar;

    private ImageView icFavorite, icWatchlist, icWatch, icReview;
    private TextView lblFavorite, lblWatchlist, lblWatch, lblReview;

    // Film yang sedang ditampilkan (awalnya dari Bundle, lalu diganti data lengkap dari API)
    private Movie movie;

    /** Helper: membuat Intent + Bundle untuk membuka halaman detail dari mana saja. */
    public static Intent newIntent(Context context, Movie movie) {
        Intent intent = new Intent(context, MovieDetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_MOVIE_ID, String.valueOf(movie.getId()));
        bundle.putString(EXTRA_MOVIE_TITLE, movie.getTitle());
        bundle.putString(EXTRA_MOVIE_POSTER, movie.getPosterUrl());
        intent.putExtras(bundle);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        bindViews();
        ivBack.setOnClickListener(v -> finish());

        // MENERIMA BUNDLE DARI MOVIEADAPTER / PROFILE
        Bundle extras = getIntent().getExtras();
        int movieId = 0;
        String title = "";
        String poster = null;
        if (extras != null) {
            try {
                movieId = Integer.parseInt(extras.getString(EXTRA_MOVIE_ID, "0"));
            } catch (NumberFormatException ignored) { }
            title = extras.getString(EXTRA_MOVIE_TITLE, "");
            poster = extras.getString(EXTRA_MOVIE_POSTER);
        }
        if (movieId == 0) {
            Toast.makeText(this, "Film tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Data sementara
        movie = new Movie(movieId, title, poster);
        bindMovie(movie);

        setupButtons();
        refreshButtonStates();

        loadMovieDetail(movieId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (movie != null) refreshButtonStates();
    }

    private void bindViews() {
        ivBack = findViewById(R.id.ivDetailBack);
        ivBackdrop = findViewById(R.id.ivDetailBackdrop);
        ivPoster = findViewById(R.id.ivDetailPoster);
        tvTitle = findViewById(R.id.tvDetailTitle);
        tvYearRuntime = findViewById(R.id.tvDetailYearRuntime);
        tvDirector = findViewById(R.id.tvDetailDirector);
        tvProducer = findViewById(R.id.tvDetailProducer);
        tvRating = findViewById(R.id.tvDetailRating);
        tvOverview = findViewById(R.id.tvDetailOverview);
        ratingBar = findViewById(R.id.ratingBarDetail);

        icFavorite = findViewById(R.id.icFavorite);
        icWatchlist = findViewById(R.id.icWatchlist);
        icWatch = findViewById(R.id.icWatch);
        icReview = findViewById(R.id.icReview);
        lblFavorite = findViewById(R.id.lblFavorite);
        lblWatchlist = findViewById(R.id.lblWatchlist);
        lblWatch = findViewById(R.id.lblWatch);
        lblReview = findViewById(R.id.lblReview);
    }


    // TAMPILKAN DATA FILM
    private void bindMovie(Movie m) {
        tvTitle.setText(m.getTitle() == null ? "" : m.getTitle());

        // Tahun • durasi
        StringBuilder info = new StringBuilder();
        if (!m.getYearText().isEmpty()) info.append(m.getYearText());
        if (m.getRuntime() > 0) {
            if (info.length() > 0) info.append("  •  ");
            info.append(m.getRuntime()).append(" mins");
        }
        tvYearRuntime.setText(info.length() > 0 ? info.toString() : "—");

        tvDirector.setText("Directed by " + valueOrDash(m.getDirector()));
        tvProducer.setText("Produced by " + valueOrDash(m.getProducer()));

        // Rating bar: rating TMDB (0-10) dikonversi ke skala 5 bintang
        double rating10 = m.getRating();
        if (rating10 > 0) {
            float stars = (float) (rating10 / 2.0);
            ratingBar.setRating(stars);
            tvRating.setText(String.format(Locale.US, "%.1f / 5  (%.1f TMDB, %,d votes)",
                    stars, rating10, m.getVoteCount()));
        } else {
            ratingBar.setRating(0);
            tvRating.setText("Belum ada rating");
        }

        String overview = m.getOverview();
        tvOverview.setText(overview == null || overview.isEmpty() ? "Sinopsis belum tersedia." : overview);

        // Poster
        Glide.with(this)
                .load(m.getPosterUrl())
                .placeholder(R.color.lb_dark)
                .error(R.color.lb_black)
                .into(ivPoster);

        // Backdrop (fallback ke poster jika kosong)
        String backdrop = m.getBackdropUrl();
        if (backdrop == null || backdrop.isEmpty()) backdrop = m.getPosterUrl();
        Glide.with(this)
                .load(backdrop)
                .placeholder(R.color.lb_dark)
                .error(R.color.lb_black)
                .into(ivBackdrop);
    }

    private String valueOrDash(String s) {
        return (s == null || s.trim().isEmpty()) ? "—" : s;
    }


    // PANGGIL API (ASINKRON / THREADING lewat enqueue)
    private void loadMovieDetail(int movieId) {
        ApiService api = ApiClient.getClient().create(ApiService.class);

        api.getMovieDetail(movieId).enqueue(new Callback<Movie>() {
            @Override
            public void onResponse(Call<Movie> call, Response<Movie> response) {
                if (isFinishing() || isDestroyed()) return;

                if (response.isSuccessful() && response.body() != null) {
                    Movie loaded = response.body();
                    // Jaga-jaga: pertahankan poster dari Bundle jika API tidak mengirim
                    if (loaded.getPosterUrl() == null || loaded.getPosterUrl().isEmpty()) {
                        loaded.setPosterUrl(movie.getPosterUrl());
                    }
                    movie = loaded;
                    bindMovie(movie);
                    refreshButtonStates();
                } else {
                    Toast.makeText(MovieDetailActivity.this, "Gagal memuat detail film", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Movie> call, Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                Toast.makeText(MovieDetailActivity.this, "Error koneksi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // TOMBOL: FAVORITE, WATCHLIST, WATCH, REVIEW
    private void setupButtons() {
        findViewById(R.id.btnFavorite).setOnClickListener(v -> {
            boolean added = UserDataManager.toggle(this, UserDataManager.FAVORITES, movie);
            Toast.makeText(this, added ? "Ditambahkan ke Favorites" : "Dihapus dari Favorites",
                    Toast.LENGTH_SHORT).show();
            refreshButtonStates();
        });

        findViewById(R.id.btnWatchlist).setOnClickListener(v -> {
            boolean added = UserDataManager.toggle(this, UserDataManager.WATCHLIST, movie);
            Toast.makeText(this, added ? "Ditambahkan ke Watchlist" : "Dihapus dari Watchlist",
                    Toast.LENGTH_SHORT).show();
            refreshButtonStates();
        });

        findViewById(R.id.btnWatch).setOnClickListener(v -> {
            boolean added = UserDataManager.toggle(this, UserDataManager.WATCHED, movie);
            Toast.makeText(this, added ? "Ditandai sudah ditonton" : "Tanda ditonton dihapus",
                    Toast.LENGTH_SHORT).show();
            refreshButtonStates();
        });

        findViewById(R.id.btnReview).setOnClickListener(v -> showReviewDialog());
    }

    /** Update warna ikon + label sesuai status film di data user. */
    private void refreshButtonStates() {
        int id = movie.getId();

        applyState(icFavorite, lblFavorite,
                UserDataManager.contains(this, UserDataManager.FAVORITES, id),
                R.color.lb_orange, "Favorite", "Favorited");

        applyState(icWatchlist, lblWatchlist,
                UserDataManager.contains(this, UserDataManager.WATCHLIST, id),
                R.color.lb_blue, "Watchlist", "In Watchlist");

        applyState(icWatch, lblWatch,
                UserDataManager.contains(this, UserDataManager.WATCHED, id),
                R.color.lb_green, "Watch", "Watched");

        applyState(icReview, lblReview,
                UserDataManager.getReview(this, id) != null,
                R.color.white, "Review", "Reviewed");
    }

    private void applyState(ImageView icon, TextView label, boolean active,
                            int activeColorRes, String offText, String onText) {
        int color = ContextCompat.getColor(this, active ? activeColorRes : R.color.lb_gray);
        icon.setColorFilter(color);
        label.setTextColor(color);
        label.setText(active ? onText : offText);
    }


    // POP UP FORM REVIEW
    private void showReviewDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_review, null);
        TextView tvMovie = view.findViewById(R.id.tvReviewMovie);
        RatingBar rbReview = view.findViewById(R.id.rbReview);
        EditText etText = view.findViewById(R.id.etReviewText);

        String yearText = movie.getYearText();
        tvMovie.setText(movie.getTitle() + (yearText.isEmpty() ? "" : " (" + yearText + ")"));

        Review existing = UserDataManager.getReview(this, movie.getId());
        if (existing != null) {
            rbReview.setRating(existing.getRating());
            etText.setText(existing.getText());
        }

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                .setTitle("Review")
                .setView(view)
                .setPositiveButton("Save", null)   // di-override di bawah supaya bisa validasi
                .setNegativeButton("Cancel", null);

        if (existing != null) {
            builder.setNeutralButton("Delete", (d, w) -> {
                UserDataManager.deleteReview(this, movie.getId());
                Toast.makeText(this, "Review dihapus", Toast.LENGTH_SHORT).show();
                refreshButtonStates();
            });
        }

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            float rating = rbReview.getRating();
            String text = etText.getText().toString().trim();

            if (rating == 0 && text.isEmpty()) {
                Toast.makeText(this, "Isi rating atau tulis review dulu", Toast.LENGTH_SHORT).show();
                return;
            }

            UserDataManager.saveReview(this, new Review(movie, rating, text));
            Toast.makeText(this, "Review disimpan", Toast.LENGTH_SHORT).show();
            refreshButtonStates();
            dialog.dismiss();
        }));
        dialog.show();
    }
}
