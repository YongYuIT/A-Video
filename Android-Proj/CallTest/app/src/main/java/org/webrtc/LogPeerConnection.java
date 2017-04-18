package org.webrtc;


import android.util.Log;

/**
 * Created by Yu Yong on 2017/4/14.
 */

public class LogPeerConnection extends PeerConnection {

    private PeerConnection mPeerConnection;

    public LogPeerConnection(long nativePeerConnection, long nativeObserver) {
        super(nativePeerConnection, nativeObserver);
        Log.i("yuyong_PeerConnection", "LogPeerConnection-->" + nativePeerConnection + "-->" + nativeObserver);
    }

    public LogPeerConnection(PeerConnection p) {
        super(-1, -1);
        mPeerConnection = p;
        Log.i("yuyong_PeerConnection", "LogPeerConnection");
    }

    @Override
    public SessionDescription getRemoteDescription() {
        Log.i("yuyong_PeerConnection", "getRemoteDescription");
        return mPeerConnection.getRemoteDescription();
    }

    @Override
    public boolean addIceCandidate(IceCandidate candidate) {
        Log.i("yuyong_PeerConnection", "addIceCandidate");
        return mPeerConnection.addIceCandidate(candidate);
    }

    @Override
    public void setLocalDescription(SdpObserver var1, SessionDescription var2) {
        Log.i("yuyong_PeerConnection", "setLocalDescription");
        mPeerConnection.setLocalDescription(var1, var2);
    }

    @Override
    public boolean addStream(MediaStream stream) {
        Log.i("yuyong_PeerConnection", "addStream");
        return mPeerConnection.addStream(stream);
    }

    @Override
    public void close() {
        Log.i("yuyong_PeerConnection", "close");
        mPeerConnection.close();
    }

    @Override
    public void dispose() {
        Log.i("yuyong_PeerConnection", "dispose");
        mPeerConnection.dispose();
    }

    @Override
    public void createOffer(SdpObserver var1, MediaConstraints var2) {
        Log.i("yuyong_PeerConnection", "createOffer");
        mPeerConnection.createOffer(var1, var2);
    }

    @Override
    public void setRemoteDescription(SdpObserver var1, SessionDescription var2) {
        Log.i("yuyong_PeerConnection", "setRemoteDescription");
        mPeerConnection.setRemoteDescription(var1, var2);
    }

    @Override
    public void createAnswer(SdpObserver var1, MediaConstraints var2) {
        Log.i("yuyong_PeerConnection", "createAnswer");
        mPeerConnection.createAnswer(var1, var2);
    }
}
