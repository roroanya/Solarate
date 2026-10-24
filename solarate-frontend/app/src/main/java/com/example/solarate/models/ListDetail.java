package com.example.solarate.models;

import com.google.gson.annotations.SerializedName;

/** Isi satu list (dari /api/lists/{slug}). "films" berbentuk sama dengan MovieResponse (field results). */
public class ListDetail {
    @SerializedName("slug")
    private String slug;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("author")
    private String author;

    @SerializedName("film_count")
    private int filmCount;

    @SerializedName("films")
    private MovieResponse films;

    public String getSlug() { return slug; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getAuthor() { return author; }
    public int getFilmCount() { return filmCount; }
    public MovieResponse getFilms() { return films; }
}