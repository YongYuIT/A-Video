package com.thinking.ffmpegtest;

import android.app.Activity;
import android.content.Intent;
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
            FFmpegTools.getStreamFromFile("/sdcard/DCIM/Camera/test.flv", "rtmp://192.168.0.130:1935/myapp/test");
        }
        if (v.getId() == R.id.btn_test_steam_camera) {
            FFmpegTools.setStreamFromCameraInit(1600, 1200, "rtmp://192.168.0.103/myapp/test");
            startActivity(new Intent(this, CameraActivity.class));
        }
    }
}
