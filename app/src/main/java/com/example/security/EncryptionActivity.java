package com.example.security;

import android.os.Bundle;
import android.widget.Button;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import androidx.appcompat.app.AppCompatActivity;

public class EncryptionActivity extends AppCompatActivity {

    private static final int KEY_LENGTH = 256;
    private static final int BUFFER_SIZE = 8192;
    private static final String ENCRYPTION_ALGORITHM = "AES";
    private static final String ENCRYPTION_MODE = "AES/CBC/PKCS5Padding";
    private static final int ITERATION_COUNT = 65536;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encryption_page);

        Button decryptButton = findViewById(R.id.decrypt_button);
        Button encryptButton = findViewById(R.id.encrypt_button);
        Button createButton = findViewById(R.id.create_button);
        Button deleteButton = findViewById(R.id.delete_button);

        decryptButton.setOnClickListener(v -> {
            try {
                decryptFolderRecursive(new File(getFilesDir(), ".abcdefghijklmnopqrstuvwxyz"), "ahoj");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        encryptButton.setOnClickListener(v -> {
            try {
                encryptFolderRecursive(new File(getFilesDir(), ".abcdefghijklmnopqrstuvwxyz"), "ahoj");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        createButton.setOnClickListener(v -> createFolder());
        deleteButton.setOnClickListener(v -> deleteFolderRecursive(new File(getFilesDir(), ".abcdefghijklmnopqrstuvwxyz")));
    }

    private void createFolder() {
        try {
            File folder = new File(getFilesDir(), ".abcdefghijklmnopqrstuvwxyz");

            if (!folder.exists()) {
                boolean success = folder.mkdirs();
                if (success) {
                    String folderPathHash = hashString(folder.getAbsolutePath());
                    File hashFile = new File(folder, "folder_hash.txt");
                    FileOutputStream fos = new FileOutputStream(hashFile);
                    assert folderPathHash != null;
                    fos.write(folderPathHash.getBytes());
                    fos.close();
                } else {
                    System.out.println("Failed to create folder.");
                }
            } else {
                System.out.println("Folder already exists.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteFolderRecursive(File folder) {
        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteFolderRecursive(file);
                }
            }
        }
        folder.delete();
    }

    public static void encryptFolderRecursive(File folder, String password) throws IOException {
        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    encryptFolderRecursive(file, password);
                }
            }
        } else {
            encryptFile(folder, password);
        }
    }

    public static void decryptFolderRecursive(File folder, String password) throws IOException {
        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    decryptFolderRecursive(file, password);
                }
            }
        } else {
            decryptFile(folder, password);
        }
    }

    private static void encryptFile(File file, String password) throws IOException {
        try {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH);
            SecretKey secretKey = factory.generateSecret(spec);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getEncoded(), ENCRYPTION_ALGORITHM);
            Cipher cipher = Cipher.getInstance(ENCRYPTION_MODE);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);

            File encryptedFile = new File(file.getParent(), file.getName() + ".encrypted");
            try (FileInputStream fis = new FileInputStream(file);
                 FileOutputStream fos = new FileOutputStream(encryptedFile);
                 CipherOutputStream cos = new CipherOutputStream(fos, cipher)) {
                fos.write(salt); // Write the salt to the beginning of the encrypted file
                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    cos.write(buffer, 0, bytesRead);
                }
            }
            Files.deleteIfExists(file.toPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void decryptFile(File file, String password) throws IOException {
        try {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH);
            SecretKey secretKey = factory.generateSecret(spec);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getEncoded(), ENCRYPTION_ALGORITHM);
            Cipher cipher = Cipher.getInstance(ENCRYPTION_MODE);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);

            File decryptedFile = new File(file.getParent(), file.getName().replace(".encrypted", ""));
            try (FileOutputStream fos = new FileOutputStream(decryptedFile);
                 CipherInputStream cis = new CipherInputStream(Files.newInputStream(file.toPath()), cipher)) {
                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead;
                while ((bytesRead = cis.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }
            Files.deleteIfExists(file.toPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String hashString(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte hashByte : hashBytes) {
                String hex = Integer.toHexString(0xff & hashByte);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}