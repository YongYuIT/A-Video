package com.thinking.video.Abandoned.calltest;

import android.graphics.Point;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.WindowManager;

import com.thinking.video.webrtc.PeerConnectionParameters;

import org.webrtc.AudioSource;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoCapturerAndroid;
import org.webrtc.VideoRendererGui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Yu Yong on 2017/4/17.
 */

public class CoonTools {


    private static final String config_remote_file = Environment.getExternalStorageDirectory().toString() + "/web_rtc_remote.config";
    private static final String config_local_file = Environment.getExternalStorageDirectory().toString() + "/web_rtc_local.config";

    private static final ExecutorService pool = Executors.newSingleThreadExecutor();

    private static final String VIDEO_CODEC_VP9 = "VP9";
    private static final String AUDIO_CODEC_OPUS = "opus";

    private String method;
    private Object[] params;
    private boolean isLocalSetted = false;

    private PeerConnectionFactory factory;
    private LinkedList<PeerConnection.IceServer> iceServers = new LinkedList<>();
    private MediaConstraints pcConstraints = new MediaConstraints();
    private PeerConnection mCallCoon;
    private MediaStream localMS;
    private String p_address;
    private onResutListener mListener;
    private Handler mHandler;

    public CoonTools(onResutListener listener) {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 1001:
                        mListener.onFinished((Result) msg.obj);
                        break;
                    case 1002:
                        mListener.onFinished((Result) msg.obj);
                        break;
                    case 1003:
                        mListener.onFinished((Result) msg.obj);
                        break;
                    case 1004:
                        mListener.onFinished((Result) msg.obj);
                        break;
                }
            }
        };
        mListener = listener;
    }

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

    public void start(String _method, Object... _params) {
        method = _method;
        params = _params;
        pool.submit(new Runnable() {
            @Override
            public void run() {
                if (method.equals("init")) {
                    init(params);
                } else if (method.equals("add_remote")) {
                    add_remote(params);
                } else if (method.equals("answer")) {
                    answer(params);
                } else if (method.equals("create_local")) {
                    create_local();
                }
            }
        });
    }

    private void init(Object... _params) {

        //获取屏幕分辨率
        Point displaySize = new Point();
        ((WindowManager) _params[0]).getDefaultDisplay().getSize(displaySize);
        //初始化连接参数
        PeerConnectionParameters params = new PeerConnectionParameters(true, false, displaySize.x, displaySize.y, 30, 1, VIDEO_CODEC_VP9, true, 1, AUDIO_CODEC_OPUS, true);
        Log.i("yuyong", "PeerConnectionParameters finish");
        //初始化连接框架
        PeerConnectionFactory.initializeAndroidGlobals(_params[1], true, true, params.videoCodecHwAcceleration, VideoRendererGui.getEGLContext());
        factory = new PeerConnectionFactory();
        Log.i("yuyong", "factory finish");
        //设置穿透服务器
        iceServers.add(new PeerConnection.IceServer("stun:23.21.150.121"));
        iceServers.add(new PeerConnection.IceServer("stun:stun.l.google.com:19302"));
        //媒体描述
        pcConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        pcConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
        pcConstraints.optional.add(new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"));
        //获取连接对象
        mCallCoon = factory.createPeerConnection(iceServers, pcConstraints, mPCObserver);
        //创建连接对象的本地媒体数据流
        localMS = factory.createLocalMediaStream("ARDAMS");
        //初始化媒体数据流
        MediaConstraints videoConstraints = new MediaConstraints();
        videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxHeight", Integer.toString(params.videoHeight)));
        videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxWidth", Integer.toString(params.videoWidth)));
        videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxFrameRate", Integer.toString(params.videoFps)));
        videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("minFrameRate", Integer.toString(params.videoFps)));
        localMS.addTrack(factory.createVideoTrack("ARDAMSv0", factory.createVideoSource(getVideoCapturer(), videoConstraints)));
        AudioSource audioSource = factory.createAudioSource(new MediaConstraints());
        localMS.addTrack(factory.createAudioTrack("ARDAMSa0", audioSource));
        //将本地媒体数据流添加到连接对象
        mCallCoon.addStream(localMS);

        Message msg = new Message();
        msg.obj = new Result("init", "", true);
        msg.what = 1001;
        mHandler.sendMessage(msg);
    }

    private void create_local() {
        //创建信元
        mCallCoon.createOffer(mSdpObserver, pcConstraints);

        Message msg = new Message();
        msg.obj = new Result("create_local", "", true);
        msg.what = 1002;
        mHandler.sendMessage(msg);
    }


    private void add_remote(Object... _params) {
        //读取配置文件
        String info = readFileStr(new File(config_remote_file));
        Log.i("yuyong", "readFileStr-->" + info);
        SessionDescription sdp = null;
        if (isLocalSetted) {
            sdp = new SessionDescription(SessionDescription.Type.fromCanonicalForm("answer"), info);
        } else {
            sdp = new SessionDescription(SessionDescription.Type.fromCanonicalForm("offer"), info);
        }
        mCallCoon.setRemoteDescription(mSdpObserver, sdp);
        //----------------------------------------
        Message msg = new Message();
        msg.obj = new Result("add_remote", "", true);
        msg.what = 1003;
        mHandler.sendMessage(msg);
    }

    private void answer(Object... _params) {
        mCallCoon.createAnswer(mSdpObserver, pcConstraints);

        Message msg = new Message();
        msg.obj = new Result("answer", "", true);
        msg.what = 1004;
        mHandler.sendMessage(msg);
    }


    private SdpObserver mSdpObserver = new SdpObserver() {
        @Override
        public void onCreateFailure(String s) {
            Log.i("yuyong", "onCreateFailure-->" + s);
        }

        @Override
        public void onCreateSuccess(SessionDescription sessionDescription) {
            Log.i("yuyong", "SessionDescription-->\n" + sessionDescription.description);
            if (!isLocalSetted) {
                mCallCoon.setLocalDescription(mSdpObserver, sessionDescription);
                isLocalSetted = true;
            }
            //写入配置文件
            writeFileTxt(new File(config_local_file), sessionDescription.description);
        }

        @Override
        public void onSetSuccess() {
            Log.i("yuyong", "onSetSuccess");
        }

        @Override
        public void onSetFailure(String s) {
            Log.i("yuyong", "onSetFailure-->" + s);
        }
    };

    private PeerConnection.Observer mPCObserver = new PeerConnection.Observer() {
        @Override
        public void onAddStream(MediaStream mediaStream) {

        }

        @Override
        public void onSignalingChange(PeerConnection.SignalingState signalingState) {

        }

        @Override
        public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {

        }

        @Override
        public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {

        }

        @Override
        public void onIceCandidate(IceCandidate iceCandidate) {

        }

        @Override
        public void onRemoveStream(MediaStream mediaStream) {

        }

        @Override
        public void onDataChannel(DataChannel dataChannel) {

        }

        @Override
        public void onRenegotiationNeeded() {

        }
    };

    private VideoCapturer getVideoCapturer() {
        String frontCameraDeviceName = VideoCapturerAndroid.getNameOfFrontFacingDevice();
        return VideoCapturerAndroid.create(frontCameraDeviceName);
    }

    private static String writeFileTxt(File file, String str) {
        Log.i("yuyong", "writeFileTxt-->" + file.getAbsolutePath());
        if (file.exists()) {
            String file_tmp_name = file.getAbsolutePath() + ".tmp";
            File file_tmp = new File(file_tmp_name);
            file.renameTo(file_tmp);
            file_tmp.delete();
        }
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        try {
            file.createNewFile();
        } catch (Exception e) {
            return "CREATE_FAIL";
        }
        try {
            FileWriter fw = new FileWriter(file, true);
            fw.write(str);
            fw.close();
        } catch (Exception e) {
            return "WRITE_ERROR";
        }
        return "SUCCESS";
    }

    private static String readFileStr(File file) {
        String result = "";
        if (!file.exists()) {
            return "NO_FILE";
        }
        FileInputStream f_in = null;
        try {
            f_in = new FileInputStream(file);
        } catch (Exception e) {
            return "IO_ERROR";
        }
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(f_in, "UTF-8"));
        } catch (Exception e) {
            return "READ_TYPE_ERROR";
        }
        while (true) {
            String line = "";
            try {
                line = reader.readLine();
            } catch (Exception e) {
                return "READ_TYPE_ERROR";
            }
            if (line == null)
                break;
            result += line + "\n";
        }
        return result;
    }

}
