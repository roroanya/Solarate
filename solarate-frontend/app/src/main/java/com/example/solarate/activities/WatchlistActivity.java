package com.example.solarate.activities;

import com.example.solarate.data.UserDataManager;

/** Daftar film yang ingin ditonton. Terisi dari tombol "Watchlist" di halaman detail. */
public class WatchlistActivity extends MovieGridActivity {
    @Override protected String getListName() { return UserDataManager.WATCHLIST; }
    @Override protected String getScreenTitle() { return "Your Watchlist"; }
    @Override protected String getEmptyMessage() {
        return "Belum ada film yang ingin ditonton.\nTekan tombol Watchlist di halaman detail film.";
    }
}
