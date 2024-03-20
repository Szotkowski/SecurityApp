package com.example.security;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Button;
import android.view.View;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Objects;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EncryptionActivity extends AppCompatActivity {

    private EditText passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encryption_page);

        passwordEditText = (EditText) findViewById(R.id.password_edit_text);
        Button decryptButton = (Button) findViewById(R.id.decrypt_button);

        decryptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                decryptFolder();
            }
        });
    }

    private void decryptFolder() {
        try {
            String password = passwordEditText.getText().toString();
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            SecretKeySpec secretKey = generateKey(password);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(new byte[16]);

            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);

            // Path to the folder to be decrypted
            File folderToDecrypt = new File(getFilesDir(), "encrypted_folder");
            if (!folderToDecrypt.exists()) {
                if (!folderToDecrypt.mkdirs()) {
                    Toast.makeText(getApplicationContext(), "Failed to create folder!", Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(getApplicationContext(), "Folder created!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Decrypt all files in the folder
            for (File file : Objects.requireNonNull(folderToDecrypt.listFiles())) {
                FileInputStream fis = new FileInputStream(file);
                byte[] inputBytes = new byte[(int) file.length()];
                fis.read(inputBytes);

                byte[] decryptedBytes = cipher.doFinal(inputBytes);

                // Write decrypted bytes back to the file
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(decryptedBytes);

                fis.close();
                fos.close();
            }
            Toast.makeText(getApplicationContext(), "Folder decrypted successfully!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("DecryptionError", "Error decrypting folder: " + e.getMessage());
            Toast.makeText(getApplicationContext(), "Error decrypting folder!", Toast.LENGTH_SHORT).show();
        }
    }

    private SecretKeySpec generateKey(String password) throws Exception {
        final MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = password.getBytes(StandardCharsets.UTF_8);
        digest.update(bytes, 0, bytes.length);
        byte[] key = digest.digest();
        return new SecretKeySpec(key, "AES");
    }
}