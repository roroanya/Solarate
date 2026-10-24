package com.example.solarate.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.solarate.R;
import com.example.solarate.activities.ListsActivity;
import com.example.solarate.models.ListSummary;

import java.util.List;

/** Adapter kartu list untuk tab Lists di Profile. Klik kartu -> ListsActivity (isi list). */
public class ListAdapter extends RecyclerView.Adapter<ListAdapter.VH> {

    private static final int MAX_PREVIEWS = 4;

    private final List<ListSummary> items;
    private final Context context;

    public ListAdapter(List<ListSummary> items, Context context) {
        this.items = items;
        this.context = context;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_list, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        ListSummary l = items.get(position);
        h.title.setText(l.getTitle());
        h.meta.setText("by " + l.getAuthor() + "  •  " + l.getFilmCount() + " films");
        h.description.setText(l.getDescription());

        // Poster contoh: dibuat dari kode karena jumlahnya bisa kurang dari 4
        h.previews.removeAllViews();
        List<String> previews = l.getPreviews();
        if (previews != null) {
            float d = context.getResources().getDisplayMetrics().density;
            for (int i = 0; i < Math.min(MAX_PREVIEWS, previews.size()); i++) {
                ImageView iv = new ImageView(context);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams((int) (64 * d), (int) (96 * d));
                lp.setMarginEnd((int) (8 * d));
                iv.setLayoutParams(lp);
                iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                Glide.with(context).load(previews.get(i))
                        .placeholder(R.color.lb_dark).error(R.color.lb_dark).into(iv);
                h.previews.addView(iv);
            }
        }

        h.itemView.setOnClickListener(v ->
                context.startActivity(ListsActivity.newIntent(context, l.getSlug(), l.getTitle())));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        LinearLayout previews;
        TextView title, meta, description;

        VH(@NonNull View itemView) {
            super(itemView);
            previews = itemView.findViewById(R.id.listPreviews);
            title = itemView.findViewById(R.id.ListTitle);
            meta = itemView.findViewById(R.id.ListMeta);
            description = itemView.findViewById(R.id.ListDescription);
        }
        }
    }