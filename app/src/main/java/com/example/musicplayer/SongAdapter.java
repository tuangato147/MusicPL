package com.example.musicplayer;

import java.util.List;  // cho lỗi cannot find symbol class List
import android.net.Uri; // cho lỗi cannot find symbol variable Uri
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {
    private List<String> songs;
    private OnSongClickListener listener;

    public interface OnSongClickListener {
        void onSongClick(int position);
    }

    public SongAdapter(List<String> songs, OnSongClickListener listener) {
        this.songs = songs;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.song_item, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        String songPath = songs.get(position);
        String songName = Uri.parse(songPath).getLastPathSegment();
        holder.songName.setText(songName);
        holder.itemView.setOnClickListener(v -> listener.onSongClick(position));
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    public void updateSongs(List<String> newSongs) {
        this.songs = newSongs;
    }

    static class SongViewHolder extends RecyclerView.ViewHolder {
        TextView songName;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            songName = itemView.findViewById(R.id.songName);
        }
    }
}
