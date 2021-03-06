/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
#include<android\log.h>
/* Header for class com_thinking_ffmpegtest_FFmpegTools */

#ifndef INT64_C
#define INT64_C
#define UINT64_C
#endif

#ifndef _Included_com_thinking_ffmpegtest_FFmpegTools
#define _Included_com_thinking_ffmpegtest_FFmpegTools

#define LOG_TAG "yuyong"

#define LOGE(format, ...)  __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, format, ##__VA_ARGS__)
#define LOGI(format, ...)  __android_log_print(ANDROID_LOG_INFO,  LOG_TAG, format, ##__VA_ARGS__)


#ifdef __cplusplus
extern "C" {
#endif

	//由于ffmpeg是纯C写的，所以所有的include必须包含在extern "C"内
#include<libavcodec\avcodec.h>
#include<libavformat\avformat.h>
#include<libavutil\time.h>
#include<libavutil\imgutils.h>
	/*
	 * Class:     com_thinking_cameralive_FFmpeg
	 * Method:    streamerInit
	 * Signature: (II)I
	 */
	JNIEXPORT jint JNICALL Java_com_thinking_cameralive_FFmpeg_streamerInit
		(JNIEnv *, jclass, jint, jint);

	/*
	 * Class:     com_thinking_cameralive_FFmpeg
	 * Method:    streamerHandle
	 * Signature: ([B)I
	 */
	JNIEXPORT jint JNICALL Java_com_thinking_cameralive_FFmpeg_streamerHandle
		(JNIEnv *, jclass, jbyteArray);

	/*
	 * Class:     com_thinking_cameralive_FFmpeg
	 * Method:    streamerFlush
	 * Signature: ()I
	 */
	JNIEXPORT jint JNICALL Java_com_thinking_cameralive_FFmpeg_streamerFlush
		(JNIEnv *, jclass);

	/*
	 * Class:     com_thinking_cameralive_FFmpeg
	 * Method:    streamerRelease
	 * Signature: ()I
	 */
	JNIEXPORT jint JNICALL Java_com_thinking_cameralive_FFmpeg_streamerRelease
		(JNIEnv *, jclass);

#ifdef __cplusplus
}
#endif
#endif
