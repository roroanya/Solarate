package com.example.solarate.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.solarate.R;
import com.example.solarate.adapters.JournalAdapter;
import com.example.solarate.models.JournalSummary;
import com.example.solarate.network.ApiClient;
import com.example.solarate.network.ApiService;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Response;

public class JournalFragment extends Fragment {

    private RecyclerView rv;
    private TextView tvStatus;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_journal, container, false);
        rv = view.findViewById(R.id.rvJournal);
        tvStatus = view.findViewById(R.id.tvJournalStatus);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        loadJournal();
        return view;
    }

    private void loadJournal() {
        executor.execute(() -> {
            try {
                ApiService api = ApiClient.getClient().create(ApiService.class);
                Response<List<JournalSummary>> res = api.getJournal().execute();
                if (res.isSuccessful() && res.body() != null) {
                    List<JournalSummary> list = res.body();
                    handler.post(() -> {
                        if (!isAdded() || getContext() == null) return;
                        rv.setAdapter(new JournalAdapter(list, getContext()));
                        tvStatus.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                        if (list.isEmpty()) tvStatus.setText("Belum ada artikel.");
                    });
                } else {
                    showStatus("Gagal memuat Journal. Code: " + res.code());
                }
            } catch (Exception e) {
                showStatus("Error koneksi: pastikan Backend Python menyala.");
            }
        });
    }

    private void showStatus(String msg) {
        handler.post(() -> {
            if (!isAdded()) return;
            tvStatus.setVisibility(View.VISIBLE);
            tvStatus.setText(msg);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacksAndMessages(null);
    }
}