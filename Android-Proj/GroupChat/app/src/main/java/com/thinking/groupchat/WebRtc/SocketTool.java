package com.thinking.groupchat.WebRtc;

import android.content.ContextWrapper;
import android.os.Handler;
import android.util.Log;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by Yu Yong on 2017/4/25.
 */

public class SocketTool {
    private static SocketTool thiz;

    private ContextWrapper mContext;
    private WebView mWeb;
    private Listener mListener;
    private Handler mHandler;

    public interface Listener {
        void onMessage(String msg);
    }

    private SocketTool() {
    }

    private SocketTool(ContextWrapper context) {
        mContext = context;
        mHandler = new Handler();
        mWeb = new WebView(mContext);
        WebSettings webSettings = mWeb.getSettings();
        webSettings.setJavaScriptEnabled(true);
        mWeb.setWebChromeClient(this.mWebChromeListener);
        mWeb.setWebViewClient(this.mWebViewListener);
    }

    public static SocketTool getThiz(ContextWrapper context) {
        if (thiz == null)
            thiz = new SocketTool(context);
        return thiz;
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public void doConn(String url) {
        mWeb.loadUrl(url);
    }

    private WebChromeClient mWebChromeListener = new WebChromeClient() {
        //处理js的Alert函数
        @Override
        public synchronized boolean onJsAlert(WebView view, String url, final String message, final JsResult result) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mListener.onMessage(message);
                }
            });
            //相当于点击警告框的确定
            result.confirm();
            return true;
        }
    };

    private WebViewClient mWebViewListener = new WebViewClient() {
        //拦截跳转请求
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            doConn(url);
            return true;
        }
    };

    public void doDisConn() {
        mWeb.destroy();
        mContext = null;
        mWeb = null;
        mListener = null;
        mHandler = null;
        thiz = null;
    }


    public void sendMsg(String msg) {
        final String send_msg = String.format("App.test.do_test(\"%s\")", msg);
        mWeb.post(new Runnable() {
            @Override
            public void run() {
                Log.i("yuyong", "JS-->" + send_msg);
                mWeb.evaluateJavascript(send_msg, new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        Log.i("yuyong", "JS-->" + send_msg + " --> " + value);
                    }
                });
            }
        });
    }

}

