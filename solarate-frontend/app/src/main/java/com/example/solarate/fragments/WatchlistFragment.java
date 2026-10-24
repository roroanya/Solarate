package com.example.solarate.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.solarate.R;
import com.example.solarate.adapters.MovieAdapter;
import com.example.solarate.data.UserDataManager;
import com.example.solarate.models.Movie;
import java.util.ArrayList;
import java.util.List;

public class WatchlistFragment extends Fragment {

    private MovieAdapter adapter;
    private TextView tvCount, tvEmpty;

    public WatchlistFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_watchlist, container, false);

        tvCount = view.findViewById(R.id.tvWatchlistCount);
        tvEmpty = view.findViewById(R.id.tvWatchlistEmpty);

        RecyclerView rv = view.findViewById(R.id.rvWatchlist);
        rv.setLayoutManager(new GridLayoutManager(getContext(), 3));
        adapter = new MovieAdapter(new ArrayList<>(), requireContext());
        rv.setAdapter(adapter);

        return view;
    }

    // Dimuat ulang tiap kali tampil agar sinkron dengan tombol Watchlist di halaman detail
    @Override
    public void onResume() {
        super.onResume();
        List<Movie> movies = UserDataManager.getMovies(requireContext(), UserDataManager.WATCHLIST);
        adapter.setMovies(movies);
        tvCount.setText(movies.size() + (movies.size() == 1 ? " film" : " films"));
        tvEmpty.setVisibility(movies.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
