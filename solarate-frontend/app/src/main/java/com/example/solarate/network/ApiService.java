package com.example.solarate.network;

// Import semua model yang dibutuhkan
import com.example.solarate.models.LoginRequest;
import com.example.solarate.models.LoginResponse;
import com.example.solarate.models.Movie;
import com.example.solarate.models.MovieResponse;
import com.example.solarate.models.RegisterRequest;
import com.example.solarate.models.JournalDetail;
import com.example.solarate.models.JournalSummary;
import com.example.solarate.models.ListDetail;
import com.example.solarate.models.ListSummary;
import java.util.List;

// Import library Retrofit
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // 1. ENDPOINT UNTUK MENAMPILKAN DAFTAR FILM PAGE HOME
    @GET("api/movies/popular")
    Call<MovieResponse> getPopularMovies();
    @GET("api/movies/trending")
    Call<MovieResponse> getTrendingMovies(@Query("window") String window);

    // 2. ENDPOINT UNTUK LOGIN
    @POST("api/login")
    Call<LoginResponse> loginUser(@Body LoginRequest loginRequest);

    // 3. ENDPOINT UNTUK REGISTER (DAFTAR AKUN)
    @POST("api/register")
    Call<ResponseBody> registerUser(@Body RegisterRequest registerRequest);

    // 4. ENDPOINT UNTUK DETAIL FILM
    @GET("api/movies/{id}")
    Call<Movie> getMovieDetail(@Path("id") int movieId);

    // 5. ENDPOINT UNTUK JOURNAL
    @GET("api/journal")
    Call<List<JournalSummary>> getJournal();
    @GET("api/journal/{slug}")
    Call<JournalDetail> getJournalDetail(@Path("slug") String slug);

    // 6. ENDPOINT LISTS (tab Lists di Profile)
    @GET("api/lists")
    Call<List<ListSummary>> getLists();
    @GET("api/lists/{slug}")
    Call<ListDetail> getListDetail(@Path("slug") String slug);

}