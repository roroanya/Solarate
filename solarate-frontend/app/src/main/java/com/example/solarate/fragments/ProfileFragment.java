package com.example.solarate.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.solarate.R;
import com.example.solarate.activities.DiaryActivity;
import com.example.solarate.activities.ListsActivity;
import com.example.solarate.activities.LoginActivity;
import com.example.solarate.activities.MovieDetailActivity;
import com.example.solarate.activities.ReviewsActivity;
import com.example.solarate.activities.WatchedActivity;
import com.example.solarate.activities.WatchlistActivity;
import com.example.solarate.data.UserDataManager;
import com.example.solarate.models.Movie;
import com.example.solarate.models.Review;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProfileFragment extends Fragment {

    private static final int MAX_RECENT = 10;

    private TextView tvUsernameHeader;
    private Button btnLogout;
    private TabLayout profileTabs;
    private LinearLayout rowWatched, rowReviews, rowWatchlist;
    private TextView tvWatchedCount, tvReviewsCount, tvWatchlistCount;

    private LinearLayout llFavorites, llRecent;
    private TextView tvFavoritesEmpty, tvRecentEmpty;

    public ProfileFragment() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // 1. Inisialisasi semua komponen UI
        tvUsernameHeader = view.findViewById(R.id.tvProfileUsernameHeader);
        btnLogout = view.findViewById(R.id.btnLogout);
        profileTabs = view.findViewById(R.id.profileTabs);

        rowWatched = view.findViewById(R.id.rowWatched);
        rowReviews = view.findViewById(R.id.rowReviews);
        rowWatchlist = view.findViewById(R.id.rowWatchlist);
        tvWatchedCount = view.findViewById(R.id.tvWatchedCount);
        tvReviewsCount = view.findViewById(R.id.tvReviewsCount);
        tvWatchlistCount = view.findViewById(R.id.tvWatchlistCount);

        llFavorites = view.findViewById(R.id.llFavorites);
        llRecent = view.findViewById(R.id.llRecent);
        tvFavoritesEmpty = view.findViewById(R.id.tvFavoritesEmpty);
        tvRecentEmpty = view.findViewById(R.id.tvRecentEmpty);

        // 2. Ambil username dari SharedPreferences
        if (getActivity() != null) {
            SharedPreferences prefs = getActivity().getSharedPreferences("SolaratePrefs", Context.MODE_PRIVATE);
            tvUsernameHeader.setText(prefs.getString("USERNAME", "Username"));
        }

        // 3. Klik pada TAB DI ATAS (Profile, Diary, Lists, Watchlist)
        profileTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if (position == 1) {
                    startActivity(new Intent(getActivity(), DiaryActivity.class));
                } else if (position == 2) {
                    startActivity(new Intent(getActivity(), ListsActivity.class));
                } else if (position == 3) {
                    startActivity(new Intent(getActivity(), WatchlistActivity.class));
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }
            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });

        // 4. Klik pada DAFTAR STATISTIK DI BAWAH
        rowWatched.setOnClickListener(v -> startActivity(new Intent(getActivity(), WatchedActivity.class)));
        rowReviews.setOnClickListener(v -> startActivity(new Intent(getActivity(), ReviewsActivity.class)));
        rowWatchlist.setOnClickListener(v -> startActivity(new Intent(getActivity(), WatchlistActivity.class)));

        // 5. Logout
        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            if (getActivity() != null) {
                getActivity().finish();
            }
        });

        return view;
    }

    // Data profile dimuat ulang setiap kali halaman tampil, supaya selalu sinkron
    // dengan tombol Favorite / Watchlist / Watch / Review di halaman detail.
    @Override
    public void onResume() {
        super.onResume();

        // Kembalikan tab ke "Profile" setelah balik dari Diary/Lists/Watchlist
        TabLayout.Tab first = profileTabs.getTabAt(0);
        if (first != null && !first.isSelected()) first.select();

        refreshProfileData();
    }

    private void refreshProfileData() {
        Context ctx = requireContext();

        List<Movie> favorites = UserDataManager.getMovies(ctx, UserDataManager.FAVORITES);
        List<Movie> watched = UserDataManager.getMovies(ctx, UserDataManager.WATCHED);
        List<Movie> watchlist = UserDataManager.getMovies(ctx, UserDataManager.WATCHLIST);
        List<Review> reviews = UserDataManager.getReviews(ctx);

        // ---- Jumlah di daftar statistik ----
        tvWatchedCount.setText(String.valueOf(watched.size()));
        tvReviewsCount.setText(String.valueOf(reviews.size()));
        tvWatchlistCount.setText(String.valueOf(watchlist.size()));

        // ---- FAVORITES ----
        llFavorites.removeAllViews();
        for (Movie m : favorites) {
            addPoster(llFavorites, m, null);
        }
        tvFavoritesEmpty.setVisibility(favorites.isEmpty() ? View.VISIBLE : View.GONE);

        // ---- RECENT ACTIVITY: film yang baru ditonton, lalu film yang baru di-review ----
        llRecent.removeAllViews();
        List<Object[]> recent = new ArrayList<>();   // {Movie, Review atau null}
        Set<Integer> added = new HashSet<>();

        for (Movie m : watched) {
            if (recent.size() >= MAX_RECENT) break;
            recent.add(new Object[]{m, UserDataManager.getReview(ctx, m.getId())});
            added.add(m.getId());
        }
        for (Review r : reviews) {
            if (recent.size() >= MAX_RECENT) break;
            if (added.add(r.getMovieId())) {
                recent.add(new Object[]{r.toMovie(), r});
            }
        }
        for (Object[] item : recent) {
            Review r = (Review) item[1];
            addPoster(llRecent, (Movie) item[0], r != null ? r.getStars() : null);
        }
        tvRecentEmpty.setVisibility(recent.isEmpty() ? View.VISIBLE : View.GONE);
    }

    /** Tambah satu poster (klik -> halaman detail) ke baris horizontal. */
    private void addPoster(LinearLayout parent, Movie movie, @Nullable String stars) {
        View item = LayoutInflater.from(getContext()).inflate(R.layout.item_profile_poster, parent, false);
        ImageView iv = item.findViewById(R.id.ivProfilePoster);
        TextView tvStars = item.findViewById(R.id.tvProfileStars);

        Glide.with(this)
                .load(movie.getPosterUrl())
                .placeholder(R.color.lb_dark)
                .error(R.color.lb_black)
                .into(iv);

        if (stars != null && !stars.isEmpty()) {
            tvStars.setText(stars);
            tvStars.setVisibility(View.VISIBLE);
        }

        item.setOnClickListener(v ->
                startActivity(MovieDetailActivity.newIntent(requireContext(), movie)));
        parent.addView(item);
    }
}
