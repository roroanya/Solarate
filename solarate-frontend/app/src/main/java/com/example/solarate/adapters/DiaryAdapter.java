package com.example.solarate.adapters;

import android.content.Context;
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
import com.example.solarate.models.Review;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Adapter tab Diary
 */
public class DiaryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_MONTH = 0;
    private static final int TYPE_ENTRY = 1;

    private final Context context;
    private final List
            <Object> items = new ArrayList<>();

    public DiaryAdapter(Context context) {
        this.context = context;
    }

    /** Ambil review yang punya tanggal nonton, urutkan, lalu kelompokkan per bulan. */
    public void setReviews(List<Review> reviews) {
        List<Review> dated = new ArrayList<>();
        for (Review r : reviews) if (r.hasWatchedDate()) dated.add(r);

        // Tanggal "yyyy-MM-dd" bisa dibandingkan sebagai teks; terbaru di atas
        Collections.sort(dated, (a, b) -> {
            int c = b.getWatchedDate().compareTo(a.getWatchedDate());
            return c != 0 ? c : Long.compare(b.getTimestamp(), a.getTimestamp());
        });

        items.clear();
        String lastMonth = "";
        for (Review r : dated) {
            String month = Review.monthTitle(r.getWatchedDate());
            if (!month.equals(lastMonth)) {
                items.add(month);
                lastMonth = month;
            }
            items.add(r);
        }
        notifyDataSetChanged();
    }

    public boolean isEmpty() { return items.isEmpty(); }

    @Override
    public int getItemViewType(int position) {
        return items.get(position) instanceof String ? TYPE_MONTH : TYPE_ENTRY;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_MONTH) {
            return new MonthHolder(inf.inflate(R.layout.item_diary_month, parent, false));
        }
        return new EntryHolder(inf.inflate(R.layout.item_diary_entry, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object item = items.get(position);
        if (holder instanceof MonthHolder) {
            ((MonthHolder) holder).tv.setText((String) item);
            return;
        }
        Review r = (Review) item;
        EntryHolder h = (EntryHolder) holder;
        h.tvDay.setText(Review.dayOf(r.getWatchedDate()));
        h.tvTitle.setText(r.getTitle());
        h.tvYear.setText(r.getYear());
        h.tvStars.setText(r.getStars());
        Glide.with(context).load(r.getPosterUrl()).into(h.ivPoster);
        h.itemView.setOnClickListener(v ->
                context.startActivity(MovieDetailActivity.newIntent(context, r.toMovie())));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class MonthHolder extends RecyclerView.ViewHolder {
        final TextView tv;
        MonthHolder(View v) { super(v); tv = v.findViewById(R.id.tvDiaryMonth); }
    }

    static class EntryHolder extends RecyclerView.ViewHolder {
        final TextView tvDay, tvTitle, tvYear, tvStars;
        final ImageView ivPoster;
        EntryHolder(View v) {
            super(v);
            tvDay = v.findViewById(R.id.tvDiaryDay);
            tvTitle = v.findViewById(R.id.tvDiaryTitle);
            tvYear = v.findViewById(R.id.tvDiaryYear);
            tvStars = v.findViewById(R.id.tvDiaryStars);
            ivPoster = v.findViewById(R.id.ivDiaryPoster);
        }
    }
}