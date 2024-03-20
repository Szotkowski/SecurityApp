package com.example.security;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.app.ActivityCompat;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class BiometricLoginActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PERMISSION = 100;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_biometric_login);

        // Check if the necessary permissions are granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_BIOMETRIC) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.USE_BIOMETRIC}, REQUEST_CODE_PERMISSION);
        } else {
            setupBiometricPrompt();
        }
    }

    private void setupBiometricPrompt() {
        Executor executor = Executors.newSingleThreadExecutor();

        biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                runOnUiThread(() -> {
            // Handle authentication error
                    Toast.makeText(BiometricLoginActivity.this, "Authentication error: " + errString, Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                // Handle authentication success
                // Handle authentication success
                runOnUiThread(() -> {
                    Toast.makeText(BiometricLoginActivity.this, "Authentication succeeded!", Toast.LENGTH_SHORT).show();
                    // Navigate to another activity
                    Intent intent = new Intent(BiometricLoginActivity.this, EncryptionActivity.class);
                    startActivity(intent);
                    // Finish the current activity (optional, depending on your navigation flow)
                    finish();
                });
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                // Handle authentication failure
                runOnUiThread(() -> {
                    Toast.makeText(BiometricLoginActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                });
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric login")
                .setSubtitle("Authenticate using your biometric data")
                .setNegativeButtonText("Cancel")
                .build();

        findViewById(R.id.buttonBiometricLogin).setOnClickListener(view -> biometricPrompt.authenticate(promptInfo));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupBiometricPrompt();
            } else {
                Toast.makeText(this, "Permission denied. Biometric authentication will not be available.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
