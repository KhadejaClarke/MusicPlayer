package com.khadejaclarke.musicplayer;

public interface PlayerAdapter {

    void loadMedia();

    void release();

    boolean isPlaying();

    void play();

    void stop();

    void pause();

    void next();

    void previous();

    void initProgressCallback();

    void seekTo(int position);

    int getItem(int position);

    int getItemCount();
}