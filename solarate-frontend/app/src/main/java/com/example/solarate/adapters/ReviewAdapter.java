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
import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private List<Review> reviews = new ArrayList<>();
    private final Context context;

    public ReviewAdapter(Context context) {
        this.context = context;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews != null ? reviews : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder h, int position) {
        Review r = reviews.get(position);

        String year = r.getYear();
        h.tvTitle.setText(r.getTitle() + (year == null || year.isEmpty() ? "" : " (" + year + ")"));

        String stars = r.getStars();
        h.tvStars.setText(stars);
        h.tvStars.setVisibility(stars.isEmpty() ? View.GONE : View.VISIBLE);

        h.tvText.setText(r.getText());
        h.tvText.setVisibility(r.getText().isEmpty() ? View.GONE : View.VISIBLE);

        h.tvDate.setText(r.getDateText());

        Glide.with(context)
                .load(r.getPosterUrl())
                .placeholder(R.color.lb_dark)
                .error(R.color.lb_black)
                .into(h.ivPoster);

        h.itemView.setOnClickListener(v ->
                context.startActivity(MovieDetailActivity.newIntent(context, r.toMovie())));
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPoster;
        TextView tvTitle, tvStars, tvText, tvDate;

        ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPoster = itemView.findViewById(R.id.ivReviewPoster);
            tvTitle = itemView.findViewById(R.id.tvReviewTitle);
            tvStars = itemView.findViewById(R.id.tvReviewStars);
            tvText = itemView.findViewById(R.id.tvReviewText);
            tvDate = itemView.findViewById(R.id.tvReviewDate);
        }
    }
}
