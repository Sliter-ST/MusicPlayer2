package com.example.musicplayer2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class MusicAdapter extends ArrayAdapter<Music> {
    private Context context;
    private List<Music> musics;
    private Music music;

    public MusicAdapter(Context context, int textViewResourceId, List<Music> musics) {
        super(context, textViewResourceId, musics);
        this.context = context;
        this.musics = musics;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        music = musics.get(position);
        View view = LayoutInflater.from(context).inflate(R.layout.music_item, null);
        TextView songname = (TextView) view.findViewById(R.id.songname);
        TextView singer = (TextView) view.findViewById(R.id.singer);
        songname.setText(music.getTitle());         //显示标题
        singer.setText(music.getArtist());       //显示艺术家
        return view;

    }
}
