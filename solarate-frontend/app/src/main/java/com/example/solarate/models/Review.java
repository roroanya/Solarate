package com.example.solarate.models;

import com.google.gson.annotations.SerializedName;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Review {
    @SerializedName("movieId")
    private int movieId;
    @SerializedName("title")
    private String title;
    @SerializedName("year")
    private String year;
    @SerializedName("posterUrl")
    private String posterUrl;
    @SerializedName("rating")
    private float rating;       // 0 - 5 (kelipatan 0.5)
    @SerializedName("text")
    private String text;
    @SerializedName("timestamp")
    private long timestamp;

    public Review() { }

    public Review(Movie movie, float rating, String text) {
        this.movieId = movie.getId();
        this.title = movie.getTitle();
        this.year = movie.getYearText();
        this.posterUrl = movie.getPosterUrl();
        this.rating = rating;
        this.text = text;
        this.timestamp = System.currentTimeMillis();
    }

    public int getMovieId() { return movieId; }
    public String getTitle() { return title; }
    public String getYear() { return year; }
    public String getPosterUrl() { return posterUrl; }
    public float getRating() { return rating; }
    public String getText() { return text == null ? "" : text; }
    public long getTimestamp() { return timestamp; }

    /** Rating dalam bentuk bintang, contoh 3.5 -> "★★★½" */
    public String getStars() {
        if (rating <= 0) return "";
        int full = (int) rating;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < full; i++) sb.append("★");
        if (rating - full >= 0.5f) sb.append("½");
        return sb.toString();
    }

    public String getDateText() {
        return new SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(new Date(timestamp));
    }

    /** Film minimal untuk dibuka kembali ke halaman detail. */
    public Movie toMovie() {
        return new Movie(movieId, title, posterUrl);
    }
}
