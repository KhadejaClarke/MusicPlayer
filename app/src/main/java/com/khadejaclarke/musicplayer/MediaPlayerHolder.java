package com.khadejaclarke.musicplayer;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MediaPlayerHolder implements PlayerAdapter {
    private Context context;
    public static final int PLAYBACK_POSITION_REFRESH_INTERVAL_MS = 1000;

    private MediaPlayer player;
    private int current = 0;
    private PlaybackInfoListener listener;
    private ScheduledExecutorService service;
    private Runnable seekbarPositionUpdateTask;

    private ArrayList<Integer> files = new ArrayList<>(Arrays.asList(R.raw.jazz_in_paris, R.raw.sunset_ocean_moon, R.raw.random));

    public MediaPlayerHolder(Context context) {
        this.context = context;
    }

    private void initMediaPlayer() {
        if (player == null) {
            player = new MediaPlayer();
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    stopUpdatingCallbackWithPosition();
                    if (listener != null) {
                        listener.onStateChanged(PlaybackInfoListener.State.COMPLETED);
                        listener.onPlaybackCompleted();
                    }
                }
            });
        }
    }

    public void setPlaybackInfoListener(PlaybackInfoListener listener) {
        this.listener = listener;
    }

    @Override
    public void loadMedia() {
        int mResourceId = getItem(current);
        System.out.println("OUTPUT-------------------" + current);

        initMediaPlayer();

        AssetFileDescriptor assetFileDescriptor =
                context.getResources().openRawResourceFd(mResourceId);
        try {
            player.setDataSource(assetFileDescriptor);
        } catch (Exception e) {
            System.out.println(e.toString());
        }

        try {
            player.prepare();
        } catch (Exception e) {
            System.out.println(e.toString());
        }

        initProgressCallback();
    }

    @Override
    public void release() {
        if (player != null) {
            player.release();
            player = null;
        }
    }

    @Override
    public boolean isPlaying() {
        if (player != null)
            return player.isPlaying();
        else
            return false;
    }

    @Override
    public int getItem(int position) {
        return files.get(position);
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    @Override
    public void play() {
        if (player != null && !player.isPlaying()) {
            player.start();
            if (listener != null) {
                listener.onStateChanged(PlaybackInfoListener.State.PLAYING);
            }
            startUpdatingCallbackWithPosition();
        }
    }

    @Override
    public void stop() {
        if (player != null) {
            player.stop();
            loadMedia();
            if (listener != null) {
                listener.onStateChanged(PlaybackInfoListener.State.STOPPED);
            }
            stopUpdatingCallbackWithPosition();
        }
    }

    @Override
    public void pause() {
        if (player != null && player.isPlaying()) {
            player.pause();
            if (listener != null) {
                listener.onStateChanged(PlaybackInfoListener.State.PAUSED);
            }
        }

    }

    @Override
    public void next() {
        current = current == getItemCount()-1 ? 0 : current+1;
        stop();
        player = null;
        loadMedia();
        play();
    }

    @Override
    public void previous() {
        current = current == 0 ? getItemCount()-1 : current-1;
        stop();
        player = null;
        loadMedia();
        play();
    }

    @Override
    public void seekTo(int position) {
        if (player != null)
            player.seekTo(position);
    }

    @Override
    public void initProgressCallback() {
        final int duration = player.getDuration();
        if (listener != null) {
            listener.onDurationChanged(duration);
            listener.onPositionChanged(0);
        }
    }

    /**
     * Syncs the player position with mPlaybackProgressCallback via recurring task.
     */
    private void startUpdatingCallbackWithPosition() {
        if (service == null) {
            service = Executors.newSingleThreadScheduledExecutor();
        }
        if (seekbarPositionUpdateTask == null) {
            seekbarPositionUpdateTask = new Runnable() {
                @Override
                public void run() {
                    updateProgressCallbackTask();
                }
            };
        }
        service.scheduleAtFixedRate(
                seekbarPositionUpdateTask,
                0,
                PLAYBACK_POSITION_REFRESH_INTERVAL_MS,
                TimeUnit.MILLISECONDS
        );
    }

    // Reports media playback position to mPlaybackProgressCallback.
    private void stopUpdatingCallbackWithPosition() {
        if (service != null) {
            service.shutdownNow();
            service = null;
            seekbarPositionUpdateTask = null;
            if (listener != null) {
                listener.onPositionChanged(0);
            }
        }
    }

    private void updateProgressCallbackTask() {
        if (player != null && player.isPlaying()) {
            int currentPosition = player.getCurrentPosition();
            if (listener != null) {
                listener.onPositionChanged(currentPosition);
            }
        }
    }

}
