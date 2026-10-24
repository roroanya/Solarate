package com.example.solarate.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.solarate.R;
import com.example.solarate.activities.MovieDetailActivity;
import com.example.solarate.models.Movie;
import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    private List<Movie> movieList;
    private Context context;

    // Constructor untuk menerima data dari Fragment
    public MovieAdapter(List<Movie> movieList, Context context) {
        this.movieList = movieList;
        this.context = context;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Menghubungkan ke desain item_movie.xml
        View view = LayoutInflater.from(context).inflate(R.layout.item_movie, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movie movie = movieList.get(position);

        // Load Gambar Poster
        String imageUrl = movie.getPosterUrl();

        Glide.with(context)
                .load(imageUrl)
                .placeholder(R.color.lb_dark) // Warna abu-abu saat gambar sedang loading
                .error(R.color.lb_black)      // Warna hitam jika gambar gagal dimuat
                .into(holder.ivPoster);

        // INTENT & BUNDLE: Jika poster diklik, pindah ke MovieDetailActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MovieDetailActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("MOVIE_ID", String.valueOf(movie.getId()));
            bundle.putString("MOVIE_TITLE", movie.getTitle());
            intent.putExtras(bundle);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        // Menghitung total film yang akan ditampilkan
        return movieList.size();
    }

    // Class ViewHolder untuk mendeklarasikan komponen UI
    public static class MovieViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPoster;
        TextView tvTitle;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPoster = itemView.findViewById(R.id.ivPoster);
        }
    }
}