package com.thinking.video.Call_Answer;

import android.graphics.Point;
import android.hardware.Camera;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.WindowManager;

import org.json.JSONObject;
import org.webrtc.AudioSource;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RendererCommon;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoCapturerAndroid;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoRendererGui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Yu Yong on 2017/4/18.
 */

public class ConnTool {
    protected static final String config_remote_file = Environment.getExternalStorageDirectory().toString() + "/web_rtc_remote.config";
    protected static final String config_local_file = Environment.getExternalStorageDirectory().toString() + "/web_rtc_local.config";
    protected static final String config_local_params_file = Environment.getExternalStorageDirectory().toString() + "/web_rtc_local_params.config";
    protected static final String config_remote_params_file = Environment.getExternalStorageDirectory().toString() + "/web_rtc_remote_params.config";

    private static final ExecutorService pool = Executors.newSingleThreadExecutor();

    private static final String VIDEO_CODEC_VP9 = "VP9";
    private static final String AUDIO_CODEC_OPUS = "opus";

    private static final int LOCAL_X_CONNECTING = 0;
    private static final int LOCAL_Y_CONNECTING = 0;
    private static final int LOCAL_WIDTH_CONNECTING = 100;
    private static final int LOCAL_HEIGHT_CONNECTING = 100;
    private static final RendererCommon.ScalingType scalingType = RendererCommon.ScalingType.SCALE_ASPECT_FILL;

    private static final int REMOTE_X = 0;
    private static final int REMOTE_Y = 0;
    private static final int REMOTE_WIDTH = 100;
    private static final int REMOTE_HEIGHT = 100;

    private static final int LOCAL_X_CONNECTED = 72;
    private static final int LOCAL_Y_CONNECTED = 72;
    private static final int LOCAL_WIDTH_CONNECTED = 25;
    private static final int LOCAL_HEIGHT_CONNECTED = 25;

    private PeerConnectionFactory factory;
    private LinkedList<PeerConnection.IceServer> iceServers = new LinkedList<>();
    protected MediaConstraints pcConstraints = new MediaConstraints();
    protected PeerConnection mCallCoon;
    private MediaStream localMS;
    private VideoRenderer.Callbacks localRender;
    private VideoRenderer.Callbacks remoteRender;

    protected Class mClass;
    private String method;
    private Object[] params;

    protected Handler mHandler;
    private onResutListener mListener;

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

