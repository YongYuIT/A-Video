package com.thinking.groupchat.WebRtc;

import android.os.Message;
import android.util.Log;

import org.webrtc.MediaConstraints;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;

import java.util.LinkedList;

/**
 * Created by Yu Yong on 2017/5/8.
 */

public class WebRtcCallConn extends WebRtcConn {

    public WebRtcCallConn(PeerConnectionFactory fac, MediaConstraints mc, LinkedList<PeerConnection.IceServer> ice) {
        super(fac, mc, ice);
        type = "call";
        mSdpObserver = new SdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                Log.i("yuyong", "onCreateSuccess--" + sessionDescription.description);
                setState = "setLocalDescription";
                mConn.setLocalDescription(this, sessionDescription);
                Message msg = new Message();
                msg.obj = new ConnTool.Result("createOffer", sessionDescription.description, true);
                msg.what = 1001;
                mHandler.sendMessage(msg);
            }

            @Override
            public void onSetSuccess() {
                Log.i("yuyong", "onSetSuccess");
                if (setState.equalsIgnoreCase("setRemoteDescription")) {
                    Message msg = new Message();
                    msg.obj = new ConnTool.Result("call_setRemoteDescription", "", true);
                    msg.what = 1001;
                    mHandler.sendMessage(msg);
                }
            }

            @Override
            public void onCreateFailure(String s) {
                Log.i("yuyong", "onCreateFailure" + s);
            }

            @Override
            public void onSetFailure(String s) {
                Log.i("yuyong", "onSetFailure" + s);
            }
        };
    }

    public void createOffer() {
        mConn.createOffer(mSdpObserver, pcConstraints);
    }
}
