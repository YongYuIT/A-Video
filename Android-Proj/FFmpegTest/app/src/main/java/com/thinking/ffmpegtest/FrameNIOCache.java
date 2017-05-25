package com.thinking.ffmpegtest;

import java.nio.ByteBuffer;

/**
 * Created by Yu Yong on 2017/5/25.
 */

public class FrameNIOCache {

    public static ByteBuffer mCache;
    public static int mWidth;
    public static int mHeight;

    public static void setCache(int width, int height) {
        if (mCache == null) {
            mWidth = width;
            mHeight = height;
            mCache = ByteBuffer.allocateDirect(mWidth * mHeight * 4);
        }
    }

    public static int getCacheSize() {
        if (mCache == null)
            return -1;
        return mWidth * mHeight * 4;
    }
}
/*
警告: 二进制文件FrameNIOCache包含com.thinking.ffmpegtest.FrameNIOCache
Compiled from "FrameNIOCache.java"
public class com.thinking.ffmpegtest.FrameNIOCache {
  public static java.nio.ByteBuffer mCache;
    descriptor: Ljava/nio/ByteBuffer;
  public static int mWidth;
    descriptor: I
  public static int mHeight;
    descriptor: I
  public com.thinking.ffmpegtest.FrameNIOCache();
    descriptor: ()V

  public static void setCache(int, int);
    descriptor: (II)V

  public static int getCacheSize();
    descriptor: ()I
}
 */
