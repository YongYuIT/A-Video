#ifndef INT64_C
#define INT64_C
#define UINT64_C
#endif

#include"com_thinking_ffmpegtest_FFmpegTools.h"
#include<android\log.h>

JNIEXPORT void JNICALL Java_com_thinking_ffmpegtest_FFmpegTools_test
(JNIEnv * env, jclass j_class){
	AVCodec * pAv = NULL;
	avcodec_register_all();
	pAv = avcodec_find_encoder(AV_CODEC_ID_MPEG4);
	if (pAv == NULL){
		__android_log_print(ANDROID_LOG_INFO, "yuyong", "test fail");
	}
	__android_log_print(ANDROID_LOG_INFO, "yuyong", "test success");
}
