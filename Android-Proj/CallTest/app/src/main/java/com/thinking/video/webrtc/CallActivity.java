package com.thinking.video.webrtc;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Point;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.thinking.video.R;

import org.webrtc.MediaStream;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoRendererGui;

public class CallActivity extends Activity {

    private static final String VIDEO_CODEC_VP9 = "VP9";
    private static final String AUDIO_CODEC_OPUS = "opus";

    private static final int LOCAL_X_CONNECTING = 0;
    private static final int LOCAL_Y_CONNECTING = 0;
    private static final int LOCAL_WIDTH_CONNECTING = 100;
    private static final int LOCAL_HEIGHT_CONNECTING = 100;

    private VideoRendererGui.ScalingType scalingType = VideoRendererGui.ScalingType.SCALE_ASPECT_FILL;

    private GLSurfaceView glsv_main;
    private String p_address;
    private Button btn_call;
    private EditText edt_address;
    private WebRtcClient mClient;
    private VideoRenderer.Callbacks localRender;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        setContentView(R.layout.activity_call);
        btn_call = (Button) findViewById(R.id.btn_add_remote);
        edt_address = (EditText) findViewById(R.id.edt_address);
        glsv_main = (GLSurfaceView) findViewById(R.id.glsv_main);
        //GLSurfaceView创建之后必须立即指定Renderer（即setRenderer）
        //否则报错：Attempt to invoke virtual method 'void android.opengl.GLSurfaceView$GLThread.surfaceCreated()' on a null object reference、
        glsv_main.setPreserveEGLContextOnPause(true);
        glsv_main.setKeepScreenOn(true);
        VideoRendererGui.setView(glsv_main, onGLViewReady);
    }

    @Override
    public void onPause() {
        super.onPause();
        glsv_main.onPause();
        if (mClient != null) {
            mClient.onPause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        glsv_main.onResume();
        if (mClient != null) {
            mClient.onResume();
        }
    }

    @Override
    public void onDestroy() {
        if (mClient != null) {
            mClient.onDestroy();
        }
        super.onDestroy();
    }

    private Runnable onGLViewReady = new Runnable() {
        @Override
        public void run() {
            mHandler.sendEmptyMessage(1001);
        }
    };

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1001:
                    btn_call.setClickable(true);
                    btn_call.setTextColor(Color.WHITE);
                    break;
                default:
                    break;
            }
        }
    };

    public void onClick(View v) {
        if (v.getId() == R.id.btn_add_remote) {
            p_address = String.format("http://%s:3000/", edt_address.getText().toString().equals("") ? edt_address.getHint().toString() : edt_address.getText().toString());
            Point displaySize = new Point();
            getWindowManager().getDefaultDisplay().getSize(displaySize);
            PeerConnectionParameters params = new PeerConnectionParameters(
                    true, false, displaySize.x, displaySize.y, 30, 1, VIDEO_CODEC_VP9, true, 1, AUDIO_CODEC_OPUS, true);
            mClient = new WebRtcClient(this, WebRtcCallListener, p_address, params, VideoRendererGui.getEGLContext());
        }
    }

    private WebRtcClient.RtcListener WebRtcCallListener = new WebRtcClient.RtcListener() {
        @Override
        public void onAddRemoteStream(MediaStream remoteStream, int endPoint) {

        }

        @Override
        public void onCallReady(String callId) {
            Log.i("yuyong", "onCallReady-->" + callId);
            //准备完毕，发起p2p连接，这里的callId是自己的通信标识
            mClient.start("yong_yu");
        }

        @Override
        public void onStatusChanged(String newStatus) {

        }

        @Override
        public void onLocalStream(MediaStream localStream) {
            Log.i("yuyong", "onLocalStream");
            if (localRender == null) {
                localRender = VideoRendererGui.create(LOCAL_X_CONNECTING, LOCAL_Y_CONNECTING, LOCAL_WIDTH_CONNECTING, LOCAL_HEIGHT_CONNECTING, scalingType, true);
            }
            localStream.videoTracks.get(0).addRenderer(new VideoRenderer(localRender));
            VideoRendererGui.update(localRender, LOCAL_X_CONNECTING, LOCAL_Y_CONNECTING, LOCAL_WIDTH_CONNECTING, LOCAL_HEIGHT_CONNECTING, scalingType);
        }

        @Override
        public void onRemoveRemoteStream(int endPoint) {
            Log.i("yuyong", "onRemoveRemoteStream");
            VideoRendererGui.update(localRender, LOCAL_X_CONNECTING, LOCAL_Y_CONNECTING, LOCAL_WIDTH_CONNECTING, LOCAL_HEIGHT_CONNECTING, scalingType);
        }
    };


}
