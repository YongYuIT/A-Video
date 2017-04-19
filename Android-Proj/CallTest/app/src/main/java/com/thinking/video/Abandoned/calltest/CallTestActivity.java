package com.thinking.video.Abandoned.calltest;

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
import android.widget.Toast;

;import com.thinking.video.R;

import org.webrtc.VideoRendererGui;

public class CallTestActivity extends Activity implements CoonTools.onResutListener {


    private GLSurfaceView glsv_main;
    private Button btn_init;
    private Button btn_create_local;
    private Button btn_add_remote;
    private Button btn_answer;
    private EditText edt_address;

    private CoonTools mConnTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("yuyong", "onCreate");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        super.onCreate(savedInstanceState);
        inutUI();
    }

    private void inutUI() {
        Log.i("yuyong", "inutUI start");
        setContentView(R.layout.activity_test);
        btn_init = (Button) findViewById(R.id.btn_init);
        btn_create_local = (Button) findViewById(R.id.btn_create_local);
        btn_add_remote = (Button) findViewById(R.id.btn_add_remote);
        btn_answer = (Button) findViewById(R.id.btn_answer);
        edt_address = (EditText) findViewById(R.id.edt_address);
        glsv_main = (GLSurfaceView) findViewById(R.id.glsv_main);
        //GLSurfaceView创建之后必须立即指定Renderer（即setRenderer）
        //否则报错：Attempt to invoke virtual method 'void android.opengl.GLSurfaceView$GLThread.surfaceCreated()' on a null object reference、
        glsv_main.setPreserveEGLContextOnPause(true);
        glsv_main.setKeepScreenOn(true);
        VideoRendererGui.setView(glsv_main, onGLViewReady);
        Log.i("yuyong", "inutUI end");
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
            mConnTask = new CoonTools(this);
            mConnTask.start("init", getWindowManager(), this);
        }
        if (v.getId() == R.id.btn_create_local) {
            mConnTask.start("create_local");
        }
        if (v.getId() == R.id.btn_add_remote) {
            String ip = String.format("http://%s:3000/", edt_address.getText().toString().equals("") ? edt_address.getHint().toString() : edt_address.getText().toString());
            Log.i("yuyong", "try ip-->" + ip);
            mConnTask.start("add_remote");
        }
        if (v.getId() == R.id.btn_answer) {
            mConnTask.start("answer");
        }
    }

    @Override
    public void onFinished(CoonTools.Result result) {
        Toast.makeText(this, result.methodName, Toast.LENGTH_SHORT).show();
        if (result.methodName.equals("init") && result.result) {
            btn_add_remote.setClickable(true);
            btn_add_remote.setTextColor(Color.WHITE);
            btn_answer.setClickable(true);
            btn_answer.setTextColor(Color.WHITE);
            btn_create_local.setClickable(true);
            btn_create_local.setTextColor(Color.WHITE);
        }

    }
}
