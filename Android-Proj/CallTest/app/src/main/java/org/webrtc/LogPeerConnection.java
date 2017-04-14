package org.webrtc;


import org.webrtc.PeerConnection;

/**
 * Created by Yu Yong on 2017/4/14.
 */

public class LogPeerConnection extends PeerConnection {

    private PeerConnection mPeerConnection;

    public LogPeerConnection(long nativePeerConnection, long nativeObserver) {
        super(nativePeerConnection, nativeObserver);
    }

    public LogPeerConnection(PeerConnection p) {
        super(-1, -1);
        mPeerConnection = p;
    }

    @Override
    public SessionDescription getRemoteDescription() {
        return mPeerConnection.getRemoteDescription();
    }

    @Override
    public boolean addIceCandidate(IceCandidate candidate) {
        return mPeerConnection.addIceCandidate(candidate);
    }

    @Override
    public void setLocalDescription(SdpObserver var1, SessionDescription var2) {
        mPeerConnection.setLocalDescription(var1, var2);
    }

    @Override
    public boolean addStream(MediaStream stream) {
        return mPeerConnection.addStream(stream);
    }

    @Override
    public void close() {
        mPeerConnection.close();
    }

    @Override
    public void dispose() {
        mPeerConnection.dispose();
    }

    @Override
    public void createOffer(SdpObserver var1, MediaConstraints var2) {
        mPeerConnection.createOffer(var1, var2);
    }

    @Override
    public void setRemoteDescription(SdpObserver var1, SessionDescription var2) {
        mPeerConnection.setRemoteDescription(var1, var2);
    }

    @Override
    public void createAnswer(SdpObserver var1, MediaConstraints var2) {
        mPeerConnection.createAnswer(var1, var2);
    }
}
