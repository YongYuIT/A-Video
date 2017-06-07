package com.thinking.cameralive.new_func;

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

    public static native boolean ffmpegInit(int width, int height, String server_add);

    public static native int ffmpegPush(byte[] datas);
}

//javah -d G:\WebRtc\20170607003\A-Video\Android-Proj\CameraLive\app\jni -classpath G:\WebRtc\20170607003\A-Video\Android-Proj\CameraLive\app\build\intermediates\classes\debug com.thinking.cameralive.new_func.FFmpeg
