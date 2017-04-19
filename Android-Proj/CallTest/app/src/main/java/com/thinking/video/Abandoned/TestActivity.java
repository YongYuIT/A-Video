package com.thinking.video.Abandoned;

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

import com.thinking.video.R;

import org.webrtc.VideoRendererGui;

public class TestActivity extends Activity implements InitConnTask.onResutListener {


    private GLSurfaceView glsv_main;
    private Button btn_init;
    private Button btn_call;
    private EditText edt_address;

    private InitConnTask mConnTask;

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
        btn_call = (Button) findViewById(R.id.btn_add_remote);
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
            mConnTask = new InitConnTask(this);
            mConnTask.execute("init", getWindowManager(), this);
        }
        if (v.getId() == R.id.btn_add_remote) {
            String ip = String.format("http://%s:3000/", edt_address.getText().toString().equals("") ? edt_address.getHint().toString() : edt_address.getText().toString());
            Log.i("yuyong", "try ip-->" + ip);
            mConnTask.execute("conn", ip, getWindowManager(), this);
        }
    }

    @Override
    public void onInited(InitConnTask.Result result) {
        Toast.makeText(this, result.methodName, Toast.LENGTH_SHORT).show();
        if (result.methodName.equals("init") && result.result) {
            btn_call.setClickable(true);
            btn_call.setTextColor(Color.WHITE);
        }

    }
}
