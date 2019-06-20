package com.khadejaclarke.musicplayer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;

public class MainActivity extends AppCompatActivity {
    private SeekBar seekBar;
    private ImageView btn_play_pause;
    private ImageView btn_stop;
    private ImageView btn_previous;
    private ImageView btn_next;

    private PlayerAdapter adapter;

    boolean isUserSeeking = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initSeekbar();
        initControls();
        initPlaybackController();
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.loadMedia();
    }

    private void initControls() {
        btn_previous = findViewById(R.id.button_previous);
        btn_previous.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        adapter.previous();
                    }
                });

        btn_play_pause = findViewById(R.id.button_play_pause);
        btn_play_pause.setTag("PLAY");
        btn_play_pause.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (btn_play_pause.getTag().equals("PLAY")) {
                            adapter.play();
                            btn_play_pause.setImageResource(R.drawable.vector_pause);
                            btn_play_pause.setTag("PAUSE");
                            btn_stop.setVisibility(View.VISIBLE);
                        } else {
                            adapter.pause();
                            btn_play_pause.setImageResource(R.drawable.vector_play);
                            btn_play_pause.setTag("PLAY");
                            btn_stop.setVisibility(View.GONE);
                        }

                    }
                });

        btn_stop = findViewById(R.id.button_stop);
        btn_stop.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        adapter.stop();
                    }
                });

        btn_next = findViewById(R.id.button_next);
        btn_next.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        adapter.next();
                    }
                });
    }

    private void initPlaybackController() {
        MediaPlayerHolder holder = new MediaPlayerHolder(this);
        holder.setPlaybackInfoListener(new PlaybackListener());
        adapter = holder;
    }

    private void initSeekbar() {
        seekBar = findViewById(R.id.seekbar);
        seekBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    int userSelectedPosition = 0;

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        isUserSeeking = true;
                    }

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (fromUser) {
                            userSelectedPosition = progress;
                        }
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        isUserSeeking = false;
                        adapter.seekTo(userSelectedPosition);
                    }
                });
    }

    public class PlaybackListener extends PlaybackInfoListener {

        @Override
        public void onDurationChanged(int duration) {
            seekBar.setMax(duration);
        }

        @Override
        public void onPositionChanged(int position) {
            if (!isUserSeeking) {
                seekBar.setProgress(position, true);
            }
        }

        @Override
        public void onPlaybackCompleted() {
            adapter.next();
        }
    }
}
