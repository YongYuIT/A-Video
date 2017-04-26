package com.thinking.video.CallAnswer;

import android.app.Activity;
import android.graphics.Color;
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
import com.thinking.video.Tools.AnswerTool;
import com.thinking.video.Tools.ConnTool;

import org.webrtc.VideoRendererGui;

public class AnswerActivity extends Activity implements ConnTool.onResutListener {
    private Button btn_init;
    private Button btn_receive_conn;
    private Button btn_create_answer;
    private Button btn_set_params;

    private GLSurfaceView glsv_main;
    private EditText edt_address;

    private AnswerTool mConnTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        super.onCreate(savedInstanceState);
        inutUI();
    }

    private void inutUI() {
        setContentView(R.layout.activity_answer);
        btn_init = (Button) findViewById(R.id.btn_init);
        btn_receive_conn = (Button) findViewById(R.id.btn_receive_conn);
        btn_create_answer = (Button) findViewById(R.id.btn_create_answer);
        btn_set_params = (Button) findViewById(R.id.btn_set_params);
        edt_address = (EditText) findViewById(R.id.edt_address);
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
        if (v.getId() == R.id.btn_receive_conn) {
            mConnTask.start("setRemote");
        }
        if (v.getId() == R.id.btn_create_answer) {
            mConnTask.start("answer");
        }
        if (v.getId() == R.id.btn_set_params) {
            mConnTask.start("serParams");
        }
    }

    @Override
    public void onFinished(ConnTool.Result result) {
        Log.i("yuyong", result.methodName + "-->" + result.disc);
        if (result.methodName.equals("init") && result.result) {
            btn_receive_conn.setClickable(true);
            btn_receive_conn.setTextColor(Color.WHITE);
        }
        if (result.methodName.equals("setRemote") && result.result) {
            btn_create_answer.setClickable(true);
            btn_create_answer.setTextColor(Color.WHITE);
        }
        if (result.methodName.equals("answer") && result.result) {
            btn_set_params.setClickable(true);
            btn_set_params.setTextColor(Color.WHITE);
        }
    }
}
