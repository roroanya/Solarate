package com.example.solarate.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/** Ringkasan satu list (dari /api/lists), dipakai untuk kartu di tab Lists. */
public class ListSummary {
    @SerializedName("slug")
    private String slug;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("author")
    private String author;

    @SerializedName("official")
    private boolean official;

    @SerializedName("film_count")
    private int filmCount;

    @SerializedName("previews")
    private List<String> previews;   // URL poster contoh

    public String getSlug() { return slug; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getAuthor() { return author; }
    public boolean isOfficial() { return official; }
    public int getFilmCount() { return filmCount; }
    public List<String> getPreviews() { return previews; }
}