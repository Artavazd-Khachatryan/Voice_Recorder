package test.voice.higherlive.simple.recorder.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

import test.voice.higherlive.simple.recorder.R;
import test.voice.higherlive.simple.recorder.application.App;
import test.voice.higherlive.simple.recorder.database.DbRepository;
import test.voice.higherlive.simple.recorder.database.Sound;
import test.voice.higherlive.simple.recorder.dialog.SoundSetNameDialog;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static test.voice.higherlive.simple.recorder.activity.MainActivity.SOUND_COUNT;
import static test.voice.higherlive.simple.recorder.activity.MainActivity.TEMP_RECORD_NAME;
import static test.voice.higherlive.simple.recorder.activity.MainActivity.VOICE_PACKAGE_PATH;


public class RecordSaveActivity extends AppCompatActivity {

    public static final String SOUND_NAME_KAY = "sound name kay";


    FrameLayout flBack;
    TextView tvRecordName, tvDate, tvRecordTime;
    SeekBar sbRecord;
    Button btnSave;
    ToggleButton tbPlay;

    String recordPath;
    long recordTime;

    private String changeRecordName = "change_temp.wav";

    @Inject
    DbRepository dbRepository;

    private MediaPlayer mediaPlayer;
    private Timer seekBarChangeTimer;
    private String timerFormat = "%02d:%02d:%02d";

    private double soundPositionPercent = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_change);

        init();
        injectDagger();
        setViewListener();
        getIntentData();
        setData();

    }

    private void init() {
        flBack = findViewById(R.id.fl_back);
        tbPlay = findViewById(R.id.tb_play);


        tvRecordName = findViewById(R.id.tv_record_name);
        tvDate = findViewById(R.id.tv_date);
        tvRecordTime = findViewById(R.id.tv_record_time);

        sbRecord = findViewById(R.id.sb_record);

        btnSave = findViewById(R.id.btn_save);
    }

    private void injectDagger() {
        App.getINSTANCE().getAppComponent().inject(this);
    }

    private void setViewListener() {
        setTbPlayListener();
        setBtnSaveListener();
        setSbRecordListener();
        setIvBackListener();
    }

    private void setIvBackListener() {
        flBack.setOnClickListener(v -> {
            deleteTempFile();
            onBackPressed();
        });

    }

    private void setTbPlayListener() {
        tbPlay.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                playWavFile(VOICE_PACKAGE_PATH + "/" + TEMP_RECORD_NAME);
            } else {
                pauseMediaplayer();
            }
        });
    }


    private void setBtnSaveListener() {
        btnSave.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            int soundIndex = (SOUND_COUNT == 0) ? 1 : SOUND_COUNT + 1;
            bundle.putString(SOUND_NAME_KAY, "Sound " + soundIndex);
            SoundSetNameDialog dialog = SoundSetNameDialog.newInstance(bundle);
            dialog.setSoundSaveListener((soundName) -> {
                changeRecordName = soundName + ".wav";

                File from = new File(VOICE_PACKAGE_PATH, TEMP_RECORD_NAME);
                File to = new File(VOICE_PACKAGE_PATH, changeRecordName);
                from.renameTo(to);

                saveWavPath(VOICE_PACKAGE_PATH, changeRecordName);
                openMainActivity();
            });
            dialog.show(getSupportFragmentManager(), "TAG");
        });

    }

    private void setSbRecordListener() {
        sbRecord.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                pauseMediaplayer();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                soundPositionPercent = 1.0 * seekBar.getProgress() / sbRecord.getMax();
                String progrestime = calculatePeriud(seekBar.getProgress());
                tvRecordTime.setText(progrestime);
                if (tbPlay.isChecked()) {
                    playWavFile(VOICE_PACKAGE_PATH + "/" + TEMP_RECORD_NAME);
                }
            }
        });
    }


    private void openMainActivity() {
        long handlerDuration = 0;
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            Intent intentMainActivity = new Intent(this, MainActivity.class);
            startActivity(intentMainActivity);
            finish();
        }, handlerDuration);
    }

    private void getIntentData() {
        Intent intent = getIntent();
        recordPath = intent.getStringExtra(MainActivity.RECORD_PATH_KAY);
        recordTime = intent.getLongExtra(MainActivity.RECORD_TIME_KAY, -1);
    }

    private void setData() {
        String date = getDate(recordTime);
        tvDate.setText(date);
    }

    private String getDate(long recordTime) {
        String format = "dd.MM.yyyy";
        String df = (String) DateFormat.format(format, recordTime);
        return df;
    }

    protected void playWavFile(String fileName) {
        stopMediaPlayer();

        mediaPlayer = MediaPlayer.create(this, Uri.parse(fileName));
        int duration = mediaPlayer.getDuration();

        mediaPlayer.seekTo((int) (duration * soundPositionPercent));
        mediaPlayer.setOnCompletionListener(mp -> {
            tbPlay.setChecked(false);
            soundPositionPercent = 0;
            sbRecord.setProgress(0);
            String progrestime = calculatePeriud(0);
            tvRecordTime.setText(progrestime);
        });
        mediaPlayer.start();
        sbRecord.setMax(duration);
        changeSeekBar();
    }

    private void changeSeekBar() {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (mediaPlayer == null)
                    return;

                try {
                    int mediaplayerPosition = mediaPlayer.getCurrentPosition();
                    String progrestime = calculatePeriud(mediaplayerPosition);
                    runOnUiThread(() -> {
                        if (mediaPlayer.isPlaying()) {
                            sbRecord.setProgress(mediaplayerPosition);
                            tvRecordTime.setText(progrestime);
                        } else {
                            cancel();
                        }
                    });
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }


            }
        };

        seekBarChangeTimer = new Timer();
        seekBarChangeTimer.schedule(task, 100, 100);
    }

    private void stopPlayer() {
        if (mediaPlayer != null) {
            stopMediaPlayer();
            stopTimer();
            String progrestime = calculatePeriud(sbRecord.getProgress());
            tvRecordTime.setText(progrestime);
        }
    }

    public void stopMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

    }

    private void pauseMediaplayer() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }

    }

    private void stopTimer() {
        if (seekBarChangeTimer != null)
            seekBarChangeTimer.cancel();
    }

    private void deleteTempFile() {
        File file = new File(VOICE_PACKAGE_PATH + "/" + TEMP_RECORD_NAME);
        file.delete();

        file = new File(VOICE_PACKAGE_PATH + "/" + "change_temp.wav");
        file.delete();
    }


    @SuppressLint("CheckResult")
    private void saveWavPath(String path, String name) {
        Observable.just(path)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .subscribe(s -> {
                    Sound sound = new Sound();
                    sound.path = path + "/" + name;

                    int indexPoint = name.lastIndexOf('.');
                    sound.name = name.substring(0, indexPoint);

                    sound.time = Calendar.getInstance().getTimeInMillis();
                    dbRepository.insert(sound);
                });

        runOnUiThread(() -> {
            Toast.makeText(this, "Sound saved", Toast.LENGTH_SHORT).show();
        });

    }

    public String calculatePeriud(long time) {
        int second = (int) (time / 1000);
        int hour = (int) (second / (60 * 60));
        int minute = (int) (second / 60 - hour * 60);
        second = second - hour * 60 * 60 - minute * 60;

        String date = String.format(timerFormat, hour, minute, second);

        return date;
    }

    @Override
    public void onBackPressed() {
        openMainActivity();
        super.onBackPressed();

    }


    @Override
    protected void onDestroy() {
        stopPlayer();
        deleteTempFile();
        super.onDestroy();
    }


}
