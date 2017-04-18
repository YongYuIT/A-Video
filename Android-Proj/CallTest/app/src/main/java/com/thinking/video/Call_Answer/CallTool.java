package com.thinking.video.Call_Answer;

import android.os.Message;
import android.util.Log;

import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;

import java.io.File;

/**
 * Created by Yu Yong on 2017/4/18.
 */

public class CallTool extends ConnTool {
    public CallTool(onResutListener listener) {
        super(listener);
    }

    @Override
    protected void start(String _method, Object... _params) {
        mClass = this.getClass();
        super.start(_method, _params);
    }

    private void initLocal(Object... params){
        mCallCoon.createOffer(mSdpObserver, pcConstraints);
        Message msg = new Message();
        msg.obj = new Result("init", "Success", true);
        msg.what = 1001;
        mHandler.sendMessage(msg);
    }

    private void setRemote(Object... params){
        String info = readFileStr(new File(config_remote_file));
        Log.i("yuyong", "readFileStr-->" + info);
        SessionDescription sdp = new SessionDescription(SessionDescription.Type.fromCanonicalForm("answer"), info);
        mCallCoon.setRemoteDescription(mSdpObserver, sdp);
        Message msg = new Message();
        msg.obj = new Result("setRemote", "Success", true);
        msg.what = 1001;
        mHandler.sendMessage(msg);
    }

    private SdpObserver mSdpObserver = new SdpObserver() {
        @Override
        public void onCreateFailure(String s) {
            Log.i("yuyong", "mLocalSdpObserver-->onCreateFailure-->" + s);
        }

        @Override
        public void onCreateSuccess(SessionDescription sessionDescription) {
            Log.i("yuyong", "mLocalSdpObserver-->onCreateSuccess-->\n" + sessionDescription.description);
            mCallCoon.setLocalDescription(this, sessionDescription);
            writeFileTxt(new File(config_local_file), sessionDescription.description);
        }

        @Override
        public void onSetSuccess() {
            Log.i("yuyong", "mLocalSdpObserver-->onSetSuccess");
        }

        @Override
        public void onSetFailure(String s) {
            Log.i("yuyong", "mLocalSdpObserver-->onSetFailure-->" + s);
        }
    };
}
