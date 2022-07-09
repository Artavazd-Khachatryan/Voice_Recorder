
package test.voice.higherlive.simple.recorder.activity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import java.util.List;
import java.util.concurrent.TimeUnit;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import javax.inject.Inject;

import test.voice.higherlive.simple.recorder.R;
import test.voice.higherlive.simple.recorder.adapter.SoundAdapter;
import test.voice.higherlive.simple.recorder.adapter.holder.SoundViewHolder;
import test.voice.higherlive.simple.recorder.application.App;
import test.voice.higherlive.simple.recorder.database.DbRepository;
import test.voice.higherlive.simple.recorder.database.Sound;
import test.voice.higherlive.simple.recorder.dialog.DeleteDialog;
import test.voice.higherlive.simple.recorder.mediaplayer.MediallyHandler;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import omrecorder.AudioRecordConfig;
import omrecorder.OmRecorder;
import omrecorder.PullTransport;
import omrecorder.PullableSource;
import omrecorder.Recorder;

public class MainActivity extends AppCompatActivity {


    public static int SOUND_COUNT = 0;
    public static final int PERMISSION_RECORD_AND_WRITE = 1001;
    public static final String RECORD_PATH_KAY = "record path";
    public static final String RECORD_TIME_KAY = "record time";

    public static final String EXTERNAL_STORAGE_PATH = Environment.getExternalStorageDirectory() + "";
    public static final String VOICE_PACKAGE_NAME = "Voice Recorder";
    public static final String VOICE_PACKAGE_PATH = EXTERNAL_STORAGE_PATH + "/" + VOICE_PACKAGE_NAME;

    ToggleButton tbRecord;
    TextView tvTimer;
    FrameLayout flArrow;
    ImageView ivArrow;
    RecyclerView rvSoundList;
    SlidingUpPanelLayout spLayout;

    Recorder recorder;

    public static final String TEMP_RECORD_NAME = "temp.wav";
    String timerFormat = "%02d:%02d:%02d";

    boolean recorderIsStarted = false;


    @Inject
    DbRepository dbRepository;
    List<Sound> sounds = new ArrayList<>();

    Disposable timerDisposable;

    SoundAdapter soundAdapter;

    SoundViewHolder lastSoundViewHolder;


    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        setActivityFullscreen();

