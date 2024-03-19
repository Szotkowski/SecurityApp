package com.example.security;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import javax.crypto.Cipher;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

public class EncryptionActivity extends AppCompatActivity {

    private static final String PASSWORD_KEY = "password";
    private static final String ENCRYPTED_FILE_PATH = "encryptedVolume.dat";

    private boolean isPasswordSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encryption_page);

        isPasswordSet = isPasswordSet();

        Button decryptButton = findViewById(R.id.decryptButton);
        decryptButton.setOnClickListener(v -> {
            if (isPasswordSet) {
                mountEncryptedVolume();
            } else {
                setupPassword();
            }
        });
    }

    private boolean isPasswordSet() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        return sharedPreferences.contains(PASSWORD_KEY);
    }

    private void setupPassword() {
        final EditText passwordEditText = new EditText(this);
        passwordEditText.setHint("Enter Password");

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Set Password")
                .setView(passwordEditText)
                .setPositiveButton("OK", (dialog, which) -> {
                    String password = passwordEditText.getText().toString();
                    savePassword(password);
                    createEncryptedVolume();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void savePassword(String password) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(PASSWORD_KEY, password).apply();
        isPasswordSet = true;
    }

    private String getPassword() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        return sharedPreferences.getString(PASSWORD_KEY, null);
    }

    private void createEncryptedVolume() {
        int volumeSizeMB = 25;

        try {
            // Hash the password using SHA-256

            File appStorageDir = getFilesDir();
            String encryptedFilePath = new File(appStorageDir, ENCRYPTED_FILE_PATH).getAbsolutePath();

            File encryptedFile = new File(encryptedFilePath);
            AESCrypt.encryptFile(getPassword(), encryptedFile, volumeSizeMB * 1024 * 1024);
            Toast.makeText(this, "Encrypted volume created successfully", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    private void mountEncryptedVolume() {
        try {
            File encryptedFile = new File(getFilesDir(), ENCRYPTED_FILE_PATH);
            AESCrypt.decryptFile(getPassword(), encryptedFile);
            Toast.makeText(this, "Encrypted volume mounted successfully", Toast.LENGTH_SHORT).show();
            // Now you can work with the decrypted file
            // Example: FileInputStream fis = new FileInputStream(decryptedFile);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}