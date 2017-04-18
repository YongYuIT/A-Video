package com.thinking.video.Abandoned;

import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Environment;
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

/**
 * Created by Yu Yong on 2017/4/14.
 */

public class InitConnTask extends AsyncTask {

    private static final String config_remote_file = Environment.getExternalStorageState() + "/web_rtc_remote.config";
    private static final String config_local_file = Environment.getExternalStorageState() + "/web_rtc_local.config";

    private static final String VIDEO_CODEC_VP9 = "VP9";
    private static final String AUDIO_CODEC_OPUS = "opus";

    private PeerConnectionFactory factory;
    private LinkedList<PeerConnection.IceServer> iceServers = new LinkedList<>();
    private MediaConstraints pcConstraints = new MediaConstraints();
    private PeerConnection mCallCoon;
    private MediaStream localMS;
    private String p_address;
    private onResutListener mListener;

    public InitConnTask(onResutListener listener) {
        mListener = listener;
    }

    public interface onResutListener {
        void onInited(Result result);
    }

    public static class Result {
        public String methodName;
        public boolean result;

        public Result(String m, boolean r) {
            methodName = m;
            result = r;
        }
    }

    @Override
    protected Object doInBackground(Object[] _params) {

        if (((String) _params[0]).equals("init")) {
            return init(_params);
        } else if (((String) _params[0]).equals("conn")) {
            return conn(_params);
        }
        return null;
    }


    private Result init(Object... _params) {

        //获取屏幕分辨率
        Point displaySize = new Point();
        ((WindowManager) _params[1]).getDefaultDisplay().getSize(displaySize);
        //初始化连接参数
        PeerConnectionParameters params = new PeerConnectionParameters(true, false, displaySize.x, displaySize.y, 30, 1, VIDEO_CODEC_VP9, true, 1, AUDIO_CODEC_OPUS, true);
        Log.i("yuyong", "PeerConnectionParameters finish");
        //初始化连接框架
        PeerConnectionFactory.initializeAndroidGlobals(_params[2], true, true, params.videoCodecHwAcceleration, VideoRendererGui.getEGLContext());
        factory = new PeerConnectionFactory();
        Log.i("yuyong", "factory finish");
        //设置穿透服务器
        iceServers.add(new PeerConnection.IceServer("stun:23.21.150.121"));
        iceServers.add(new PeerConnection.IceServer("stun:stun.l.google.com:19302"));
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
        //创建信元
        mCallCoon.createOffer(mSdpObserver, pcConstraints);
        return new Result("init", true);
    }

    private SdpObserver mSdpObserver = new SdpObserver() {
        @Override
        public void onCreateFailure(String s) {

        }

        @Override
        public void onCreateSuccess(SessionDescription sessionDescription) {
            Log.i("yuyong", "SessionDescription-->\n" + sessionDescription.description);
            //写入配置文件
            writeFileTxt(new File(config_local_file), sessionDescription.description);
        }

        @Override
        public void onSetSuccess() {

        }

        @Override
        public void onSetFailure(String s) {

        }
    };


    private Result conn(Object... _params) {
        //连接端IP
        p_address = (String) _params[1];
        Log.i("yuyong", "ip is " + p_address);
        //----------------------------------------
        //读取配置文件
        String info = readFileStr(new File(config_remote_file));
        Log.i("yuyong", "readFileStr-->" + info);
        //----------------------------------------
        return new Result("conn", true);
    }


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

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        Result isSuccess = (Result) o;
        mListener.onInited(isSuccess);
    }

    private VideoCapturer getVideoCapturer() {
        String frontCameraDeviceName = VideoCapturerAndroid.getNameOfFrontFacingDevice();
        return VideoCapturerAndroid.create(frontCameraDeviceName);
    }

    private static boolean writeFileTxt(File file, String str) {
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
            return false;
        }
        try {
            FileWriter fw = new FileWriter(file, true);
            fw.write(str);
            fw.close();
        } catch (Exception e) {
            return false;
        }
        return true;
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
            result += line;
        }
        return result;
    }
}
