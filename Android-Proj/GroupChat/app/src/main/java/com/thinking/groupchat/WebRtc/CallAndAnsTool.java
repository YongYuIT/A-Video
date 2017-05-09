package com.thinking.groupchat.WebRtc;

import android.util.Log;

import org.json.JSONArray;
import org.webrtc.SessionDescription;

/**
 * Created by Yu Yong on 2017/5/5.
 */

public class CallAndAnsTool extends ConnTool {
    public CallAndAnsTool(onResutListener listener) {
        super(listener);
    }

    @Override
    public void start(String _method, Object... _params) {
        mClass = this.getClass();
        super.start(_method, _params);
    }

    public void createOffer(Object... params) {
        if (mConns.size() == 2) {
            //假设两路视频，三方聊天
            ((WebRtcCallConn) mConns.get(0)).createOffer();
        }
    }

    public void receiveAnswer(Object... params) {
        String info = (String) params[0];
        SessionDescription sdp = new SessionDescription(SessionDescription.Type.fromCanonicalForm("answer"), info);
        ((WebRtcCallConn) mConns.get(0)).setRemoteDescription(sdp);
    }

    public void receiveAnswerParams(Object... params) {
        String info = (String) params[0];
        JSONArray arrayObj = null;
        try {
            arrayObj = new JSONArray(info);
        } catch (Exception e) {
            Log.i("yuyong", "params set fail(receiveAnswerParams)-->" + e.getMessage());
            return;
        }
        mConns.get(0).setParams(arrayObj);
    }

    public void receiveConn(Object... params) {
        String info = (String) params[0];
        SessionDescription sdp = new SessionDescription(SessionDescription.Type.fromCanonicalForm("offer"), info);
        ((WebRtcAnswerConn) mConns.get(1)).setRemoteDescription(sdp);
    }

    public void createAnswer(Object... params) {
        ((WebRtcAnswerConn) mConns.get(1)).createAnswer();
    }

    public void receiveParams(Object... params) {
        String info = (String) params[0];
        JSONArray arrayObj = null;
        try {
            arrayObj = new JSONArray(info);
        } catch (Exception e) {
            Log.i("yuyong", "params set fail(receiveParams)-->" + e.getMessage());
            return;
        }
        mConns.get(1).setParams(arrayObj);
    }


}
