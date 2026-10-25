package com.example.solarate.models;

import com.google.gson.annotations.SerializedName;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

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

    // Tanggal user menonton film, format "yyyy-MM-dd" (dipilih lewat kalender di pop up review).
    //   null -> review lama (dibuat sebelum fitur ini ada), dipakai tanggal pembuatan review
    //   ""   -> user sengaja menghapus tanggal, jadi tidak muncul di Diary
    @SerializedName("watchedDate")
    private String watchedDate;

    public Review() { }

    public Review(Movie movie, float rating, String text, String watchedDate) {
        this.movieId = movie.getId();
        this.title = movie.getTitle();
        this.year = movie.getYearText();
        this.posterUrl = movie.getPosterUrl();
        this.rating = rating;
        this.text = text;
        this.timestamp = System.currentTimeMillis();
        this.watchedDate = watchedDate;
    }

    public int getMovieId() { return movieId; }
    public String getTitle() { return title; }
    public String getYear() { return year; }
    public String getPosterUrl() { return posterUrl; }
    public float getRating() { return rating; }
    public String getText() { return text == null ? "" : text; }
    public long getTimestamp() { return timestamp; }

    /** Tanggal nonton "yyyy-MM-dd", atau "" kalau user menghapus tanggalnya. */
    public String getWatchedDate() {
        if (watchedDate == null) {
            // Review lama: pakai tanggal review dibuat
            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            return f.format(new Date(timestamp));
        }
        return watchedDate;
    }

    /** True kalau review ini punya tanggal nonton -> muncul di tab Diary. */
    public boolean hasWatchedDate() {
        return !getWatchedDate().isEmpty();
    }

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

    // ==========================================================
    // HELPER TANGGAL (disimpan sebagai teks "yyyy-MM-dd" supaya tidak bergeser
    // karena zona waktu). Kalender Material mengembalikan milidetik UTC, jadi
    // konversinya memakai zona UTC.
    // ==========================================================

    private static SimpleDateFormat isoUtc() {
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        f.setTimeZone(TimeZone.getTimeZone("UTC"));
        return f;
    }

    public static String today() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
    }

    public static long isoToUtcMillis(String iso) {
        try {
            Date d = isoUtc().parse(iso);
            return d != null ? d.getTime() : System.currentTimeMillis();
        } catch (ParseException e) {
            return System.currentTimeMillis();
        }
    }

    public static String utcMillisToIso(long utcMillis) {
        return isoUtc().format(new Date(utcMillis));
    }

    public static String toDisplay(String iso) {
        return formatIso(iso, "EEEE, MMMM d, yyyy");
    }

    public static String monthTitle(String iso) {
        return formatIso(iso, "MMMM yyyy").toUpperCase(Locale.ENGLISH);
    }

    public static String dayOf(String iso) {
        return formatIso(iso, "d");
    }

    private static String formatIso(String iso, String pattern) {
        try {
            Date d = isoUtc().parse(iso);
            SimpleDateFormat out = new SimpleDateFormat(pattern, Locale.ENGLISH);
            out.setTimeZone(TimeZone.getTimeZone("UTC"));
            return d != null ? out.format(d) : "";
        } catch (ParseException e) {
            return "";
        }
    }
}