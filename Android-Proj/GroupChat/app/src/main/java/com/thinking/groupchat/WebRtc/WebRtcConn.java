package com.thinking.groupchat.WebRtc;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RendererCommon;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoRendererGui;

import java.util.LinkedList;

/**
 * Created by Yu Yong on 2017/5/5.
 */
public class WebRtcConn {

    public static final RendererCommon.ScalingType scalingType = RendererCommon.ScalingType.SCALE_ASPECT_FILL;
    public static final String VIDEO_CODEC_VP9 = "VP9";
    public static final String AUDIO_CODEC_OPUS = "opus";

    public static MediaStream localMS;//本地媒体
    public static VideoRenderer.Callbacks mLocalRender;
    public static int[] mLocalPosition;

    public static MediaConstraints pcConstraints = new MediaConstraints();//媒体描述
    public static LinkedList<PeerConnection.IceServer> iceServers = new LinkedList<>();//穿透服务
    public static PeerConnectionFactory factory;

    public static Handler mHandler;

    protected String type;
    protected String setState = "";
    protected SdpObserver mSdpObserver;

    public PeerConnection mConn;
    public VideoRenderer.Callbacks mRemoteRender;
    public int[] mPosition;
    public JSONArray mParams;

    public WebRtcConn(PeerConnectionFactory fac, MediaConstraints mc, LinkedList<PeerConnection.IceServer> ice) {
        mConn = fac.createPeerConnection(ice, mc, mListener);
    }


    public void setRemotePosition() {
        if (mRemoteRender == null)
            mRemoteRender = VideoRendererGui.create(mPosition[0], mPosition[1], mPosition[2], mPosition[3], scalingType, false);
    }

    public void setRemoteDescription(SessionDescription sdp) {
        setState = "setRemoteDescription";
        mConn.setRemoteDescription(mSdpObserver, sdp);
    }

    public void setParams(JSONArray jsonArray) {
        Message msg = new Message();
        msg.what = 1001;
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                IceCandidate candidate = new IceCandidate(
                        jsonArray.getJSONObject(i).getString("id"),
                        jsonArray.getJSONObject(i).getInt("label"),
                        jsonArray.getJSONObject(i).getString("candidate"));
                mConn.addIceCandidate(candidate);
            } catch (Exception e) {
                msg.obj = new ConnTool.Result(type + "setParams", "Fail", false);
                mHandler.sendMessage(msg);
                return;
            }
        }
        msg.obj = new ConnTool.Result(type + "setParams", "Success", true);
        mHandler.sendMessage(msg);
        return;
    }

    private PeerConnection.Observer mListener = new PeerConnection.Observer() {
        @Override
        public void onSignalingChange(PeerConnection.SignalingState signalingState) {
            Log.i("yuyong", "onSignalingChange--" + signalingState.name());
        }

        @Override
        public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
            Log.i("yuyong", "onIceConnectionChange--" + iceConnectionState.name());
        }

        @Override
        public void onIceConnectionReceivingChange(boolean b) {
            Log.i("yuyong", "onIceConnectionReceivingChange--" + b);
        }

        @Override
        public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
            Log.i("yuyong", "onIceGatheringChange--" + iceGatheringState.name());
            if (iceGatheringState == PeerConnection.IceGatheringState.COMPLETE && mParams != null) {
                Message msg = new Message();
                msg.obj = new ConnTool.Result(type + "_onIceGatheringChange", mParams.toString(), true);
                msg.what = 1001;
                mHandler.sendMessage(msg);
            }
        }

        @Override
        public void onIceCandidate(IceCandidate iceCandidate) {
            Log.i("yuyong", "onIceCandidate--" + iceCandidate.sdp);
            if (mParams == null) {
                mParams = new JSONArray();
            }
            //追加协商信息
            JSONObject data = null;
            try {
                data = new JSONObject();
                data.put("label", iceCandidate.sdpMLineIndex);
                data.put("id", iceCandidate.sdpMid);
                data.put("candidate", iceCandidate.sdp);
            } catch (Exception e) {

            }
            if (data != null) {
                mParams.put(data);
            }
        }

        @Override
        public void onAddStream(MediaStream mediaStream) {
            Log.i("yuyong", "onAddStream--" + mediaStream.label());
            mediaStream.videoTracks.get(0).addRenderer(new VideoRenderer(WebRtcConn.this.mRemoteRender));
            VideoRendererGui.update(WebRtcConn.this.mRemoteRender, mPosition[0], mPosition[1], mPosition[2], mPosition[3], scalingType, false);
            VideoRendererGui.update(mLocalRender, mLocalPosition[0], mLocalPosition[1], mLocalPosition[2], mLocalPosition[3], scalingType, true);
        }

        @Override
        public void onRemoveStream(MediaStream mediaStream) {
            Log.i("yuyong", "onRemoveStream--" + mediaStream.label());
        }

        @Override
        public void onDataChannel(DataChannel dataChannel) {
            Log.i("yuyong", "onDataChannel--" + dataChannel.label());
        }

        @Override
        public void onRenegotiationNeeded() {
            Log.i("yuyong", "onRenegotiationNeeded");
        }
    };
}
