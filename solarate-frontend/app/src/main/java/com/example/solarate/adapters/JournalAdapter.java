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
import com.example.solarate.activities.JournalActivity;
import com.example.solarate.models.JournalSummary;

import java.util.List;

public class JournalAdapter extends RecyclerView.Adapter<JournalAdapter.VH> {

    private final List<JournalSummary> items;
    private final Context context;

    public JournalAdapter(List<JournalSummary> items, Context context) {
        this.items = items;
        this.context = context;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_journal, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        JournalSummary j = items.get(position);
        h.title.setText(j.getTitle());
        h.subtitle.setText(j.getSubtitle());
        Glide.with(context)
                .load(j.getFirstCover())
                .placeholder(R.color.lb_dark)
                .error(R.color.lb_dark)
                .into(h.cover);
        h.itemView.setOnClickListener(v ->
                context.startActivity(JournalActivity.newIntent(context, j.getSlug())));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView cover;
        TextView title, subtitle;

        VH(@NonNull View itemView) {
            super(itemView);
            cover = itemView.findViewById(R.id.ivJournalCover);
            title = itemView.findViewById(R.id.tvJournalTitle);
            subtitle = itemView.findViewById(R.id.tvJournalSubtitle);
        }
    }
}