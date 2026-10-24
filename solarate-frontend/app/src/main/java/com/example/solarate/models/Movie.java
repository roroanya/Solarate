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

    // --- FIELD BARU UNTUK HALAMAN DETAIL (opsional, diisi oleh backend) ---
    @SerializedName("runtime")
    private int runtime;      // durasi dalam menit

    @SerializedName("director")
    private String director;  // nama sutradara

    @SerializedName("producer")
    private String producer;  // nama produser

    // Constructor kosong (dibutuhkan Gson saat membaca data yang disimpan)
    public Movie() { }

    // Constructor untuk data dummy / data ringan dari Bundle
    public Movie(int id, String title, String posterUrl) {
        this.id = id;
        this.title = title;
        this.posterUrl = posterUrl;
    }

    // --- GETTERS ---
    public int getId() { return id; }
    public String getTitle() { return title; }
    public int getYear() { return year; }
    public String getReleaseDate() { return releaseDate; }
    public String getPosterUrl() { return posterUrl; }
    public String getBackdropUrl() { return backdropUrl; }
    public double getRating() { return rating; }
    public int getVoteCount() { return voteCount; }
    public String getOverview() { return overview; }
    public int getRuntime() { return runtime; }
    public String getDirector() { return director; }
    public String getProducer() { return producer; }

    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }

    /** Tahun rilis sebagai teks. Jika field year kosong, diambil dari release_date. */
    public String getYearText() {
        if (year > 0) return String.valueOf(year);
        if (releaseDate != null && releaseDate.length() >= 4) return releaseDate.substring(0, 4);
        return "";
    }
}
