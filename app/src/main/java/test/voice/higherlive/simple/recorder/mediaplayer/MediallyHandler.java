package test.voice.higherlive.simple.recorder.mediaplayer;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;

import test.voice.higherlive.simple.recorder.adapter.holder.SoundViewHolder;


public class MediallyHandler {

    private static MediaPlayer mediaPlayer;
    public static SoundViewHolder currentViewHolder;

    public static MediaPlayer create(SoundViewHolder viewHolder, Context context, Uri uri) {
        currentViewHolder = viewHolder;
        if (mediaPlayer != null) {
            stop();
        }
        mediaPlayer = MediaPlayer.create(context, uri);
        return mediaPlayer;
    }

    public static void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }

    }
}
