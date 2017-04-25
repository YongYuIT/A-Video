package com.thinking.websockettest;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity {

    CoonTool mCoonTool;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void onClick(View v) {
        if (v.getId() == R.id.btn_test_coon) {
            mCoonTool = CoonTool.getThiz(this);
            mCoonTool.setListener(new CoonTool.Listener() {
                @Override
                public void onMessage(final String msg) {
                    Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            });
            mCoonTool.doLoadUrl("http://192.168.0.118:3333/test/test_page");
        }
        if (v.getId() == R.id.btn_test_disCoon) {
            mCoonTool.doDisCoon();
        }
    }
}