        init();
        setListener();
        injectDagger();
        getSounds();


    }

    private void init() {
        spLayout = findViewById(R.id.slide_up_panel_layout);

        tbRecord = findViewById(R.id.tb_record);
        tvTimer = findViewById(R.id.tvTimer);

        flArrow = findViewById(R.id.fl_arrow);
        ivArrow = findViewById(R.id.iv_arrow);

        rvSoundList = findViewById(R.id.rv_sound_list);
        rvSoundList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        soundAdapter = new SoundAdapter(sounds);
        rvSoundList.setAdapter(soundAdapter);
        rvSoundList.setItemAnimator(new DefaultItemAnimator());

    }

    private void setActivityFullscreen() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_RECORD_AND_WRITE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    startRecord();
                } else {
                    tbRecord.setChecked(false);
                }
                break;
            }

        }
    }


    private void setListener() {
        tbRecord.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (checkPermission()) {
                    startRecord();
                }
            } else {
                if (recorderIsStarted) {
                    MediallyHandler.stop();
                    openRecordChangeActivity();
                }

                stopRecord();
            }
        });

        soundAdapter.setSoundItemClickListener(soundViewHolder -> {
            if (lastSoundViewHolder == soundViewHolder)
                return;

            if (lastSoundViewHolder != null)
                lastSoundViewHolder.stop();

            lastSoundViewHolder = soundViewHolder;
        });

        spLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                ivArrow.setRotation(calculateCorner(0 - slideOffset));
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {


            }
        });

        soundAdapter.setItemDeleteCallback((position, sound) -> {
            DeleteDialog deleteDialog = new DeleteDialog();
            deleteDialog.show(getSupportFragmentManager(), DeleteDialog.class.getName());
            deleteDialog.setOkCallback(() -> {
                soundAdapter.notifyItemRemoved(position);
                soundAdapter.notifyItemRangeChanged(position, soundAdapter.getItemCount());
                deleteSoundFile(sound.path);
                new Thread(() -> {
                    dbRepository.delete(sound);
                    runOnUiThread((() -> Toast.makeText(MainActivity.this, "Record is deleted", Toast.LENGTH_SHORT).show()));
                }).start();
            });

        });
    }

    private float calculateCorner(float slideOffset) {
        return slideOffset * 180;
    }

    private void createPackageVoice() {
        File rootPath = new File(EXTERNAL_STORAGE_PATH, VOICE_PACKAGE_NAME);
        if (!rootPath.exists()) {
            rootPath.mkdirs();
        }
    }

    private void startTimer() {
        Observable timeLoader = Observable.defer(() -> Observable.interval(0, 1, TimeUnit.SECONDS))
                .timeInterval()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

        long getSystemTime = Calendar.getInstance().getTimeInMillis();
        timerDisposable = timeLoader.subscribe(time -> tvTimer.setText(calculatePeriud(getSystemTime)));
    }

    private void stopTimer() {
        if (timerDisposable != null && !timerDisposable.isDisposed()) {
            timerDisposable.dispose();
        }
    }

    public String calculatePeriud(long startTime) {
        long currentTime = Calendar.getInstance().getTimeInMillis();
        int second = (int) ((currentTime - startTime) / 1000);
        int hour = (int) (second / (60 * 60));
        int minute = (int) (second / 60 - hour * 60);
        second = second - hour * 60 * 60 - minute * 60;

        String date = String.format(timerFormat, hour, minute, second);

        return date;
    }

    private void openRecordChangeActivity() {
        long handlerDuration = 500;
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            Intent intentRecordChangeActivity = new Intent(this, RecordSaveActivity.class);
            intentRecordChangeActivity.putExtra(RECORD_PATH_KAY, VOICE_PACKAGE_PATH + "/" + TEMP_RECORD_NAME);
            intentRecordChangeActivity.putExtra(RECORD_TIME_KAY, Calendar.getInstance().getTimeInMillis());
            startActivity(intentRecordChangeActivity);
            finish();
        }, handlerDuration);

    }

    private void injectDagger() {
        App.getINSTANCE().getAppComponent().inject(this);
    }

    private void createRecorder() {
        recorder = OmRecorder.wav(
                new PullTransport.Default(mic(), audioChunk -> animateVoice((float) (audioChunk.maxAmplitude() / 200.0))), file());
    }

    private PullableSource mic() {
        return new PullableSource.Default(
                new AudioRecordConfig.Default(
                        MediaRecorder.AudioSource.MIC, AudioFormat.ENCODING_PCM_16BIT,
                        AudioFormat.CHANNEL_IN_MONO, 44100)
        );
    }

    private void animateVoice(final float maxPeak) {
        tbRecord.animate().scaleX(1 + maxPeak).scaleY(1 + maxPeak).setDuration(10).start();
    }

    @NonNull
    private File file() {
        createPackageVoice();
        File file = new File(VOICE_PACKAGE_PATH, TEMP_RECORD_NAME);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    private void startRecord() {
        recorderIsStarted = true;
        stopPlayer();
        createRecorder();
        recorder.startRecording();
        startTimer();

    }

    private void stopPlayer() {
        if (MediallyHandler.currentViewHolder != null) {
            MediallyHandler.currentViewHolder.stop();
        }

        MediallyHandler.stop();
    }

    private void stopRecord() {
        if (recorder != null) {
            recorder.pauseRecording();
            try {
                recorder.stopRecording();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        stopTimer();
        animateVoice(0);

        recorderIsStarted = false;

    }

    private boolean checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_RECORD_AND_WRITE);

            return false;
        }
        return true;
    }

    @SuppressLint("CheckResult")
    private void getSounds() {
        dbRepository.getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(sounds -> {
                    this.sounds = sounds;
                    SOUND_COUNT = sounds.size();

                    soundAdapter.setSoundList(sounds);
                    soundAdapter.notifyDataSetChanged();
                });
    }

    private void deleteSoundFile(String path) {
        File file = new File(path);
        file.delete();
    }

}