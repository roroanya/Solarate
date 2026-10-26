package com.example.solarate.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.solarate.R;
import com.example.solarate.data.AccountStore;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnGo;
    private TextView tvJoin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnGo = findViewById(R.id.btnGo);
        tvJoin = findViewById(R.id.tvJoin);

        // Akun dari versi sebelum multi-akun dimasukkan ke daftar akun (sekali saja, aman diulang)
        AccountStore.importLegacy(this);

        tvJoin.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        btnGo.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Isi username dan password", Toast.LENGTH_SHORT).show();
                return;
            }

            // --- CARI AKUN DI DAFTAR SEMUA AKUN ---
            // Username tidak peka huruf besar/kecil, password peka.
            AccountStore.Account account = AccountStore.find(this, username);
            if (account == null || !password.equals(account.password)) {
                Toast.makeText(LoginActivity.this, "Akun belum terdaftar atau password salah!", Toast.LENGTH_LONG).show();
                return; // Hentikan proses
            }

            // --- JIKA COCOK: jadikan akun ini yang sedang login ---
            // Profile, Settings, dan data film (favorit, watchlist, review) membaca akun ini.
            AccountStore.startSession(this, account);

            Toast.makeText(LoginActivity.this, "Login Berhasil!", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("EXTRA_USERNAME", account.username);
            startActivity(intent);
            finish();
        });
    }
}