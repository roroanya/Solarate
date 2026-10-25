package com.example.solarate.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;
import com.example.solarate.R;
import com.example.solarate.activities.LoginActivity;
import com.example.solarate.activities.MovieDetailActivity;
import com.example.solarate.activities.ReviewsActivity;
import com.example.solarate.activities.SettingsActivity;
import com.example.solarate.activities.WatchedActivity;
import com.example.solarate.adapters.ListAdapter;
import com.example.solarate.adapters.DiaryAdapter;
import com.example.solarate.adapters.MovieAdapter;
import com.example.solarate.data.UserDataManager;
import com.example.solarate.models.ListSummary;
import com.example.solarate.models.Movie;
import com.example.solarate.models.Review;
import com.example.solarate.network.ApiClient;
import com.example.solarate.network.ApiService;
import com.google.android.material.tabs.TabLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Response;

/**
 * Halaman Profile (tab paling kanan di bottom navigation).
 * Header + tab (Profile, Diary, Lists, Watchlist) selalu tampil di atas, sedangkan isi di bawahnya
 * berupa 4 panel yang berganti di tempat (tanpa membuka halaman baru).
 */
public class ProfileFragment extends Fragment {

    private static final int MAX_RECENT = 10;   // maksimal poster di Recent Activity

    private TextView tvUsernameHeader;
    private Button btnLogout, btnSettings;
    private ImageView ivAvatar;
    private TextView tvBio;
    private TabLayout profileTabs;
    private LinearLayout rowWatched, rowReviews, rowWatchlist;
    private TextView tvWatchedCount, tvReviewsCount, tvWatchlistCount;

    // Panel isi: hanya satu yang tampil sesuai tab yang dipilih
    private View panelProfile, panelDiary, panelLists, panelWatchlist;
    private MovieAdapter watchlistAdapter;
    private DiaryAdapter diaryAdapter;
    private TextView tvDiaryEmpty;
    private TextView tvWatchlistTabCount, tvWatchlistTabEmpty;

    // Tab Lists: data dari backend (/api/lists), dimuat sekali saat tab pertama kali dibuka
    private RecyclerView rvLists;
    private TextView tvListsStatus;
    private boolean listsLoaded = false;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    private LinearLayout llFavorites, llRecent;
    private TextView tvFavoritesEmpty, tvRecentEmpty;

    public ProfileFragment() { }

    // ==========================================================
    // onCreateView: dipanggil sekali saat layout dibuat.
    // Tugasnya menghubungkan komponen XML ke Java dan memasang semua listener klik.
    // Data (username, bio, foto, film) TIDAK diisi di sini, tapi di onResume().
    // ==========================================================
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // 1. Inisialisasi semua komponen UI
        tvUsernameHeader = view.findViewById(R.id.tvProfileUsernameHeader);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnSettings = view.findViewById(R.id.btnSettings);
        ivAvatar = view.findViewById(R.id.ivProfileAvatar);
        tvBio = view.findViewById(R.id.tvProfileBio);
        profileTabs = view.findViewById(R.id.profileTabs);

        rowWatched = view.findViewById(R.id.rowWatched);
        rowReviews = view.findViewById(R.id.rowReviews);
        rowWatchlist = view.findViewById(R.id.rowWatchlist);
        tvWatchedCount = view.findViewById(R.id.tvWatchedCount);
        tvReviewsCount = view.findViewById(R.id.tvReviewsCount);
        tvWatchlistCount = view.findViewById(R.id.tvWatchlistCount);

        panelProfile = view.findViewById(R.id.panelProfile);
        panelDiary = view.findViewById(R.id.panelDiary);
        panelLists = view.findViewById(R.id.panelLists);
        panelWatchlist = view.findViewById(R.id.panelWatchlist);
        tvWatchlistTabCount = view.findViewById(R.id.tvWatchlistTabCount);
        tvWatchlistTabEmpty = view.findViewById(R.id.tvWatchlistTabEmpty);

        // Tab Lists: daftar list (kartu) dari API
        rvLists = view.findViewById(R.id.rvListsTab);
        tvListsStatus = view.findViewById(R.id.tvListsStatus);
        rvLists.setLayoutManager(new LinearLayoutManager(getContext()));

