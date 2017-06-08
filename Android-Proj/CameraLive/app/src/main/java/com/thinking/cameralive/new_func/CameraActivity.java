package com.thinking.cameralive.new_func;

import android.app.Activity;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.ViewGroup;

import com.thinking.cameralive.R;

import java.io.IOException;

public class CameraActivity extends Activity {

    private static final int width = 720;
    private static final int height = 480;

    private TextureView txt_view;
    private Camera mCamera;
    private byte mBuf[];

    private boolean isInitSuccess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        txt_view = (TextureView) findViewById(R.id.txt_view);
        txt_view.setSurfaceTextureListener(mTxtViewListener);
    }


    private TextureView.SurfaceTextureListener mTxtViewListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            OnViewReady(surface);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            OnViewStop();
            return false;
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture arg0) {
        }
    };

    private void OnViewReady(SurfaceTexture surface) {

        isInitSuccess = FFmpeg.ffmpegInit(width, height, "rtmp://192.168.0.116/myapp/test");

        try {
            mCamera = Camera.open(1);
        } catch (Exception e) {
            mCamera = null;
            return;
        }
        try {
            Camera.Parameters c_params = mCamera.getParameters();
            c_params.setPreviewSize(width, height);
            c_params.setPictureSize(width, height);
            c_params.setPreviewFormat(ImageFormat.NV21);
            mCamera.setDisplayOrientation(90);
            mCamera.setParameters(c_params);

            ViewGroup.LayoutParams t_params = txt_view.getLayoutParams();
            t_params.width = height;
            t_params.height = width;
            txt_view.setLayoutParams(t_params);

            mBuf = new byte[mCamera.getParameters().getPreviewSize().width * mCamera.getParameters().getPreviewSize().height * 3 / 2];
            mCamera.addCallbackBuffer(mBuf);
            mCamera.setPreviewCallbackWithBuffer(mCameraCallBack);
            mCamera.setPreviewTexture(surface);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCamera.startPreview();
    }

    private void OnViewStop() {
        if (mCamera != null) {
            Camera tmp = mCamera;
            mCamera = null;
            tmp.stopPreview();
            tmp.release();
        }
    }

    private Camera.PreviewCallback mCameraCallBack = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            if (mCamera != null) {
                onFeame(data, camera);
                camera.addCallbackBuffer(data);
            }
        }
    };

    private void onFeame(byte[] data, Camera camera) {
        Log.i("yuyong", "onFeame");
        FFmpeg.ffmpegPush(data);
    }

}
