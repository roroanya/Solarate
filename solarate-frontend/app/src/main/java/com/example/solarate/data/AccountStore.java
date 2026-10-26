package com.example.solarate.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.util.Locale;

/**
 * Penyimpanan SEMUA akun yang pernah didaftarkan di perangkat ini (multi-akun).
 *
 * Ada dua tempat penyimpanan, jangan tertukar:
 *  - "SolarateAccounts" (file ini)  -> daftar semua akun: satu entri per username.
 *  - "SolaratePrefs"                -> akun yang SEDANG LOGIN (session). Dibaca oleh Profile,
 *                                      Settings, dan UserDataManager seperti sebelumnya.
 *
 * Alurnya:
 *  Register -> akun ditambah ke daftar (akun lama tidak tersentuh)
 *  Login    -> akun dicari di daftar, lalu disalin ke session (startSession)
 *  Settings -> perubahan di session disalin balik ke daftar (syncFromSession)
 */
public class AccountStore {

    private static final String ACCOUNTS_PREFS = "SolarateAccounts";
    private static final String SESSION_PREFS = "SolaratePrefs";

    private static final Gson gson = new Gson();

    /** Satu akun. */
    public static class Account {
        public String username;
        public String email;
        public String password;
        public String bio;
        public String avatarPath;
    }

    private AccountStore() { }

    private static SharedPreferences accounts(Context c) {
        return c.getApplicationContext().getSharedPreferences(ACCOUNTS_PREFS, Context.MODE_PRIVATE);
    }

    private static SharedPreferences session(Context c) {
        return c.getApplicationContext().getSharedPreferences(SESSION_PREFS, Context.MODE_PRIVATE);
    }

    /** Kunci penyimpanan: username huruf kecil, supaya "Solar" dan "solar" dianggap sama. */
    private static String key(String username) {
        return username.trim().toLowerCase(Locale.ROOT);
    }

    /** True kalau username sudah dipakai akun lain. */
    public static synchronized boolean exists(Context c, String username) {
        return accounts(c).contains(key(username));
    }

    /** Cari akun berdasarkan username (tidak peka huruf besar/kecil). Null kalau tidak ada. */
    public static synchronized Account find(Context c, String username) {
        String json = accounts(c).getString(key(username), null);
        return json == null ? null : gson.fromJson(json, Account.class);
    }

    private static void save(Context c, Account a) {
        accounts(c).edit().putString(key(a.username), gson.toJson(a)).apply();
    }

    /**
     * Mendaftarkan akun baru. Akun lain TIDAK diubah atau dihapus.
     * @return akun baru, atau null kalau username sudah dipakai
     */
    public static synchronized Account register(Context c, String username, String email, String password) {
        if (exists(c, username)) return null;
        Account a = new Account();
        a.username = username.trim();
        a.email = email;
        a.password = password;
        a.bio = "";
        a.avatarPath = null;
        save(c, a);
        return a;
    }

    /** Menjadikan akun ini sebagai akun yang sedang login (menulis ke SolaratePrefs). */
    public static synchronized void startSession(Context c, Account a) {
        SharedPreferences.Editor e = session(c).edit();
        e.clear();
        e.putString("USERNAME", a.username);
        e.putString("EMAIL", a.email == null ? "" : a.email);
        e.putString("PASSWORD", a.password == null ? "" : a.password);
        e.putString("BIO", a.bio == null ? "" : a.bio);
        if (a.avatarPath != null) e.putString("AVATAR_PATH", a.avatarPath);
        e.apply();
    }

    /**
     * Dipanggil setelah Settings menyimpan perubahan: menyalin data session kembali ke daftar akun,
     * supaya bio / foto / email / username baru tidak hilang saat logout lalu login lagi.
     * @param oldUsername username sebelum diubah (kalau berubah, entri lama dihapus)
     */
    public static synchronized void syncFromSession(Context c, String oldUsername) {
        SharedPreferences s = session(c);
        String username = s.getString("USERNAME", "");
        if (username.isEmpty()) return;

        Account a = oldUsername == null ? null : find(c, oldUsername);
        if (a == null) a = new Account();
        a.username = username;
        a.email = s.getString("EMAIL", "");
        a.password = s.getString("PASSWORD", "");
        a.bio = s.getString("BIO", "");
        a.avatarPath = s.getString("AVATAR_PATH", null);

        if (oldUsername != null && !key(oldUsername).equals(key(username))) {
            accounts(c).edit().remove(key(oldUsername)).apply();
        }
        save(c, a);
    }

    /**
     * Untuk akun yang dibuat sebelum fitur multi-akun ada: kalau ada akun di session tapi belum
     * ada di daftar, masukkan ke daftar supaya tetap bisa login.
     */
    public static synchronized void importLegacy(Context c) {
        SharedPreferences s = session(c);
        String username = s.getString("USERNAME", "");
        String password = s.getString("PASSWORD", "");
        if (username.isEmpty() || password.isEmpty() || exists(c, username)) return;

        Account a = new Account();
        a.username = username;
        a.email = s.getString("EMAIL", "");
        a.password = password;
        a.bio = s.getString("BIO", "");
        a.avatarPath = s.getString("AVATAR_PATH", null);
        save(c, a);
    }
}