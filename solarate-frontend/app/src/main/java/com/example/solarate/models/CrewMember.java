package com.example.solarate.models;

import com.google.gson.annotations.SerializedName;

/** Satu kru film (dari field "crew" di /api/movies/{id}). role: Director, Writer, Producer, dst. */
public class CrewMember {
    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("role")
    private String role;

    @SerializedName("profile_url")
    private String profileUrl;

    public int getId() { return id; }
    public String getName() { return name; }
    public String getRole() { return role; }
    public String getProfileUrl() { return profileUrl; }
}