package com.example.solarate.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class JournalSummary {
    @SerializedName("slug")
    protected String slug;

    @SerializedName("title")
    protected String title;

    @SerializedName("subtitle")
    protected String subtitle;

    @SerializedName("cover_urls")
    protected List<String> coverUrls;

    public String getSlug() { return slug; }
    public String getTitle() { return title; }
    public String getSubtitle() { return subtitle; }
    public List<String> getCoverUrls() { return coverUrls; }

    public String getFirstCover() {
        return (coverUrls != null && !coverUrls.isEmpty()) ? coverUrls.get(0) : null;
    }
}