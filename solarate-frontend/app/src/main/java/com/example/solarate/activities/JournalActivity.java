package com.example.solarate.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.solarate.R;
import com.example.solarate.models.JournalDetail;
import com.example.solarate.models.Movie;
import com.example.solarate.network.ApiClient;
import com.example.solarate.network.ApiService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Response;

public class JournalActivity extends AppCompatActivity {

    private static final String EXTRA_SLUG = "JOURNAL_SLUG";

    public static Intent newIntent(Context ctx, String slug) {
        Intent i = new Intent(ctx, JournalActivity.class);
        i.putExtra(EXTRA_SLUG, slug);
        return i;
    }

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_journal);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        String slug = getIntent().getStringExtra(EXTRA_SLUG);
        if (slug == null) {
            finish();
            return;
        }
        load(slug);
    }

    private void load(String slug) {
        executor.execute(() -> {
            try {
                ApiService api = ApiClient.getClient().create(ApiService.class);
                Response<JournalDetail> res = api.getJournalDetail(slug).execute();
                if (res.isSuccessful() && res.body() != null) {
                    JournalDetail d = res.body();
                    handler.post(() -> { if (!isFinishing()) bind(d); });
                } else {
                    toast("Gagal memuat artikel. Code: " + res.code());
                }
            } catch (Exception e) {
                toast("Error koneksi: pastikan Backend Python menyala.");
            }
        });
    }

    private void bind(JournalDetail d) {
        ((TextView) findViewById(R.id.tvJournalDetailTitle)).setText(d.getTitle());
        ((TextView) findViewById(R.id.tvJournalDetailSubtitle)).setText(d.getSubtitle());
        Glide.with(this).load(d.getFirstCover())
                .placeholder(R.color.lb_dark).error(R.color.lb_dark)
                .into((ImageView) findViewById(R.id.ivJournalDetailCover));

        LinearLayout paragraphs = findViewById(R.id.llJournalParagraphs);
        if (d.getParagraphs() != null) {
            for (String p : d.getParagraphs()) {
                TextView tv = new TextView(this);
                tv.setText(p);
                tv.setTextColor(getResources().getColor(R.color.white, getTheme()));
                tv.setTextSize(15);
                tv.setLineSpacing(0, 1.2f);
                tv.setPadding(0, 0, 0, (int) (12 * getResources().getDisplayMetrics().density));
                paragraphs.addView(tv);
            }
        }

        LinearLayout films = findViewById(R.id.llJournalFilms);
        if (d.getFilms() != null && !d.getFilms().isEmpty()) {
            findViewById(R.id.tvJournalFilmsLabel).setVisibility(View.VISIBLE);
            for (Movie m : d.getFilms()) {
                View item = LayoutInflater.from(this).inflate(R.layout.item_profile_poster, films, false);
                Glide.with(this).load(m.getPosterUrl())
                        .placeholder(R.color.lb_dark).error(R.color.lb_black)
                        .into((ImageView) item.findViewById(R.id.ivProfilePoster));
                item.setOnClickListener(v -> startActivity(MovieDetailActivity.newIntent(this, m)));
                films.addView(item);
            }
        }
    }

    private void toast(String msg) {
        handler.post(() -> Toast.makeText(this, msg, Toast.LENGTH_LONG).show());
    }
}