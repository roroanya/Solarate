package com.example.solarate.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.solarate.R;
import com.example.solarate.adapters.MovieAdapter;
import com.example.solarate.models.Movie;
import com.example.solarate.network.ApiClient;
import com.example.solarate.network.ApiService;
import com.example.solarate.models.MovieResponse;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import retrofit2.Response;

public class FilmsFragment extends Fragment {

    private RecyclerView rvMovies;
    private MovieAdapter adapter;

    // THREADING: Menyiapkan Background Thread dan Main Thread
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_films, container, false);

        rvMovies = view.findViewById(R.id.rvMovies);

        // Gunakan GridLayoutManager (2 kolom) agar tampilannya seperti Letterboxd
        rvMovies.setLayoutManager(new GridLayoutManager(getContext(), 3));

        // Panggil fungsi untuk mengambil data
        loadMovies();

        return view;
    }

    private void loadMovies() {
        // THREADING: Eksekusi jaringan di Background Thread agar UI tidak freeze
        executor.execute(() -> {
            try {
                // Hubungkan ke API Service
                ApiService api = ApiClient.getClient().create(ApiService.class);

                // Eksekusi HTTP Request (Sinkron)
                Response<MovieResponse> response = api.getTrendingMovies("day").execute();

                // Jika request berhasil (Status 200 OK)
                if (response.isSuccessful() && response.body() != null) {
                    // Ambil list film dari dalam object "results"
                    List<Movie> movies = response.body().getResults();

                    // THREADING: Kembali ke Main Thread untuk update UI (RecyclerView)
                    handler.post(() -> {
                        if (getContext() == null) return;
                        adapter = new MovieAdapter(movies, getContext());
                        rvMovies.setAdapter(adapter);
                    });
                } else {
                    // Jika server error (404, 500, dll)
                    showError("Gagal mengambil data dari server. Code: " + response.code());
                }
            } catch (Exception e) {
                // Jika koneksi putus atau backend mati
                showError("Error koneksi: Pastikan Backend Python menyala! (" + e.getMessage() + ")");
            }
        });
    }

    // Saat kembali dari halaman detail, perbarui tanda "sudah ditonton" di poster
    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.refreshWatched();
            adapter.notifyDataSetChanged();
        }
    }

    private void showError(String message) {
        // Tampilkan pesan error di Main Thread
        handler.post(() -> Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show());
    }
}