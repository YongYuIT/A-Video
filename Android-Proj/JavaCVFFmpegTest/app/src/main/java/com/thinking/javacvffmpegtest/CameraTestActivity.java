package com.thinking.javacvffmpegtest;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.ViewGroup;

import java.io.IOException;

public class CameraTestActivity extends Activity {

    TextureView txt_view;
    Camera mCamera;
    byte mBuf[];

    private TextureView.SurfaceTextureListener mTxtViewListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            startPreview(surface);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            release();
            return false;
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture arg0) {
        }
    };

    private Camera.PreviewCallback mCameraCallBack = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            if (mCamera != null) {
                doWhenFrameReady(data, camera);
                camera.addCallbackBuffer(data);
            }
        }
    };

    protected void doWhenFrameReady(byte[] data, Camera camera) {
        //Log.i("yuyong", "size-->" + camera.getParameters().getPreviewSize().width + "," + camera.getParameters().getPreviewSize().height);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView();
        txt_view = (TextureView) findViewById(R.id.txt_view);
        txt_view.setSurfaceTextureListener(mTxtViewListener);
    }

    protected void setContentView() {
        setContentView(R.layout.activity_camera_test);
    }

    private void startPreview(SurfaceTexture surface) {
        try {
            mCamera = Camera.open(1);
        } catch (Exception e) {
            mCamera = null;
            return;
        }
        try {
            int width = 1600;
            int height = 1200;

            Camera.Parameters c_params = mCamera.getParameters();
            c_params.setPreviewSize(width, height);
            mCamera.setParameters(c_params);

            ViewGroup.LayoutParams t_params = txt_view.getLayoutParams();
            t_params.width = width;
            t_params.height = height;
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

    private void release() {
        if (mCamera != null) {
            Camera tmp = mCamera;
            mCamera = null;
            tmp.stopPreview();
            tmp.release();
        }
    }
}
