package com.thinking.video.Tools;

import android.os.Message;
import android.util.Log;

import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;

import java.io.File;

/**
 * Created by Yu Yong on 2017/4/19.
 */

public class AnswerTool extends ConnTool {
    public AnswerTool(onResutListener listener) {
        super(listener);
    }

    @Override
    public void start(String _method, Object... _params) {
        mClass = this.getClass();
        super.start(_method, _params);
    }

    public void answer(Object... params) {
        Log.i("yuyong_rtc", "try createAnswer");
        mCallCoon.createAnswer(mSdpObserver, pcConstraints);
    }

    public void setRemote(Object... params) {
        String info = readFileStr(new File(config_remote_file));
        if (params.length > 0)
            info = (String) params[0];
        Log.i("yuyong", "readFileStr-->" + info);
        SessionDescription sdp = new SessionDescription(SessionDescription.Type.fromCanonicalForm("offer"), info);
        Log.i("yuyong_rtc", "try setRemoteDescription");
        mCallCoon.setRemoteDescription(mSdpObserver, sdp);
    }


    private SdpObserver mSdpObserver = new SdpObserver() {
        @Override
        public void onCreateFailure(String s) {
            Log.i("yuyong_rtc", "try createAnswer fail" + s);
            Message msg = new Message();
            msg.obj = new Result("answer", s, false);
            msg.what = 1001;
            mHandler.sendMessage(msg);
        }

        @Override
        public void onCreateSuccess(SessionDescription sessionDescription) {
            Log.i("yuyong_rtc", "try createAnswer success" + sessionDescription.description);
            Log.i("yuyong_rtc", "try setLocalDescription" + sessionDescription.description);
            mCallCoon.setLocalDescription(new SdpObserver() {
                @Override
                public void onCreateSuccess(SessionDescription sessionDescription) {

                }

                @Override
                public void onSetSuccess() {
                    Log.i("yuyong_rtc", "try setLocalDescription success");
                }

                @Override
                public void onCreateFailure(String s) {

                }

                @Override
                public void onSetFailure(String s) {
                    Log.i("yuyong_rtc", "try setLocalDescription fail--" + s);
                }
            }, sessionDescription);
            Message msg = new Message();
            msg.obj = new Result("answer", sessionDescription.description, true);
            msg.what = 1001;
            mHandler.sendMessage(msg);
            writeFileTxt(new File(config_local_file), sessionDescription.description);
        }

        @Override
        public void onSetSuccess() {
            Log.i("yuyong_rtc", "try setRemoteDescription success");
            Message msg = new Message();
            msg.obj = new Result("setRemote", "Success", true);
            msg.what = 1001;
            mHandler.sendMessage(msg);
        }

        @Override
        public void onSetFailure(String s) {
            Log.i("yuyong_rtc", "try setRemoteDescription fail--" + s);
            Message msg = new Message();
            msg.obj = new Result("setRemote", "Fail", false);
            msg.what = 1001;
            mHandler.sendMessage(msg);
        }
    };
}
