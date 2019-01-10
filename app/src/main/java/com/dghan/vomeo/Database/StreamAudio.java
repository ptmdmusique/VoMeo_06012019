package com.dghan.vomeo.Database;

import android.app.Activity;
import android.media.MediaPlayer;
import android.provider.MediaStore;
import android.util.Log;

import java.io.IOException;

public class StreamAudio {
    static MediaPlayer mediaPlayer = null;
    static MediaPlayer.OnCompletionListener complete = null;
    public StreamAudio(){
        mediaPlayer = new MediaPlayer();
        complete = new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mediaPlayer.stop();
                mediaPlayer.reset();
            }
        };
    }
    public void playUrl(String url){
        try {
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepare();
        } catch (IOException e){
        }
    }
    public void play(String term) {
        String url = "https://ssl.gstatic.com/dictionary/static/sounds/oxford/" + term.toLowerCase() + "--_gb_1.mp3";
        try {
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepare();
        } catch (IOException e){
            Log.e("Audio: no sound for", term);
        }
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(complete);
    }

/*    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if(mediaPlayer != null)
            mediaPlayer.release();
    }*/
}