    public ConnTool(onResutListener listener) {
        mHandler = new Handler() {
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
        mListener = listener;
    }

    protected void start(String _method, Object... _params) {
        method = _method;
        params = _params;
        pool.submit(new Runnable() {
            @Override
            public void run() {
                Class ConnTool_class = mClass;
                try {
                    if (1 == 1/*for test*/) {
                        Method[] tmps = ConnTool_class.getMethods();
                        int i = tmps.length;
                    }
                    Method some_method = ConnTool_class.getMethod(method, Object[].class);
                    some_method.invoke(ConnTool.this, new Object[]{params});

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }

    public void serParams(Object... _params) {
        String info = readFileStr(new File(config_remote_params_file));
        String infos[] = info.split("########");
        Message msg = new Message();
        msg.what = 1001;
        for (int i = 0; i < infos.length; i++) {
            if (infos[i].equals("") || infos[i].equals("\n"))
                continue;
            Log.i("yuyong", "serParams-->" + infos[i]);
            try {
                JSONObject params = new JSONObject(infos[i]);
                IceCandidate candidate = new IceCandidate(
                        params.getString("id"),
                        params.getInt("label"),
                        params.getString("candidate")
                );
                mCallCoon.addIceCandidate(candidate);
                msg.obj = new Result("answer-->serParams", "Success", true);
            } catch (Exception e) {
                e.printStackTrace();
                msg.obj = new Result("answer-->serParams", "Fail", true);
                break;
            }
        }
        mHandler.sendMessage(msg);

    }

    public void init(Object... _params) {
        //清空缓存信息
        deleteCache();
        //获取屏幕分辨率
        Point displaySize = new Point();
        ((WindowManager) _params[0]).getDefaultDisplay().getSize(displaySize);
        //初始化连接参数
        PeerConnectionParameters params = new PeerConnectionParameters(true, false, displaySize.x, displaySize.y, 30, 1, VIDEO_CODEC_VP9, true, 1, AUDIO_CODEC_OPUS, true);
        //初始化连接框架
        PeerConnectionFactory.initializeAndroidGlobals(_params[1], true, true, params.videoCodecHwAcceleration);
        factory = new PeerConnectionFactory();
        //设置穿透服务器（STUN服务器）
        //iceServers.add(new PeerConnection.IceServer("stun:23.21.150.121"));
        //iceServers.add(new PeerConnection.IceServer("stun:stun.l.google.com:19302"));
        iceServers.add(new PeerConnection.IceServer("stun:stun.counterpath.com"));
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
        //输出
        if (remoteRender == null) {
            remoteRender = VideoRendererGui.create(REMOTE_X, REMOTE_Y, REMOTE_WIDTH, REMOTE_HEIGHT, scalingType, false);
        }
        if (localRender == null) {
            localRender = VideoRendererGui.create(LOCAL_X_CONNECTING, LOCAL_Y_CONNECTING, LOCAL_WIDTH_CONNECTING, LOCAL_HEIGHT_CONNECTING, scalingType, true);
        }
        Log.i("yuyong_out_put", "localRender addRenderer");
        localMS.videoTracks.get(0).addRenderer(new VideoRenderer(localRender));
        VideoRendererGui.update(localRender, LOCAL_X_CONNECTING, LOCAL_Y_CONNECTING, LOCAL_WIDTH_CONNECTING, LOCAL_HEIGHT_CONNECTING, scalingType, true);

        Message msg = new Message();
        msg.obj = new Result("init", "", true);
        msg.what = 1001;
        mHandler.sendMessage(msg);
    }

    private PeerConnection.Observer mPCObserver = new PeerConnection.Observer() {
        @Override
        public void onIceConnectionReceivingChange(boolean b) {

        }

        @Override
        public void onAddStream(MediaStream mediaStream) {
            Log.i("yuyong", "onAddStream-->" + mediaStream.label());
            Log.i("yuyong_out_put", "remoteRender addRenderer");
            mediaStream.videoTracks.get(0).addRenderer(new VideoRenderer(remoteRender));
            VideoRendererGui.update(remoteRender, REMOTE_X, REMOTE_Y, REMOTE_WIDTH, REMOTE_HEIGHT, scalingType, false);
            VideoRendererGui.update(localRender, LOCAL_X_CONNECTED, LOCAL_Y_CONNECTED, LOCAL_WIDTH_CONNECTED, LOCAL_HEIGHT_CONNECTED, scalingType, true);
        }

        @Override
        public void onSignalingChange(PeerConnection.SignalingState signalingState) {
            Log.i("yuyong", "onSignalingChange-->" + signalingState.name());
        }

        @Override
        public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
            Log.i("yuyong", "onIceConnectionChange-->" + iceConnectionState.name());
        }

        @Override
        public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
            Log.i("yuyong", "onIceGatheringChange-->" + iceGatheringState.name());
        }

        @Override
        public void onIceCandidate(IceCandidate iceCandidate) {
            Log.i("yuyong", "onIceCandidate-->" + iceCandidate.sdp);
            //追加填写协商信息
            String info = "";
            try {
                JSONObject data = new JSONObject();
                data.put("label", iceCandidate.sdpMLineIndex);
                data.put("id", iceCandidate.sdpMid);
                data.put("candidate", iceCandidate.sdp);
                info = data.toString();
            } catch (Exception e) {
                Log.i("yuyong", "onIceCandidate-->recoed error-->" + e.getMessage());
            }
            writeFileTxtAdd(new File(config_local_params_file), info + "########");
        }

        @Override
        public void onRemoveStream(MediaStream mediaStream) {
            Log.i("yuyong", "onRemoveStream-->" + mediaStream.label());
        }

        @Override
        public void onDataChannel(DataChannel dataChannel) {
            Log.i("yuyong", "onDataChannel-->" + dataChannel.label());
        }

        @Override
        public void onRenegotiationNeeded() {
            Log.i("yuyong", "onRenegotiationNeeded");
        }
    };

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

    private static void deleteCache() {
        File[] files = new File[]{new File(config_remote_file), new File(config_remote_params_file), new File(config_local_params_file), new File(config_local_file)};
        for (int i = 0; i < files.length; i++) {
            if (files[i].exists()) {
                String file_tmp_name = files[i].getAbsolutePath() + ".tmp";
                File file_tmp = new File(file_tmp_name);
                files[i].renameTo(file_tmp);
                file_tmp.delete();
            }
        }
    }

    private static String writeFileTxtAdd(File file, String str) {
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {
                return "CREATE_FAIL";
            }
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

    protected static String writeFileTxt(File file, String str) {
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

    protected static String readFileStr(File file) {
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
