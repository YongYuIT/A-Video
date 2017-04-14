package com.thinking.video.webrtc;

import android.util.Log;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Ack;
import com.github.nkzawa.socketio.client.Manager;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONObject;

/**
 * Created by Yu Yong on 2017/4/14.
 */

public class LogSocket extends Socket {

    private Socket mSocket;

    public LogSocket(Socket socket) {
        super(null, null);
        mSocket = socket;
    }

    public LogSocket(Manager io, String nsp) {
        super(io, nsp);
    }

    @Override
    public Emitter emit(String event, Object... args) {
        Log.i("yuyong_socket", "emit-->event:" + event + "-->" + (args[0] instanceof JSONObject ? ((JSONObject) args[0]).toString() : ""));
        return mSocket.emit(event, args);
    }

    @Override
    public Emitter emit(String event, Object[] args, Ack ack) {
        Log.i("yuyong_socket", "emit-->event:" + event);
        return mSocket.emit(event, args, ack);
    }

    @Override
    public Emitter on(String event, Listener fn) {
        Log.i("yuyong_socket", "on-->event:" + event);
        return mSocket.on(event, fn);
    }

    @Override
    public Socket connect() {
        return mSocket.connect();
    }

    @Override
    public Socket disconnect() {
        return mSocket.disconnect();
    }

    @Override
    public Socket close() {
        return mSocket.close();
    }
}
