package com.thinking.ffmpegtest;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

public class MainActivity extends Activity {

    private FFmpeg ffmpeg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FFmpeg ffmpeg = FFmpeg.getInstance(this);
        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {

                @Override
                public void onStart() {
                }

                @Override
                public void onFailure() {
                }

                @Override
                public void onSuccess() {
                }

                @Override
                public void onFinish() {
                }
            });
        } catch (FFmpegNotSupportedException e) {
            Log.i("yuyong", "FFmpegNotSupportedException-->" + e.getMessage());
        }
        try {
            ffmpeg.execute(new String[]{"-version"}, new ExecuteBinaryResponseHandler() {

                @Override
                public void onStart() {
                    Log.i("yuyong", "onStart");
                }

                @Override
                public void onProgress(String message) {
                    Log.i("yuyong", "onProgress-->" + message);
                }

                @Override
                public void onFailure(String message) {
                    Log.i("yuyong", "onFailure-->" + message);
                }

                @Override
                public void onSuccess(String message) {
                    Log.i("yuyong", "onSuccess-->" + message);
                }

                @Override
                public void onFinish() {
                    Log.i("yuyong", "onFinish");
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            Log.i("yuyong", "FFmpegCommandAlreadyRunningException-->" + e.getMessage());
        }
    }
}
