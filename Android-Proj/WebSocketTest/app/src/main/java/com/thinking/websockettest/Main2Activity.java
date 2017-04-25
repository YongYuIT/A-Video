package com.thinking.websockettest;

import android.annotation.TargetApi;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.Toast;

public class Main2Activity extends AppCompatActivity {

    WebView web_view_main;
    EditText edt_message;
    Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        web_view_main = new WebView(this);
        edt_message = (EditText) findViewById(R.id.edt_message);
        mHandler = new Handler();
        WebSettings web_settings = web_view_main.getSettings();
        web_settings.setJavaScriptEnabled(true);
        WebViewClient web_client = new WebViewClient() {
            //拦截跳转请求
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                gotoUrl(url);
                return true;
            }
        };

        WebChromeClient web_chrome_client = new WebChromeClient() {
            //处理js的Alert函数
            @Override
            public boolean onJsAlert(WebView view, String url, final String message, final JsResult result) {
                Log.i("yuyong", "alert -> " + message);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(Main2Activity.this, message, Toast.LENGTH_SHORT).show();
                    }
                });
                //相当于点击警告框的确定
                result.confirm();
                return true;
            }
        };

        web_view_main.setWebViewClient(web_client);
        web_view_main.setWebChromeClient(web_chrome_client);
        gotoUrl("http://192.168.0.118:3333/test/test_page");
    }

    private void gotoUrl(String url) {
        Log.i("yuyong", "going to --> " + url);
        web_view_main.loadUrl(url);
    }

    @TargetApi(19)
    private void doJS(final String method) {
        Log.i("yuyong", "doJS --> " + method);
        web_view_main.post(new Runnable() {
            @Override
            public void run() {
                web_view_main.evaluateJavascript(method, new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        Log.i("yuyong", "JS:" + method + " --> " + value);
                    }
                });
            }
        });
    }

    public void onClick(View v) {
        if (v.getId() == R.id.btn_send) {
            doJS("App.test.do_test(\"" + edt_message.getText().toString() + "\")");
            edt_message.setText("");
        }
    }
}
