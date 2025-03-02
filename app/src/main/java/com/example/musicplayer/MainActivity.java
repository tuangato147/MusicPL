package com.example.musicplayer;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.DocumentsContract;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerView;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.Locale;
import java.util.Random;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

public class MainActivity extends AppCompatActivity implements Player.Listener {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int REQUEST_CODE_OPEN_DIRECTORY = 101;
    private static final int REQUEST_CODE_PICK_SONG = 102;

    private ExoPlayer player;
    private EditText searchEditText;
    private Button searchButton;
    private ProgressBar progressBar;
    private TextView songTitleTextView;
    private TextView songStatusTextView;
    private TextView currentTimeTextView;
    private TextView totalTimeTextView;
    private SeekBar seekBar;
    private ImageView playButton;
    private ImageView pauseButton;
    private ImageView nextButton;
    private ImageView previousButton;

    private boolean isShuffleOn = false;
    private int repeatMode = 0; // 0: không lặp lại, 1: lặp lại tất cả, 2: lặp lại một bài
    private ImageView shuffleButton;
    private ImageView repeatButton;

    private ArrayList<String> songFiles = new ArrayList<>();
    private int currentSongIndex = 0;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean isTracking = false;
    private RecyclerView songListRecyclerView;
    private SongAdapter songAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        initializeViews();
        setupPlayer();
        setupClickListeners();
        checkPermissions();
    }

    private void initializeViews() {
        searchEditText = findViewById(R.id.searchEditText);
        searchButton = findViewById(R.id.searchButton);
        progressBar = findViewById(R.id.progressBar);
        songTitleTextView = findViewById(R.id.textView3);
        songStatusTextView = findViewById(R.id.textView2);
        currentTimeTextView = findViewById(R.id.textView4);
        totalTimeTextView = findViewById(R.id.textView5);
        seekBar = findViewById(R.id.seekBar2);
        playButton = findViewById(R.id.imageView3);
        pauseButton = findViewById(R.id.imageView4);
        nextButton = findViewById(R.id.imageView5);
        previousButton = findViewById(R.id.imageView2);

        shuffleButton = findViewById(R.id.shuffleButton);
        repeatButton = findViewById(R.id.repeatButton);

        // Hide search edit text as we're using direct file selection
        searchEditText.setVisibility(View.GONE);
        searchButton.setText("Chọn Nhạc");

        songListRecyclerView = findViewById(R.id.songListRecyclerView);
        songListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        songAdapter = new SongAdapter(new ArrayList<>(), this::onSongSelected);
        songListRecyclerView.setAdapter(songAdapter);
    }

    private void setupPlayer() {
        player = new ExoPlayer.Builder(this).build();
        player.addListener(this);
    }

    private void setupClickListeners() {
        // Tạo animation
        Animation buttonAnimation = AnimationUtils.loadAnimation(this, R.anim.button_press);

        // Áp dụng animation cho các nút
        playButton.setOnClickListener(v -> {
            v.startAnimation(buttonAnimation);
            if (player != null) {
                player.play();
                updatePlaybackStatus("Đang phát nhạc");
            }
        });

        pauseButton.setOnClickListener(v -> {
            v.startAnimation(buttonAnimation);
            if (player != null) {
                player.pause();
                updatePlaybackStatus("Tạm dừng nhạc");
            }
        });

        nextButton.setOnClickListener(v -> {
            v.startAnimation(buttonAnimation);
            playNextSong();
        });

        previousButton.setOnClickListener(v -> {
            v.startAnimation(buttonAnimation);
            playPreviousSong();
        });

        shuffleButton.setOnClickListener(v -> {
            v.startAnimation(buttonAnimation);
            toggleShuffle();
        });

        repeatButton.setOnClickListener(v -> {
            v.startAnimation(buttonAnimation);
            toggleRepeat();
        });

        searchButton.setOnClickListener(v -> {
            v.startAnimation(buttonAnimation);
            pickSongFile();
        });

        setupSeekBarListener();
    }

    private void setupSeekBarListener() {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && player != null) {
                    long duration = player.getDuration();
                    long newPosition = (duration * progress) / 100;
                    currentTimeTextView.setText(formatTime(newPosition));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isTracking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (player != null) {
                    long duration = player.getDuration();
                    long newPosition = (duration * seekBar.getProgress()) / 100;
                    player.seekTo(newPosition);
                }
                isTracking = false;
            }
        });
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, PERMISSION_REQUEST_CODE);
            } else {
                showDirectorySelectionDialog();
            }
        } else {
            showDirectorySelectionDialog();
        }
    }

    private void showDirectorySelectionDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Chọn thư mục nhạc")
                .setMessage("Hãy chọn thư mục chứa nhạc của bạn!")
                .setPositiveButton("OK", (dialog, which) -> requestDirectoryPermission())
                .setCancelable(false)
                .show();
    }

    private void requestDirectoryPermission() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(intent, REQUEST_CODE_OPEN_DIRECTORY);
    }

    private void pickSongFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        startActivityForResult(intent, REQUEST_CODE_PICK_SONG);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == REQUEST_CODE_OPEN_DIRECTORY) {
                handleDirectorySelection(data.getData());
            } else if (requestCode == REQUEST_CODE_PICK_SONG) {
                handleSongSelection(data.getData());
            }
        }
    }

    private void handleDirectorySelection(Uri treeUri) {
        if (treeUri == null) {
            Toast.makeText(this, "Không thể truy cập thư mục", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION |
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
            getContentResolver().takePersistableUriPermission(treeUri, takeFlags);

            DocumentFile pickedDir = DocumentFile.fromTreeUri(this, treeUri);
            if (pickedDir != null && pickedDir.canRead()) {
                songFiles.clear();
                scanMusicFolder(pickedDir);
            } else {
                Toast.makeText(this, "Không thể đọc thư mục đã chọn", Toast.LENGTH_SHORT).show();
            }
        } catch (SecurityException e) {
            Toast.makeText(this, "Lỗi quyền truy cập: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void handleSongSelection(Uri uri) {
        if (uri != null) {
            songFiles.clear();
            songFiles.add(uri.toString());
            currentSongIndex = 0;
            playSong(uri.toString());
        }
    }

    private void scanMusicFolder(DocumentFile directory) {
        if (directory == null || !directory.exists()) {
            runOnUiThread(() -> {
                if (!songFiles.isEmpty()) {
                    songAdapter.updateSongs(songFiles);
                    songAdapter.notifyDataSetChanged();
                    playSong(songFiles.get(0));
                }
            });
            return;
        }

        DocumentFile[] files = directory.listFiles();
        for (DocumentFile file : files) {
            if (file.isFile()) {
                String name = file.getName();
                String mimeType = file.getType();

                if (mimeType != null && mimeType.startsWith("audio/") ||
                        (name != null && (name.endsWith(".mp3") || name.endsWith(".m4a")
                                || name.endsWith(".wav") || name.endsWith(".aac")))) {
                    songFiles.add(file.getUri().toString());
                }
            }
        }

        runOnUiThread(() -> {
            if (songFiles.isEmpty()) {
                Toast.makeText(this, "Không tìm thấy file nhạc nào trong thư mục",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Đã tìm thấy " + songFiles.size() + " bài hát",
                        Toast.LENGTH_SHORT).show();
                songAdapter.updateSongs(songFiles);
                songAdapter.notifyDataSetChanged();
                if (!songFiles.isEmpty()) {
                    playSong(songFiles.get(0));
                }
            }
        });
    }

    private void onSongSelected(int position) {
        currentSongIndex = position;
        playSong(songFiles.get(position));
    }

    private void playSong(String songUri) {
        if (songUri != null) {
            MediaItem mediaItem = MediaItem.fromUri(Uri.parse(songUri));
            player.setMediaItem(mediaItem);
            player.prepare();
            player.play();

            // Update song title
            String fileName = Uri.parse(songUri).getLastPathSegment();
            songTitleTextView.setText(fileName);
            updatePlaybackStatus("Đang phát nhạc");
            startProgressUpdate();
        }
    }

    private void playNextSong() {
        if (!songFiles.isEmpty()) {
            if (repeatMode == 2) {
                // Lặp lại bài hiện tại
                playSong(songFiles.get(currentSongIndex));
                return;
            }
    
            if (isShuffleOn) {
                // Phát ngẫu nhiên
                int nextIndex;
                do {
                    nextIndex = new Random().nextInt(songFiles.size());
                } while (nextIndex == currentSongIndex && songFiles.size() > 1);
                currentSongIndex = nextIndex;
            } else {
                // Phát tuần tự
                currentSongIndex = (currentSongIndex + 1) % songFiles.size();
            }
    
            if (currentSongIndex == 0 && repeatMode == 0) {
                // Dừng phát nếu đã hết danh sách và không lặp lại
                player.stop();
                updatePlaybackStatus("Đã dừng phát nhạc");
            } else {
                playSong(songFiles.get(currentSongIndex));
            }
        }
    }

    private void playPreviousSong() {
        if (!songFiles.isEmpty()) {
            currentSongIndex = (currentSongIndex - 1 + songFiles.size()) % songFiles.size();
            playSong(songFiles.get(currentSongIndex));
        }
    }

    private void startProgressUpdate() {
        handler.removeCallbacksAndMessages(null);
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (player != null && !isTracking) {
                    long duration = player.getDuration();
                    long position = player.getCurrentPosition();
                    updateProgressBar(position, duration);
                    handler.postDelayed(this, 1000);
                }
            }
        });
    }

    private void updateProgressBar(long position, long duration) {
        if (duration > 0) {
            int progress = (int) ((position * 100) / duration);
            seekBar.setProgress(progress);
            currentTimeTextView.setText(formatTime(position));
            totalTimeTextView.setText(formatTime(duration));
        }
    }

    private void updatePlaybackStatus(String status) {
        songStatusTextView.setText(status);
    }

    private String formatTime(long timeMs) {
        StringBuilder formatBuilder = new StringBuilder();
        Formatter formatter = new Formatter(formatBuilder, Locale.getDefault());
        long totalSeconds = timeMs / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        formatBuilder.setLength(0);
        return formatter.format("%02d:%02d", minutes, seconds).toString();
    }

    @Override
    public void onPlaybackStateChanged(int state) {
        if (state == Player.STATE_ENDED) {
            playNextSong();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                showDirectorySelectionDialog();
            } else {
                Toast.makeText(this, "Ứng dụng cần quyền truy cập để đọc file nhạc",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
            player = null;
        }
        handler.removeCallbacksAndMessages(null);
    }

private void toggleShuffle() {
    isShuffleOn = !isShuffleOn;
    if (isShuffleOn) {
        shuffleButton.setImageResource(R.drawable.ic_shuffle_on);
        Toast.makeText(this, "Đã bật phát ngẫu nhiên", Toast.LENGTH_SHORT).show();
    } else {
        shuffleButton.setImageResource(R.drawable.ic_shuffle);
        Toast.makeText(this, "Đã tắt phát ngẫu nhiên", Toast.LENGTH_SHORT).show();
    }
}

private void toggleRepeat() {
    repeatMode = (repeatMode + 1) % 3;
    switch (repeatMode) {
        case 0: // Tắt lặp lại
            repeatButton.setImageResource(R.drawable.ic_repeat_on);
            Toast.makeText(this, "Tắt lặp lại", Toast.LENGTH_SHORT).show();
            break;
        case 1: // Lặp lại tất cả
            repeatButton.setImageResource(R.drawable.ic_repeat_one_on);
            Toast.makeText(this, "Lặp lại tất cả", Toast.LENGTH_SHORT).show();
            break;
        case 2: // Lặp lại một bài
            repeatButton.setImageResource(R.drawable.ic_repeat_one);
            Toast.makeText(this, "Lặp lại một bài", Toast.LENGTH_SHORT).show();
            break;
    }
}
}