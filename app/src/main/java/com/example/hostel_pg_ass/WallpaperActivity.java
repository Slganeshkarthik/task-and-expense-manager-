package com.example.hostel_pg_ass;

import android.app.WallpaperManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class WallpaperActivity extends AppCompatActivity {

    private ImageView ivWallpaper;
    private TextView tvStatus;
    private MaterialButton btnToggleAuto;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final List<File> wallpaperFiles = new ArrayList<>();
    private int currentIdx = -1;
    private boolean isAutoRunning = false;

    private final Runnable changeWallpaperRunnable = new Runnable() {
        @Override
        public void run() {
            changeWallpaper();
            handler.postDelayed(this, 30000); // 30 seconds
        }
    };

    private final ActivityResultLauncher<Intent> pickImagesLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    if (data.getClipData() != null) {
                        for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                            saveImageToInternalStorage(data.getClipData().getItemAt(i).getUri());
                        }
                    } else if (data.getData() != null) {
                        saveImageToInternalStorage(data.getData());
                    }
                    loadSavedImages();
                    Toast.makeText(this, "Photos Added!", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallpaper);

        ivWallpaper = findViewById(R.id.ivWallpaper);
        tvStatus = findViewById(R.id.tvStatus);
        MaterialButton btnAddPhotos = findViewById(R.id.btnAddPhotos);
        btnToggleAuto = findViewById(R.id.btnToggleAuto);

        loadSavedImages();

        btnAddPhotos.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            pickImagesLauncher.launch(Intent.createChooser(intent, "Select Wallpapers"));
        });

        btnToggleAuto.setOnClickListener(v -> {
            if (wallpaperFiles.isEmpty()) {
                Toast.makeText(this, "Please add photos first!", Toast.LENGTH_SHORT).show();
                return;
            }
            isAutoRunning = !isAutoRunning;
            if (isAutoRunning) {
                btnToggleAuto.setText("STOP AUTO-CHANGER");
                handler.post(changeWallpaperRunnable);
                Toast.makeText(this, "Started! Wallpaper will change every 30s.", Toast.LENGTH_SHORT).show();
            } else {
                btnToggleAuto.setText("START AUTO-CHANGER");
                handler.removeCallbacks(changeWallpaperRunnable);
            }
        });
    }

    private void loadSavedImages() {
        File dir = new File(getFilesDir(), "wallpapers");
        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                wallpaperFiles.clear();
                wallpaperFiles.addAll(Arrays.asList(files));
            }
        }
        tvStatus.setText("Saved Photos: " + wallpaperFiles.size());
        if (!wallpaperFiles.isEmpty() && currentIdx == -1) {
            // Just preview the first one without setting it as system wallpaper yet
            Bitmap bitmap = BitmapFactory.decodeFile(wallpaperFiles.get(0).getAbsolutePath());
            ivWallpaper.setImageBitmap(bitmap);
        }
    }

    private void saveImageToInternalStorage(Uri uri) {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            if (is == null) return;
            File dir = new File(getFilesDir(), "wallpapers");
            if (!dir.exists()) dir.mkdirs();
            File file = new File(dir, "wp_" + System.currentTimeMillis() + "_" + new Random().nextInt(1000) + ".jpg");
            FileOutputStream fos = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
            fos.close();
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void changeWallpaper() {
        if (wallpaperFiles.isEmpty()) return;

        int newIdx;
        do {
            newIdx = new Random().nextInt(wallpaperFiles.size());
        } while (newIdx == currentIdx && wallpaperFiles.size() > 1);
        currentIdx = newIdx;

        File wpFile = wallpaperFiles.get(currentIdx);
        Bitmap bitmap = BitmapFactory.decodeFile(wpFile.getAbsolutePath());
        if (bitmap == null) return;

        ivWallpaper.setImageBitmap(bitmap);

        // Change actual device wallpaper in a background thread
        new Thread(() -> {
            try {
                WallpaperManager wm = WallpaperManager.getInstance(WallpaperActivity.this);
                wm.setBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // We stop it when activity is destroyed so it doesn't leak or unexpectedly run forever
        handler.removeCallbacks(changeWallpaperRunnable);
    }
}
