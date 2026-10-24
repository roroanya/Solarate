package com.example.solarate.models;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    // Cek di http://localhost:8000/docs bagian login.
    // Apakah backend mengembalikan "access_token", "token", atau "message"?
    // Di bawah ini asumsinya "access_token".
    @SerializedName("access_token")
    private String token;

    public String getToken() {
        return token;
    }
}