        // Tab Diary: daftar film per bulan berdasarkan tanggal nonton
        RecyclerView rvDiary = view.findViewById(R.id.rvDiaryTab);
        rvDiary.setLayoutManager(new LinearLayoutManager(getContext()));
        diaryAdapter = new DiaryAdapter(requireContext());
        rvDiary.setAdapter(diaryAdapter);
        tvDiaryEmpty = view.findViewById(R.id.tvDiaryEmpty);

        // Tab Watchlist: grid 3 kolom (sama dengan grid di Home)
        RecyclerView rvWatchlist = view.findViewById(R.id.rvWatchlistTab);
        rvWatchlist.setLayoutManager(new GridLayoutManager(getContext(), 3));
        watchlistAdapter = new MovieAdapter(new ArrayList<>(), requireContext());
        rvWatchlist.setAdapter(watchlistAdapter);

        llFavorites = view.findViewById(R.id.llFavorites);
        llRecent = view.findViewById(R.id.llRecent);
        tvFavoritesEmpty = view.findViewById(R.id.tvFavoritesEmpty);
        tvRecentEmpty = view.findViewById(R.id.tvRecentEmpty);

        // 2. Klik pada TAB DI ATAS (Profile, Diary, Lists, Watchlist).
        //    Tidak membuka halaman baru: header & tab tetap, hanya panel di bawahnya yang berganti.
        profileTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                showPanel(tab.getPosition());
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }
            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });

        // 3. Klik pada DAFTAR STATISTIK DI BAWAH
        rowWatched.setOnClickListener(v -> startActivity(new Intent(getActivity(), WatchedActivity.class)));
        rowReviews.setOnClickListener(v -> startActivity(new Intent(getActivity(), ReviewsActivity.class)));
        // Baris "Watchlist" juga hanya berpindah ke tab Watchlist (tidak membuka halaman baru)
        rowWatchlist.setOnClickListener(v -> selectTab(3));

        // 4. Tombol Settings -> buka halaman Settings (ubah foto, username, bio, email)
        btnSettings.setOnClickListener(v -> startActivity(new Intent(getActivity(), SettingsActivity.class)));

        // 5. Logout: kembali ke Login dan hapus semua activity sebelumnya dari back stack
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

    // ==========================================================
    // onResume: dipanggil setiap halaman Profile tampil lagi
    // (misalnya setelah kembali dari Settings atau halaman detail film).
    // Inilah kenapa perubahan dari Settings / tombol di detail film langsung terlihat.
    // ==========================================================
    @Override
    public void onResume() {
        super.onResume();
        refreshProfileData();
        refreshWatchlistPanel();
        refreshDiaryPanel();
    }

    /** Isi tab Diary dari review yang punya tanggal nonton (dikelompokkan per bulan). */
    private void refreshDiaryPanel() {
        diaryAdapter.setReviews(UserDataManager.getReviews(requireContext()));
        tvDiaryEmpty.setVisibility(diaryAdapter.isEmpty() ? View.VISIBLE : View.GONE);
    }

    // ==========================================================
    // showPanel: menampilkan panel sesuai tab (0=Profile, 1=Diary, 2=Lists, 3=Watchlist)
    // dan menyembunyikan yang lain, dengan efek fade singkat supaya terasa seperti transisi.
    // ==========================================================
    private void showPanel(int position) {
        if (position == 2) loadLists();   // Lists baru diambil dari API saat tab pertama kali dibuka

        View[] panels = {panelProfile, panelDiary, panelLists, panelWatchlist};
        for (int i = 0; i < panels.length; i++) {
            View p = panels[i];
            if (i == position) {
                p.setAlpha(0f);
                p.setVisibility(View.VISIBLE);
                p.animate().alpha(1f).setDuration(150).start();
            } else {
                p.animate().cancel();
                p.setVisibility(View.GONE);
            }
        }
    }

    /** Ambil daftar list dari backend (/api/lists) di background thread. Hanya sekali kalau berhasil. */
    private void loadLists() {
        if (listsLoaded) return;
        listsLoaded = true;
        tvListsStatus.setVisibility(View.VISIBLE);
        tvListsStatus.setText("Memuat lists...");
        executor.execute(() -> {
            try {
                ApiService api = ApiClient.getClient().create(ApiService.class);
                Response<List<ListSummary>> res = api.getLists().execute();
                if (res.isSuccessful() && res.body() != null) {
                    List<ListSummary> lists = res.body();
                    handler.post(() -> {
                        if (!isAdded() || getContext() == null) return;
                        rvLists.setAdapter(new ListAdapter(lists, getContext()));
                        tvListsStatus.setVisibility(lists.isEmpty() ? View.VISIBLE : View.GONE);
                        if (lists.isEmpty()) tvListsStatus.setText("Belum ada list.");
                    });
                } else {
                    failLists("Gagal memuat lists. Code: " + res.code());
                }
            } catch (Exception e) {
                failLists("Error koneksi: pastikan Backend Python menyala.");
            }
        });
    }

    private void failLists(String msg) {
        listsLoaded = false;   // supaya dicoba lagi saat tab dibuka berikutnya
        handler.post(() -> {
            if (!isAdded()) return;
            tvListsStatus.setVisibility(View.VISIBLE);
            tvListsStatus.setText(msg);
        });
    }

    /** Pindah ke tab tertentu lewat kode (otomatis memicu showPanel lewat listener). */
    private void selectTab(int position) {
        TabLayout.Tab tab = profileTabs.getTabAt(position);
        if (tab != null) tab.select();
    }

    /** Isi grid tab Watchlist dari data user (sama dengan tombol Watchlist di halaman detail). */
    private void refreshWatchlistPanel() {
        List<Movie> movies = UserDataManager.getMovies(requireContext(), UserDataManager.WATCHLIST);
        watchlistAdapter.setMovies(movies);
        tvWatchlistTabCount.setText(movies.size() + (movies.size() == 1 ? " film" : " films"));
        tvWatchlistTabEmpty.setVisibility(movies.isEmpty() ? View.VISIBLE : View.GONE);
    }

    // ==========================================================
    // bindHeader: mengisi bagian atas profile (username, bio, foto)
    // dari SharedPreferences "SolaratePrefs". Data ini ditulis oleh SettingsActivity:
    //   USERNAME    -> nama pengguna
    //   BIO         -> teks bio (kalau kosong dipakai teks default)
    //   AVATAR_PATH -> lokasi file foto profil di penyimpanan internal app
    // Kalau belum ada foto, yang tampil logo Solarate bawaan.
    // ==========================================================
    private void bindHeader(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences("SolaratePrefs", Context.MODE_PRIVATE);
        tvUsernameHeader.setText(prefs.getString("USERNAME", "Username"));

        String bio = prefs.getString("BIO", "");
        tvBio.setText(bio.isEmpty() ? "here's my museum of movies" : bio);

        String path = prefs.getString("AVATAR_PATH", null);
        File file = path == null ? null : new File(path);
        if (file != null && file.exists()) {
            // signature(lastModified) memaksa Glide memuat ulang kalau foto diganti
            // (nama file selalu sama, jadi tanpa ini yang tampil bisa foto lama dari cache).
            ivAvatar.setPadding(0, 0, 0, 0);
            Glide.with(this).load(file)
                    .signature(new ObjectKey(file.lastModified()))
                    .circleCrop()
                    .into(ivAvatar);
        } else {
            int pad = (int) (12 * ctx.getResources().getDisplayMetrics().density);
            ivAvatar.setPadding(pad, pad, pad, pad);
            ivAvatar.setImageResource(R.drawable.logo_solarate);
        }
    }

    // ==========================================================
    // refreshProfileData: memuat ulang isi panel Profile:
    // header, jumlah statistik, Favorites, dan Recent Activity.
    // Sumber datanya UserDataManager (satu sumber data yang sama dengan halaman Detail).
    // ==========================================================
    private void refreshProfileData() {
        Context ctx = requireContext();

        bindHeader(ctx);   // username, bio, dan foto profil

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
        Set<Integer> added = new HashSet<>();        // mencegah film yang sama muncul dua kali

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

        // Bintang review hanya muncul kalau film itu sudah di-review
        if (stars != null && !stars.isEmpty()) {
            tvStars.setText(stars);
            tvStars.setVisibility(View.VISIBLE);
        }

        item.setOnClickListener(v ->
                startActivity(MovieDetailActivity.newIntent(requireContext(), movie)));
        parent.addView(item);
    }
}