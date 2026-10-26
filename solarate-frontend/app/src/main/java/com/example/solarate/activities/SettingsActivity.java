package com.example.solarate.activities;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;
import com.example.solarate.R;
import com.example.solarate.data.AccountStore;
import com.example.solarate.data.UserDataManager;

import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS = "SolaratePrefs";

    private ImageView ivAvatar;
    private EditText etUsername, etPassword, etEmail;
    private Button btnSave;

    private Uri pendingAvatar;   // foto yang baru dipilih, belum disimpan

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    // Galeri bawaan Android (tidak butuh izin storage)
    private final ActivityResultLauncher<String> pickImage =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    pendingAvatar = uri;
                    showAvatar(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        ivAvatar = findViewById(R.id.ivSettingsAvatar);
        etUsername = findViewById(R.id.etSettingsUsername);
        etPassword = findViewById(R.id.etSettingsPassword);
        etEmail = findViewById(R.id.etSettingsEmail);
        btnSave = findViewById(R.id.btnSaveChanges);

        findViewById(R.id.btnSettingsBack).setOnClickListener(v -> finish());
        findViewById(R.id.llChangePhoto).setOnClickListener(v -> pickImage.launch("image/*"));
        btnSave.setOnClickListener(v -> save());

        loadCurrentData();
    }

    private void loadCurrentData() {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        etUsername.setText(prefs.getString("USERNAME", ""));
        etPassword.setText(prefs.getString("PASSWORD", ""));
        etEmail.setText(prefs.getString("EMAIL", ""));

        String path = prefs.getString("AVATAR_PATH", null);
        if (path != null) {
            File f = new File(path);
            if (f.exists()) {
                ivAvatar.setPadding(0, 0, 0, 0);
                Glide.with(this).load(f)
                        .signature(new ObjectKey(f.lastModified()))
                        .circleCrop().into(ivAvatar);
            }
        }
    }

    private void showAvatar(Uri uri) {
        ivAvatar.setPadding(0, 0, 0, 0);
        Glide.with(this).load(uri).circleCrop().into(ivAvatar);
    }

    private void save() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        // ---- Validasi ----
        if (username.length() < 3) {
            etUsername.setError("Username minimal 3 karakter");
            etUsername.requestFocus();
            return;
        }
        if (password.length() < 6) {
            etPassword.setError("Password minimal 6 karakter");
            etPassword.requestFocus();
            return;
        }
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Format email tidak valid");
            etEmail.requestFocus();
            return;
        }

        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        final String oldUsername = prefs.getString("USERNAME", "");

        // Username tidak boleh sama dengan akun LAIN (mengganti huruf besar/kecil akun sendiri boleh)
        if (!username.equalsIgnoreCase(oldUsername) && AccountStore.exists(this, username)) {
            etUsername.setError("Username sudah dipakai");
            etUsername.requestFocus();
            return;
        }

        btnSave.setEnabled(false);

        // Data favorites/watchlist/watched/reviews terikat ke username -> pindahkan ke nama baru
        UserDataManager.migrateUser(this, oldUsername, username);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("USERNAME", username);
        // Password baru langsung dipakai untuk login berikutnya (disalin ke daftar akun lewat syncFromSession)
        editor.putString("PASSWORD", password);
        editor.putString("EMAIL", email);

        if (pendingAvatar == null) {
            editor.apply();
            AccountStore.syncFromSession(this, oldUsername);   // salin perubahan ke daftar akun
            done();
            return;
        }

        // Foto: dikecilkan ke 512x512 lalu disimpan di penyimpanan internal app (background thread)
        final Uri uri = pendingAvatar;
        executor.execute(() -> {
            try {
                Bitmap bmp = Glide.with(getApplicationContext()).asBitmap().load(uri)
                        .centerCrop().submit(512, 512).get();
                // Nama file per username supaya foto tiap akun tidak saling menimpa
                File out = new File(getFilesDir(),
                        "avatar_" + username.toLowerCase().replaceAll("[^a-z0-9]", "_") + ".jpg");
                try (FileOutputStream fos = new FileOutputStream(out)) {
                    bmp.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                }
                editor.putString("AVATAR_PATH", out.getAbsolutePath());
                editor.apply();
                AccountStore.syncFromSession(this, oldUsername);
                handler.post(this::done);
            } catch (Exception e) {
                editor.apply();   // teks tetap tersimpan walau foto gagal
                AccountStore.syncFromSession(this, oldUsername);
                handler.post(() -> {
                    Toast.makeText(this, "Foto gagal disimpan, data lain tersimpan", Toast.LENGTH_LONG).show();
                    finish();
                });
            }
        });
    }

    private void done() {
        Toast.makeText(this, "Perubahan disimpan", Toast.LENGTH_SHORT).show();
        finish();
    }
}