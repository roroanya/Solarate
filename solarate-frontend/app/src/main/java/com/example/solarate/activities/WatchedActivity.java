package com.example.solarate.activities;

import com.example.solarate.data.UserDataManager;

/** Daftar film yang sudah ditonton. Terisi dari tombol "Watch" di halaman detail. */
public class WatchedActivity extends MovieGridActivity {
    @Override protected String getListName() { return UserDataManager.WATCHED; }
    @Override protected String getScreenTitle() { return "Watched"; }
    @Override protected String getEmptyMessage() {
        return "Belum ada film yang ditonton.\nTekan tombol Watch di halaman detail film.";
    }
}
