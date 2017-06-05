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
            mCache = ByteBuffer.allocateDirect(mWidth * mHeight * 3 / 2);
        }
    }
}

