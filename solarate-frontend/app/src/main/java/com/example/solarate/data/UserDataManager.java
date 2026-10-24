package com.example.solarate.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.solarate.models.Movie;
import com.example.solarate.models.Review;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Penyimpanan data user (favorites, watchlist, watched, reviews) di SharedPreferences.
 * Satu-satunya "sumber data" yang dipakai bersama oleh halaman Detail, Films, Watchlist, dan Profile.
 * Data dipisah per username (diambil dari SolaratePrefs yang diisi saat Register).
 */
public class UserDataManager {

    public static final String FAVORITES = "favorites";
    public static final String WATCHLIST = "watchlist";
    public static final String WATCHED = "watched";

    private static final String PREFS_NAME = "SolarateUserData";
    private static final String KEY_REVIEWS = "reviews";

    private static final Gson gson = new Gson();
    private static final Type MOVIE_LIST_TYPE = new TypeToken<List<Movie>>() { }.getType();
    private static final Type REVIEW_LIST_TYPE = new TypeToken<List<Review>>() { }.getType();

    private UserDataManager() { }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    private static String key(Context context, String name) {
        String user = context.getApplicationContext()
                .getSharedPreferences("SolaratePrefs", Context.MODE_PRIVATE)
                .getString("USERNAME", "guest");
        return name + "_" + user;
    }

    // ==================== FILM LISTS (favorites / watchlist / watched) ====================

    /** Urutan: yang terakhir ditambahkan ada di paling depan. */
    public static synchronized List<Movie> getMovies(Context context, String listName) {
        String json = prefs(context).getString(key(context, listName), null);
        if (json == null) return new ArrayList<>();
        List<Movie> list = gson.fromJson(json, MOVIE_LIST_TYPE);
        return list != null ? list : new ArrayList<>();
    }

    private static void saveMovies(Context context, String listName, List<Movie> list) {
        prefs(context).edit().putString(key(context, listName), gson.toJson(list)).apply();
    }

    public static synchronized boolean contains(Context context, String listName, int movieId) {
        for (Movie m : getMovies(context, listName)) {
            if (m.getId() == movieId) return true;
        }
        return false;
    }

    public static synchronized Set<Integer> getIds(Context context, String listName) {
        Set<Integer> ids = new HashSet<>();
        for (Movie m : getMovies(context, listName)) ids.add(m.getId());
        return ids;
    }

    /** Tambah jika belum ada, hapus jika sudah ada. Return true bila sekarang ADA di list. */
    public static synchronized boolean toggle(Context context, String listName, Movie movie) {
        List<Movie> list = getMovies(context, listName);
        boolean removed = false;
        for (Iterator<Movie> it = list.iterator(); it.hasNext(); ) {
            if (it.next().getId() == movie.getId()) {
                it.remove();
                removed = true;
            }
        }
        if (!removed) list.add(0, movie);
        saveMovies(context, listName, list);
        return !removed;
    }

    // ==================== REVIEWS ====================

    public static synchronized List<Review> getReviews(Context context) {
        String json = prefs(context).getString(key(context, KEY_REVIEWS), null);
        if (json == null) return new ArrayList<>();
        List<Review> list = gson.fromJson(json, REVIEW_LIST_TYPE);
        return list != null ? list : new ArrayList<>();
    }

    public static synchronized Review getReview(Context context, int movieId) {
        for (Review r : getReviews(context)) {
            if (r.getMovieId() == movieId) return r;
        }
        return null;
    }

    /** Simpan review. Jika film ini sudah pernah di-review, review lama diganti. */
    public static synchronized void saveReview(Context context, Review review) {
        List<Review> list = getReviews(context);
        for (Iterator<Review> it = list.iterator(); it.hasNext(); ) {
            if (it.next().getMovieId() == review.getMovieId()) it.remove();
        }
        list.add(0, review);
        prefs(context).edit().putString(key(context, KEY_REVIEWS), gson.toJson(list)).apply();
    }

    public static synchronized void deleteReview(Context context, int movieId) {
        List<Review> list = getReviews(context);
        for (Iterator<Review> it = list.iterator(); it.hasNext(); ) {
            if (it.next().getMovieId() == movieId) it.remove();
        }
        prefs(context).edit().putString(key(context, KEY_REVIEWS), gson.toJson(list)).apply();
    }
}
