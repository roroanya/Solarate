package com.example.solarate.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.solarate.R;
import com.example.solarate.adapters.MovieAdapter;
import com.example.solarate.models.ListDetail;
import com.example.solarate.models.Movie;
import com.example.solarate.network.ApiClient;
import com.example.solarate.network.ApiService;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Response;

/** Isi satu list: grid film dari backend (/api/lists/{slug}). Dibuka dari tab Lists di Profile. */
public class ListsActivity extends AppCompatActivity {

    private static final String EXTRA_SLUG = "LIST_SLUG";
    private static final String EXTRA_TITLE = "LIST_TITLE";

    public static Intent newIntent(Context ctx, String slug, String title) {
        Intent i = new Intent(ctx, ListsActivity.class);
        i.putExtra(EXTRA_SLUG, slug);
        i.putExtra(EXTRA_TITLE, title);
        return i;
    }

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    private RecyclerView rvLists;
    private TextView tvTitle, tvDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lists);

        tvTitle = findViewById(R.id.tvListsTitle);
        tvDescription = findViewById(R.id.tvListsDescription);
        rvLists = findViewById(R.id.rvLists);
        rvLists.setLayoutManager(new GridLayoutManager(this, 3));   // sama seperti beranda

        String slug = getIntent().getStringExtra(EXTRA_SLUG);
        String title = getIntent().getStringExtra(EXTRA_TITLE);
        if (slug == null) {
            finish();
            return;
        }
        if (title != null) tvTitle.setText(title);

        loadList(slug);
    }

    private void loadList(String slug) {
        executor.execute(() -> {
            try {
                ApiService api = ApiClient.getClient().create(ApiService.class);
                Response<ListDetail> res = api.getListDetail(slug).execute();
                if (res.isSuccessful() && res.body() != null) {
                    ListDetail d = res.body();
                    List<Movie> movies = d.getFilms().getResults();
                    handler.post(() -> {
                        if (isFinishing()) return;
                        tvTitle.setText(d.getTitle());
                        tvDescription.setText(d.getDescription() + "\nby " + d.getAuthor()
                                + "  •  " + d.getFilmCount() + " films");
                        // MovieAdapter yang sama dengan beranda -> klik poster otomatis ke halaman detail
                        rvLists.setAdapter(new MovieAdapter(movies, this));
                    });
                } else {
                    toast("Gagal memuat list. Code: " + res.code());
                }
            } catch (Exception e) {
                toast("Error koneksi: pastikan Backend Python menyala.");
            }
        });
    }

    private void toast(String msg) {
        handler.post(() -> Toast.makeText(this, msg, Toast.LENGTH_LONG).show());
    }
}