package com.example.solarate.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.solarate.R;

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

        tvJoin.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        btnGo.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            // ... (BIARKAN KODE VALIDASI FORM YANG LAMA DI SINI) ...

            // --- CEK KE DATABASE LOKAL (SHAREDPREFERENCES) ---
            android.content.SharedPreferences prefs = getSharedPreferences("SolaratePrefs", MODE_PRIVATE);
            String savedUsername = prefs.getString("USERNAME", "");
            String savedPassword = prefs.getString("PASSWORD", "");

            // Jika belum ada yang daftar, atau username/pass tidak cocok
            if (savedUsername.isEmpty() || !username.equals(savedUsername) || !password.equals(savedPassword)) {
                Toast.makeText(LoginActivity.this, "Akun belum terdaftar atau password salah!", Toast.LENGTH_LONG).show();
                return; // Hentikan proses
            }

            // --- JIKA COCOK, LANJUT LOGIN ---
            Toast.makeText(LoginActivity.this, "Login Berhasil!", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("EXTRA_USERNAME", username);
            startActivity(intent);
            finish();
        });
    }
}