package com.thinking.ffmpegtest;

/**
 * Created by Yu Yong on 2017/5/15.
 */

public class FFmpegTools {
    static {
        System.loadLibrary("avcodec-57");
        System.loadLibrary("avdevice-57");
        System.loadLibrary("avfilter-6");
        System.loadLibrary("avformat-57");
        System.loadLibrary("avutil-55");
        System.loadLibrary("swresample-2");
        System.loadLibrary("swscale-4");
        System.loadLibrary("com_thinking_ffmpegtest");
    }

    public static native void test();
}
//javah -d G:\WebRtc\20170515001\A-Video\Android-Proj\FFmpegTest\app\jni -classpath G:\WebRtc\20170515001\A-Video\Android-Proj\FFmpegTest\app\build\intermediates\classes\debug com.thinking.ffmpegtest.FFmpegTools

