package com.example.solarate.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class JournalDetail extends JournalSummary {
    @SerializedName("paragraphs")
    private List<String> paragraphs;

    @SerializedName("films")
    private List<Movie> films;

    public List<String> getParagraphs() { return paragraphs; }
    public List<Movie> getFilms() { return films; }
}