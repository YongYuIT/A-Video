package com.thinking.groupchat.WebRtc;

import android.graphics.Point;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.WindowManager;

import org.webrtc.AudioSource;
import org.webrtc.MediaConstraints;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoCapturerAndroid;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoRendererGui;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Yu Yong on 2017/5/5.
 */

public class ConnTool {
    //------------------------------------------------------------------------------------
    private static final ExecutorService pool = Executors.newSingleThreadExecutor();
    protected List<WebRtcConn> mConns;//连接对象集合
    //------------------------------------------------------------------------------------
    //反射相关
    protected Class mClass;
    private String method;
    private Object[] params;
    //------------------------------------------------------------------------------------
    private onResutListener mListener;

    //====================================================================================
    public static class Result {
        public String methodName;
        public boolean result;
        public String disc;

        public Result(String m, String d, boolean r) {
            methodName = m;
            disc = d;
            result = r;
        }
    }

    public interface onResutListener {
        void onFinished(Result result);
    }

    //====================================================================================
    public ConnTool(onResutListener listener) {
        mConns = new ArrayList<>();
        mListener = listener;
        WebRtcConn.mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 1001:
                        mListener.onFinished((Result) msg.obj);
                        break;
                }
            }
        };
    }


    public void start(String _method, Object... _params) {
        method = _method;
        params = _params;
        pool.submit(new Runnable() {
            @Override
            public void run() {
                if (mClass == null)
                    mClass = ConnTool.class;
                try {
                    Method some_method = mClass.getMethod(method, Object[].class);
                    some_method.invoke(ConnTool.this, new Object[]{params});
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }

    //第一个参数指明有多少个连接需要创建
    public void init(Object... _params) {
        Log.i("yuyong", String.format("init--%s--%s--%s", params[0], params[1], params[2]));
        int mCurrentCoonNum = (int) _params[0];
        //获取屏幕分辨率
        Point displaySize = new Point();
        ((WindowManager) _params[1]).getDefaultDisplay().getSize(displaySize);
        //初始化连接参数
        ConnParams params = new ConnParams(true, false, displaySize.x, displaySize.y, 30, 1, WebRtcConn.VIDEO_CODEC_VP9, true, 1, WebRtcConn.AUDIO_CODEC_OPUS, true);
        //初始化连接框架
        PeerConnectionFactory.initializeAndroidGlobals(_params[2], true, true, params.videoCodecHwAcceleration);
        WebRtcConn.factory = new PeerConnectionFactory();
        //设置穿透服务器（STUN服务器）
        WebRtcConn.iceServers.add(new PeerConnection.IceServer("stun:stun.counterpath.com"));
        //媒体描述
        WebRtcConn.pcConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        WebRtcConn.pcConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
        WebRtcConn.pcConstraints.optional.add(new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"));
        //初始化连接对象
        if (mCurrentCoonNum == 2) {
            //如果是两路连接，即三方视频
            mConns.add(new WebRtcCallConn(WebRtcConn.factory, WebRtcConn.pcConstraints, WebRtcConn.iceServers));
            mConns.add(new WebRtcAnswerConn(WebRtcConn.factory, WebRtcConn.pcConstraints, WebRtcConn.iceServers));
        }
        //创建本地媒体数据流
        WebRtcConn.localMS = WebRtcConn.factory.createLocalMediaStream("ARDAMS");
        //初始化媒体数据流
        MediaConstraints videoConstraints = new MediaConstraints();
        videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxHeight", Integer.toString(params.videoHeight)));
        videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxWidth", Integer.toString(params.videoWidth)));
        videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxFrameRate", Integer.toString(params.videoFps)));
        videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("minFrameRate", Integer.toString(params.videoFps)));
        WebRtcConn.localMS.addTrack(WebRtcConn.factory.createVideoTrack("ARDAMSv0", WebRtcConn.factory.createVideoSource(getVideoCapturer(), videoConstraints)));
        AudioSource audioSource = WebRtcConn.factory.createAudioSource(new MediaConstraints());
        WebRtcConn.localMS.addTrack(WebRtcConn.factory.createAudioTrack("ARDAMSa0", audioSource));
        for (WebRtcConn conn : mConns) {
            conn.mConn.addStream(WebRtcConn.localMS);
        }
        //设置输出位置
        setPosition();

        Message msg = new Message();
        msg.obj = new Result("init", "Success", true);
        msg.what = 1001;
        WebRtcConn.mHandler.sendMessage(msg);
    }

    private void setPosition() {
        if (WebRtcConn.mLocalRender == null) {
            WebRtcConn.mLocalPosition = new int[]{0, 0, 50, 50};
            WebRtcConn.mLocalRender = VideoRendererGui.create(WebRtcConn.mLocalPosition[0], WebRtcConn.mLocalPosition[1], WebRtcConn.mLocalPosition[2], WebRtcConn.mLocalPosition[3], WebRtcConn.scalingType, true);
        }
        WebRtcConn.localMS.videoTracks.get(0).addRenderer(new VideoRenderer(WebRtcConn.mLocalRender));
        VideoRendererGui.update(WebRtcConn.mLocalRender, WebRtcConn.mLocalPosition[0], WebRtcConn.mLocalPosition[1], WebRtcConn.mLocalPosition[2], WebRtcConn.mLocalPosition[3], WebRtcConn.scalingType, true);
        if (mConns.size() == 2) {
            //假设两路连接，三方视频
            mConns.get(0).mPosition = new int[]{0, 50, 50, 50};
            mConns.get(0).setRemotePosition();
            mConns.get(1).mPosition = new int[]{50, 0, 50, 50};
            mConns.get(1).setRemotePosition();
        }
    }

    private static VideoCapturer getVideoCapturer() {
        String frontCameraDeviceName = getNameOfFrontFacingDevice();
        return VideoCapturerAndroid.create(frontCameraDeviceName);
    }

    public static String getNameOfFrontFacingDevice() {
        for (int i = 0; i < Camera.getNumberOfCameras(); ++i) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == 1) {
                return getDeviceName(i);
            }
        }

        return null;
    }

    public static String getDeviceName(int index) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(index, info);
        String facing = info.facing == 1 ? "front" : "back";
        return "Camera " + index + ", Facing " + facing + ", Orientation " + info.orientation;
    }
}
