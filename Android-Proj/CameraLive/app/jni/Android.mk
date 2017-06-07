LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := avcodec
LOCAL_SRC_FILES := G:\WebRtc\FFmpeg-SDK\arm\lib\libavcodec-57.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := avdevice
LOCAL_SRC_FILES := G:\WebRtc\FFmpeg-SDK\arm\lib\libavdevice-57.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := avfilter
LOCAL_SRC_FILES := G:\WebRtc\FFmpeg-SDK\arm\lib\libavfilter-6.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := avformat
LOCAL_SRC_FILES := G:\WebRtc\FFmpeg-SDK\arm\lib\libavformat-57.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := avutil
LOCAL_SRC_FILES := G:\WebRtc\FFmpeg-SDK\arm\lib\libavutil-55.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := swresample
LOCAL_SRC_FILES := G:\WebRtc\FFmpeg-SDK\arm\lib\libswresample-2.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := swscale
LOCAL_SRC_FILES := G:\WebRtc\FFmpeg-SDK\arm\lib\libswscale-4.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := x264
LOCAL_SRC_FILES := G:\WebRtc\FFmpeg-SDK\arm\lib\libx264-150.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := postproc
LOCAL_SRC_FILES := G:\WebRtc\FFmpeg-SDK\arm\lib\libpostproc-54.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE    := com_thinking_ffmpegtest
LOCAL_SRC_FILES := com_thinking_cameralive_old_func_FFmpeg.cpp

LOCAL_C_INCLUDES += G:\WebRtc\FFmpeg-SDK\arm\include
LOCAL_LDLIBS +=  -llog -ldl -lz
LOCAL_SHARED_LIBRARIES := avcodec avdevice avfilter avformat avutil swresample swscale x264 postproc
include $(BUILD_SHARED_LIBRARY)
