package com.thinking.ffmpegtest;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class FFmpegActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ffmpeg);
    }

    public void onClick(View v) {
        if (v.getId() == R.id.btn_test_lib) {
            FFmpegTools.test();
        }
        if (v.getId() == R.id.btn_test_steam_file) {
            FFmpegTools.getStreamFromFile("/sdcard/DCIM/Camera/test.avi", "rtmp://192.168.0.119:1935/myapp/test");
        }
    }
}
