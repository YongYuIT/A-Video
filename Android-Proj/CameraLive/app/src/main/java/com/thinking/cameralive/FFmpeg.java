package com.thinking.cameralive;

/**
 * Created by Yu Yong on 2017/6/7.
 */

public class FFmpeg {

    static {
        System.loadLibrary("avcodec-57");
        System.loadLibrary("avdevice-57");
        System.loadLibrary("avfilter-6");
        System.loadLibrary("avformat-57");
        System.loadLibrary("avutil-55");
        System.loadLibrary("swresample-2");
        System.loadLibrary("swscale-4");
        System.loadLibrary("x264-150");
        System.loadLibrary("postproc-54");
        System.loadLibrary("com_thinking_ffmpegtest");
    }

    public static native int streamerInit(int width, int height);

    public static native int streamerHandle(byte[] data);

    public static native int streamerFlush();

    public static native int streamerRelease();
}

//javah -d G:\WebRtc\20170607001\A-Video\Android-Proj\CameraLive\app\jni -classpath G:\WebRtc\20170607001\A-Video\Android-Proj\CameraLive\app\build\intermediates\classes\debug com.thinking.cameralive.FFmpeg