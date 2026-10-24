package com.example.solarate.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    // WAJIB 10.0.2.2 UNTUK EMULATOR (Jangan pakai localhost)
    public static final String BASE_URL = "http://10.0.2.2:8000/";

    public static Retrofit getClient() {
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
}