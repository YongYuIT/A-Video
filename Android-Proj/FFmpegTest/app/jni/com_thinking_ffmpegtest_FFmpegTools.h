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
#ifdef __cplusplus
extern "C" {
#endif

	//由于ffmpeg是纯C写的，所以所有的include必须包含在extern "C"内
#include<libavcodec\avcodec.h>
#include<libavformat\avformat.h>
#include<libavutil\time.h>
#include "libavutil/imgutils.h"
	/*
	 * Class:     com_thinking_ffmpegtest_FFmpegTools
	 * Method:    test
	 * Signature: ()V
	 */
	JNIEXPORT void JNICALL Java_com_thinking_ffmpegtest_FFmpegTools_test
		(JNIEnv *, jclass);

	/*
	 * Class:     com_thinking_ffmpegtest_FFmpegTools
	 * Method:    getStreamFromFile
	 * Signature: (Ljava/lang/String;Ljava/lang/String;)V
	 */
	JNIEXPORT void JNICALL Java_com_thinking_ffmpegtest_FFmpegTools_getStreamFromFile
		(JNIEnv *, jclass, jstring, jstring);

	/*
	 * Class:     com_thinking_ffmpegtest_FFmpegTools
	 * Method:    getStreamFromCamera
	 * Signature: ()V
	 */
	JNIEXPORT void JNICALL Java_com_thinking_ffmpegtest_FFmpegTools_getStreamFromCamera
		(JNIEnv *, jclass);

	/*
	 * Class:     com_thinking_ffmpegtest_FFmpegTools
	 * Method:    setStreamFromCameraInit
	 * Signature: (IILjava/lang/String;)V
	 */
	JNIEXPORT void JNICALL Java_com_thinking_ffmpegtest_FFmpegTools_setStreamFromCameraInit
		(JNIEnv *, jclass, jint, jint, jstring);


#ifdef __cplusplus
}
#endif
#endif
