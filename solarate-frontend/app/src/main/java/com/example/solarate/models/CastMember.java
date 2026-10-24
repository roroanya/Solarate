package com.example.solarate.models;

import com.google.gson.annotations.SerializedName;

/** Satu pemeran film (dari field "cast" di /api/movies/{id}). */
public class CastMember {
    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("character")
    private String character;

    @SerializedName("profile_url")
    private String profileUrl;

    public int getId() { return id; }
    public String getName() { return name; }
    public String getCharacter() { return character; }
    public String getProfileUrl() { return profileUrl; }
}