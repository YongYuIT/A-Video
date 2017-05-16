package com.thinking.javacvffmpegtest;

//参考http://www.cnblogs.com/android-blogs/p/5816912.html

import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;

import java.nio.ByteBuffer;

public class FFCameraTestActivity extends CameraTestActivity {

    private FFmpegFrameRecorder mRecorder;
    private long startTime = -1;
    private Frame yuvImage = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        {
            super.onCreate(savedInstanceState);
            initRecorder();
        }
    }

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_ffcamera_test);
    }

    public void onClick(View v) {
        if (v.getId() == R.id.btn_test_rtmp) {
            startRecoed();
        }
    }

    @Override
    protected void doWhenFrameReady(byte[] data, Camera camera) {
        super.doWhenFrameReady(data, camera);
        if (mRecorder != null || startTime == -1) {
            return;
        }
        long t = 1000 * (System.currentTimeMillis() - startTime);
        if (t > mRecorder.getTimestamp()) {
            mRecorder.setTimestamp(t);
        }
        ((ByteBuffer) yuvImage.image[0].position(0)).put(data);
        try {
            mRecorder.record(yuvImage);
        } catch (Exception e) {
            Log.i("yuyong_ff", "doWhenFrameReady-->" + e.getMessage());
        }
    }

    private void startRecoed() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    mRecorder.start();
                    yuvImage = new Frame(1600, 1200, Frame.DEPTH_UBYTE, 2);
                    startTime = System.currentTimeMillis();
                } catch (Exception e) {
                    Log.i("yuyong_ff", "startRecoed-->" + e.getMessage());
                }
            }
        }.start();
    }

    private void initRecorder() {
        mRecorder = new FFmpegFrameRecorder("rtmp://192.168.0.119:1935/myapp/test", 1600, 1200, 1);
        mRecorder.setVideoCodec(28);
        mRecorder.setFormat("flv");
        mRecorder.setSampleRate(44100);
        mRecorder.setFrameRate(30);
    }
}
