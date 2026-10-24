package com.example.solarate.models;

import com.google.gson.annotations.SerializedName;

public class Movie {
    @SerializedName("id")
    private int id;

    @SerializedName("title")
    private String title;

    @SerializedName("year")
    private int year;

    @SerializedName("release_date")
    private String releaseDate;

    @SerializedName("poster_url")
    private String posterUrl;

    @SerializedName("backdrop_url")
    private String backdropUrl;

    @SerializedName("rating")
    private double rating;

    @SerializedName("vote_count")
    private int voteCount;

    @SerializedName("overview")
    private String overview;

    public String getOverview() { return overview; }
    // --- GETTERS ---
    public int getId() { return id; }
    public String getTitle() { return title; }
    public int getYear() { return year; }
    public String getReleaseDate() { return releaseDate; }
    public String getPosterUrl() { return posterUrl; }
    public String getBackdropUrl() { return backdropUrl; }
    public double getRating() { return rating; }
    public int getVoteCount() { return voteCount; }
    // Tambahkan constructor ini untuk membuat data dummy
    public Movie(int id, String title, String posterUrl) {
        this.id = id;
        this.title = title;
        this.posterUrl = posterUrl;
    }
}