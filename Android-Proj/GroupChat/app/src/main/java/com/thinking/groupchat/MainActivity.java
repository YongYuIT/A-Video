package com.thinking.groupchat;

import android.app.Activity;
import android.graphics.Color;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.thinking.groupchat.WebRtc.CallAndAnsTool;
import com.thinking.groupchat.WebRtc.ConnTool;
import com.thinking.groupchat.WebRtc.SocketTool;

import org.webrtc.VideoRendererGui;

public class MainActivity extends Activity implements ConnTool.onResutListener, SocketTool.Listener {

    private CallAndAnsTool mWebRtcTool;
    private SocketTool mSocketTool;

    private GLSurfaceView glsv_main;
    private Button btn_init;
    private Button btn_create_conn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        super.onCreate(savedInstanceState);
        inutUI();
        mSocketTool = SocketTool.getThiz(this);
        mSocketTool.setListener(this);
    }


    private void inutUI() {
        setContentView(R.layout.activity_main);
        btn_init = (Button) findViewById(R.id.btn_init);
        btn_create_conn = (Button) findViewById(R.id.btn_create_conn);
        glsv_main = (GLSurfaceView) findViewById(R.id.glsv_main);
        //GLSurfaceView创建之后必须立即指定Renderer（即setRenderer）
        //否则报错：Attempt to invoke virtual method 'void android.opengl.GLSurfaceView$GLThread.surfaceCreated()' on a null object reference、
        glsv_main.setPreserveEGLContextOnPause(true);
        glsv_main.setKeepScreenOn(true);
        VideoRendererGui.setView(glsv_main, onGLViewReady);
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
                    btn_init.setClickable(true);
                    btn_init.setTextColor(Color.WHITE);
                    break;
                default:
                    break;
            }
        }
    };

    public void onClick(View v) {
        if (v.getId() == R.id.btn_init) {
            mWebRtcTool = new CallAndAnsTool(this);
            mWebRtcTool.start("init", 2, getWindowManager(), this);
            Log.i("yuyong", "doConn-->" + getServerUrl());
            mSocketTool.doConn(getServerUrl());
        }
        if (v.getId() == R.id.btn_create_conn) {
            mWebRtcTool.start("createOffer");
        }
    }

    @Override
    public void onFinished(ConnTool.Result result) {
        Log.i("yuyong", "onFinished--" + result.methodName + "--" + result.result);
        if (result.methodName.equals("init") && result.result) {
            btn_create_conn.setClickable(true);
            btn_create_conn.setTextColor(Color.WHITE);
        }
        if (result.methodName.equals("createOffer") && result.result) {
            mSocketTool.sendMessage("report_sdp####" + Base64.encodeToString(result.disc.getBytes(), Base64.NO_WRAP));
        }
        if (result.methodName.equals("createAnswer") && result.result) {
            mSocketTool.sendMessage("answer_sdp####" + Base64.encodeToString(result.disc.getBytes(), Base64.NO_WRAP));
        }
        if (result.methodName.equals("answer_setRemoteDescription") && result.result) {
            mWebRtcTool.start("createAnswer");
        }

        if (result.methodName.equals("call_onIceGatheringChange") && result.result) {
            Log.i("yuyong", "call_params-->" + result.disc);
            mSocketTool.sendMessage("report_params####" + Base64.encodeToString(result.disc.getBytes(), Base64.NO_WRAP));
        }

        if (result.methodName.equals("answer_onIceGatheringChange") && result.result) {
            Log.i("yuyong", "answer_params-->" + result.disc);
            mSocketTool.sendMessage("answer_params####" + Base64.encodeToString(result.disc.getBytes(), Base64.NO_WRAP));
        }
    }


    private String getServerUrl() {
        EditText edt_address = (EditText) findViewById(R.id.edt_address);
        String url = edt_address.getText().toString();
        if (url.equals(""))
            url = edt_address.getHint().toString();
        return "http://" + url;
    }

    @Override
    public void onMessage(String msg) {
        Log.i("yuyong", "onMessage-->" + msg);
        if (msg.split("####")[0].equalsIgnoreCase("get_remote")) {
            String sdp = new String(Base64.decode(msg.split("####")[1], Base64.DEFAULT));
            Log.i("yuyong", "get_remote-->" + sdp);
            mWebRtcTool.start("receiveConn", sdp);
        }
        if (msg.split("####")[0].equalsIgnoreCase("set_remote")) {
            String sdp = new String(Base64.decode(msg.split("####")[1], Base64.DEFAULT));
            Log.i("yuyong", "set_remote-->" + sdp);
            mWebRtcTool.start("receiveAnswer", sdp);
        }
        if (msg.split("####")[0].equalsIgnoreCase("get_params")) {
            String params = new String(Base64.decode(msg.split("####")[1], Base64.DEFAULT));
            Log.i("yuyong", "get_params-->" + params);
            mWebRtcTool.start("receiveParams", params);
        }
        if (msg.split("####")[0].equalsIgnoreCase("set_params")) {
            String params = new String(Base64.decode(msg.split("####")[1], Base64.DEFAULT));
            Log.i("yuyong", "set_params-->" + params);
            mWebRtcTool.start("receiveAnswerParams", params);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSocketTool.doDisConn();
    }
}
