package com.thinking.video.WebSocket;

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

import com.thinking.video.R;
import com.thinking.video.Tools.AnswerTool;
import com.thinking.video.Tools.ConnTool;
import com.thinking.video.Tools.SocketTool;

import org.json.JSONObject;
import org.webrtc.VideoRendererGui;


public class WebAnswerActivity extends Activity implements ConnTool.onResutListener {

    private SocketTool mSocketTool;
    private AnswerTool mConnTask;

    private GLSurfaceView glsv_main;
    private Button btn_init;
    private Button btn_coon;

    private String mRemoteDisc;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        super.onCreate(savedInstanceState);
        inutUI();
        mSocketTool = SocketTool.getThiz(this);
        mSocketTool.setListener(msListener);
        mSocketTool.doConn("http://192.168.0.114:3333/test/test_page");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSocketTool.doDisConn();
    }

    private SocketTool.Listener msListener = new SocketTool.Listener() {
        @Override
        public void onMessage(String msg) {
            Log.i("yuyong", "socket_onMessage-->" + msg);
            JSONObject j_msg = null;
            try {
                j_msg = new JSONObject(msg);
            } catch (Exception e) {
                e.printStackTrace();
                Log.i("yuyong", "onMessage-->" + e.getMessage());
            }
            if (j_msg == null)
                return;
            String title = null;
            try {
                title = j_msg.get("title").toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (title == null)
                return;
            if (title.equals("createOffer")) {
                onCoonRequest(j_msg);
            }
            if (title.equals("params")) {
                onParams(j_msg);
            }
        }
    };

    private void onCoonRequest(JSONObject json) {
        String msg = null;
        try {
            msg = json.get("msg").toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        onFinished(new ConnTool.Result("onCoonRequest", msg, true));
        Log.i("yuyong", "onCoonRequest-->" + msg);
    }

    private void onParams(JSONObject json) {
        String msg = null;
        try {
            msg = new String(Base64.decode(json.get("msg").toString(), Base64.DEFAULT));
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i("yuyong", "onParams-->" + msg);
        onFinished(new ConnTool.Result("onParams", msg, true));
    }

    private void inutUI() {
        setContentView(R.layout.activity_web_answer);
        btn_init = (Button) findViewById(R.id.btn_init);
        btn_coon = (Button) findViewById(R.id.btn_coon);
        glsv_main = (GLSurfaceView) findViewById(R.id.glsv_main);
        //GLSurfaceView创建之后必须立即指定Renderer（即setRenderer）
        //否则报错：Attempt to invoke virtual method 'void android.opengl.GLSurfaceView$GLThread.surfaceCreated()' on a null object reference
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
        Log.i("yuyong", "onClick");
        if (v.getId() == R.id.btn_init) {
            mConnTask = new AnswerTool(this);
            mConnTask.start("init", getWindowManager(), this);
        }
        if (v.getId() == R.id.btn_coon) {
            mConnTask.start("setRemote", mRemoteDisc);
        }
    }

    @Override
    public void onFinished(ConnTool.Result result) {
        if (result.methodName.equals("onCoonRequest") && result.result) {
            mRemoteDisc = result.disc;
            btn_coon.setClickable(true);
            btn_coon.setTextColor(Color.WHITE);
        }
        if (result.methodName.equals("setRemote") && result.result) {
            mConnTask.start("answer");
        }
        if (result.methodName.equals("answer") && result.result) {
            Log.i("yuyong", "onFinished--answer-->" + result.disc);
            try {
                JSONObject msg = new JSONObject();
                msg.put("title", "answer");
                msg.put("msg", result.disc);
                mSocketTool.sendMsg(msg.toString().replace("{", "\\{").replace("}", "\\}").replace("\"", "\\\""));
            } catch (Exception e) {
                e.printStackTrace();
                Log.i("yuyong", "answer-->error:" + e.getMessage());
            }
        }
        if (result.methodName.equals("onIceGatheringChange") && result.result) {
            try {
                JSONObject msg = new JSONObject();
                msg.put("title", "params");
                msg.put("msg", Base64.encodeToString(result.disc.getBytes(), Base64.DEFAULT));
                mSocketTool.sendMsg(msg.toString().replace("{", "\\{").replace("}", "\\}").replace("\"", "\\\""));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (result.methodName.equals("onParams") && result.result) {
            mConnTask.start("setParams", result.disc);
        }
    }
}
