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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.solarate.R;
import com.example.solarate.activities.DiaryActivity;
import com.example.solarate.activities.ListsActivity;
import com.example.solarate.activities.LoginActivity;
import com.example.solarate.activities.WatchlistActivity;
import com.google.android.material.tabs.TabLayout;

public class ProfileFragment extends Fragment {

    private TextView tvUsernameHeader;
    private Button btnLogout;
    private TabLayout profileTabs;
    private LinearLayout rowFilms, rowReviews, rowWatchlist;

    // Deklarasi ImageView untuk poster dummy
    private ImageView ivFav1, ivFav2, ivFav3, ivRecent1, ivRecent2;

    public ProfileFragment() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // 1. Inisialisasi semua komponen UI
        tvUsernameHeader = view.findViewById(R.id.tvProfileUsernameHeader);
        btnLogout = view.findViewById(R.id.btnLogout);
        profileTabs = view.findViewById(R.id.profileTabs);
        rowFilms = view.findViewById(R.id.rowFilms);
        rowReviews = view.findViewById(R.id.rowReviews);
        rowWatchlist = view.findViewById(R.id.rowWatchlist);

        // Inisialisasi ImageView untuk poster
        ivFav1 = view.findViewById(R.id.ivFav1);
        ivFav2 = view.findViewById(R.id.ivFav2);
        ivFav3 = view.findViewById(R.id.ivFav3);
        ivRecent1 = view.findViewById(R.id.ivRecent1);
        ivRecent2 = view.findViewById(R.id.ivRecent2);

        // 2. Ambil username dari SharedPreferences
        if (getActivity() != null) {
            SharedPreferences prefs = getActivity().getSharedPreferences("SolaratePrefs", Context.MODE_PRIVATE);
            String username = prefs.getString("USERNAME", "Username");
            tvUsernameHeader.setText(username);
        }

        // ==========================================
        // 3. LOAD GAMBAR POSTER DUMMY (MENGGUNAKAN GLIDE)
        // ==========================================
        // URL gambar poster dari TMDB (Contoh film terkenal)
        String urlFav1 = "https://image.tmdb.org/t/p/w500/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg"; // Interstellar
        String urlFav2 = "https://image.tmdb.org/t/p/w500/1g0dhYtq4irTY1GPXvft6k4YLjm.jpg"; // Spider-Man
        String urlFav3 = "https://image.tmdb.org/t/p/w500/d5NXSklXo0qyIYkgV94XAgMIckC.jpg"; // Dune
        String urlRecent1 = "https://image.tmdb.org/t/p/w500/8Gxv8gSFCU0XGDykEGv7zR1n2ua.jpg"; // Oppenheimer
        String urlRecent2 = "https://image.tmdb.org/t/p/w500/62HCnUTziyWcpDaBO2i1DX17ljH.jpg"; // Top Gun Maverick

        // Memuat gambar ke dalam ImageView
        Glide.with(this).load(urlFav1).into(ivFav1);
        Glide.with(this).load(urlFav2).into(ivFav2);
        Glide.with(this).load(urlFav3).into(ivFav3);
        Glide.with(this).load(urlRecent1).into(ivRecent1);
        Glide.with(this).load(urlRecent2).into(ivRecent2);

        // ==========================================
        // 4. Logika Klik pada TAB DI ATAS (Profile, Diary, Lists, Watchlist)
        // ==========================================
        profileTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();

                if (position == 0) {
                    // Tab Profile (sedang dibuka)
                } else if (position == 1) {
                    // Pindah ke DiaryActivity
                    startActivity(new Intent(getActivity(), DiaryActivity.class));
                } else if (position == 2) {
                    // Pindah ke ListsActivity
                    startActivity(new Intent(getActivity(), ListsActivity.class));
                } else if (position == 3) {
                    // Pindah ke WatchlistActivity
                    startActivity(new Intent(getActivity(), WatchlistActivity.class));
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }
            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });

        // ==========================================
        // 5. Logika Klik pada DAFTAR STATISTIK DI BAWAH
        // ==========================================
        rowFilms.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), com.example.solarate.activities.MainActivity.class);
            startActivity(intent);
        });

        rowReviews.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), com.example.solarate.activities.ReviewsActivity.class);
            startActivity(intent);
        });

        rowWatchlist.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), WatchlistActivity.class);
            startActivity(intent);
        });

        // ==========================================
        // 6. Logika Tombol Logout
        // ==========================================
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
}