package com.example.solarate.network;

// Import semua model yang dibutuhkan
import com.example.solarate.models.LoginRequest;
import com.example.solarate.models.LoginResponse;
import com.example.solarate.models.Movie;
import com.example.solarate.models.MovieResponse;
import com.example.solarate.models.RegisterRequest;

// Import library Retrofit
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {

    // ==========================================
    // 1. ENDPOINT UNTUK MENAMPILKAN DAFTAR FILM (HOME)
    // ==========================================
    @GET("api/movies/popular")
    Call<MovieResponse> getPopularMovies();

    // ==========================================
    // 2. ENDPOINT UNTUK LOGIN
    // ==========================================
    // (Cek /docs apakah URL-nya benar /api/login)
    @POST("api/login")
    Call<LoginResponse> loginUser(@Body LoginRequest loginRequest);

    // ==========================================
    // 3. ENDPOINT UNTUK REGISTER (DAFTAR AKUN)
    // ==========================================
    // (Cek /docs apakah URL-nya benar /api/register)
    @POST("api/register")
    Call<ResponseBody> registerUser(@Body RegisterRequest registerRequest);

    // ==========================================
    // 4. ENDPOINT UNTUK DETAIL FILM
    // ==========================================
    // {id} akan diganti otomatis dengan ID film yang diklik
    @GET("api/movies/{id}")
    Call<Movie> getMovieDetail(@Path("id") int movieId);

}