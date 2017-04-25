package com.thinking.websockettest;

import android.content.ContextWrapper;
import android.os.Handler;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by Yu Yong on 2017/4/25.
 */

public class CoonTool {

    private static CoonTool thiz;

    private ContextWrapper mContext;
    private WebView mWeb;
    private Listener mListener;
    private Handler mHandler;

    public interface Listener {
        void onMessage(String msg);
    }

    private CoonTool() {
    }

    private CoonTool(ContextWrapper context) {
        mContext = context;
        mHandler = new Handler();
        mWeb = new WebView(mContext);
        WebSettings webSettings = mWeb.getSettings();
        webSettings.setJavaScriptEnabled(true);
        mWeb.setWebChromeClient(this.mWebChromeListener);
        mWeb.setWebViewClient(this.mWebViewListener);
    }

    public static CoonTool getThiz(ContextWrapper context) {
        if (thiz == null)
            thiz = new CoonTool(context);
        return thiz;
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public void doLoadUrl(String url) {
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
            doLoadUrl(url);
            return true;
        }
    };

    public void doDisCoon() {
        mWeb.destroy();
        mContext = null;
        mWeb = null;
        mListener = null;
        mHandler = null;
        thiz = null;
    }

}
