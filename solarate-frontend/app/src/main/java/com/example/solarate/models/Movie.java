package com.example.solarate.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

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

    // --- FIELD UNTUK HALAMAN DETAIL ---
    @SerializedName("runtime")
    private int runtime;

    @SerializedName("director")
    private String director;

    @SerializedName("producer")
    private String producer;

    @SerializedName("cast")
    private List<CastMember> cast;

    @SerializedName("crew")
    private List<CrewMember> crew;

    // Constructor kosong
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
    public List<CastMember> getCast() { return cast; }
    public List<CrewMember> getCrew() { return crew; }
    public Movie slimCopy() {
        Movie c = new Movie();
        c.id = id;
        c.title = title;
        c.year = year;
        c.releaseDate = releaseDate;
        c.posterUrl = posterUrl;
        c.backdropUrl = backdropUrl;
        c.rating = rating;
        c.voteCount = voteCount;
        c.overview = overview;
        c.runtime = runtime;
        c.director = director;
        c.producer = producer;
        return c;
    }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }

    /** Tahun rilis sebagai teks. Jika field year kosong, diambil dari release_date. */
    public String getYearText() {
        if (year > 0) return String.valueOf(year);
        if (releaseDate != null && releaseDate.length() >= 4) return releaseDate.substring(0, 4);
        return "";
    }
}
