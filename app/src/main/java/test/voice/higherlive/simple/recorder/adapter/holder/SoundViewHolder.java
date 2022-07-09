package test.voice.higherlive.simple.recorder.adapter.holder;

import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;


import java.util.Timer;
import java.util.TimerTask;

import test.voice.higherlive.simple.recorder.R;
import test.voice.higherlive.simple.recorder.adapter.SoundAdapter;
import test.voice.higherlive.simple.recorder.database.Sound;
import test.voice.higherlive.simple.recorder.mediaplayer.MediallyHandler;

public class SoundViewHolder extends RecyclerView.ViewHolder {

    public TextView tvRecordName;
    public TextView tvDate;
    public View viewLine;
    private TextView tvRecordTime;
    private ImageView ivDelete;

    private ToggleButton tbPlay;

    private SeekBar sbRecord;

    private Sound sound;
    private MediaPlayer mediaPlayer;

    private String timerFormat = "%02d:%02d:%02d";

    private Timer timerSeekBarChange;
    private ItemDeleteCalback deleteCallback;

    private SoundAdapter.SoundItemClickListener soundItemClickListener;

    public SoundViewHolder(@NonNull View itemView) {
        super(itemView);
        init();
        setViewListener();

    }

    private void init() {

        viewLine = itemView.findViewById(R.id.view_line);
        ivDelete = itemView.findViewById(R.id.iv_delete);

        tvRecordName = itemView.findViewById(R.id.tv_record_name);
        tvDate = itemView.findViewById(R.id.tv_date);
        tvRecordTime = itemView.findViewById(R.id.tv_record_time);

        tbPlay = itemView.findViewById(R.id.tb_play);

        sbRecord = itemView.findViewById(R.id.sb_record);


    }


    public void setSound(Sound sound) {
        this.sound = sound;

        tvRecordName.setText(sound.name);
        tvDate.setText(getDate(sound.time));
    }


    private void setViewListener() {

        tbPlay.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                soundItemClickListener.onItemClick(this);
                playSound();

            } else {
                stopPlayer();
            }
        });

        sbRecord.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    mediaPlayer.seekTo(seekBar.getProgress());
                    mediaPlayer.start();
                }
            }
        });

        ivDelete.setOnClickListener(v -> deleteCallback.delete(getAdapterPosition(), sound));
    }

    public void setDeleteCallback(ItemDeleteCalback deleteCallback) {
        this.deleteCallback = deleteCallback;
    }

    private void playSound() {
        mediaPlayer = MediallyHandler.create(this, itemView.getContext(), Uri.parse(sound.path));
        mediaPlayer.seekTo(sbRecord.getProgress());
        mediaPlayer.setOnCompletionListener(mp -> {
            tbPlay.setChecked(false);
            sbRecord.setProgress(0);
            String progrestime = calculatePeriud(0);
            tvRecordTime.setText(progrestime);
        });
        mediaPlayer.start();
        sbRecord.setMax(mediaPlayer.getDuration());

        changeSeekBar();
    }

    private void stopPlayer() {
        MediallyHandler.stop();
        timerSeekBarChange.cancel();
        sbRecord.setProgress(0);
        String progrestime = calculatePeriud(0);
        tvRecordTime.setText(progrestime);
    }

    private void changeSeekBar() {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    int mediaplayerPosition = mediaPlayer.getCurrentPosition();
                    String progrestime = calculatePeriud(mediaplayerPosition);

                    sbRecord.post(() -> {
                        if (mediaPlayer.isPlaying()) {
                            sbRecord.setProgress(mediaplayerPosition);
                            tvRecordTime.setText(progrestime);
                        }
                    });

                }


            }
        };

        timerSeekBarChange = new Timer();
        timerSeekBarChange.schedule(task, 100, 100);
    }

    public String calculatePeriud(long time) {
        int second = (int) (time / 1000);
        int hour = (int) (second / (60 * 60));
        int minute = (int) (second / 60 - hour * 60);
        second = second - hour * 60 * 60 - minute * 60;

        String date = String.format(timerFormat, hour, minute, second);

        return date;
    }

    private String getDate(long recordTime) {
        String format = "dd.MM.yyyy";
        String df = (String) DateFormat.format(format, recordTime);
        return df;
    }

    public void play() {
        tbPlay.setChecked(true);
    }

    public void stop() {
        tbPlay.setChecked(false);
    }

    public void setSoundItemClickListener(SoundAdapter.SoundItemClickListener soundItemClickListener) {
        this.soundItemClickListener = soundItemClickListener;
    }

    public interface ItemDeleteCalback {
        void delete(int position, Sound sound);
    }
}
