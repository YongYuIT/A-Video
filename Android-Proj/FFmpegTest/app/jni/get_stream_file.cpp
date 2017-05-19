#include"com_thinking_ffmpegtest_FFmpegTools.h"


JNIEXPORT void JNICALL Java_com_thinking_ffmpegtest_FFmpegTools_getStreamFromFile
(JNIEnv * env, jclass j_class, jstring file_path, jstring rtmp_add){
	const char *   _file_path = env->GetStringUTFChars(file_path, false);
	const char *   _rtmp_add = env->GetStringUTFChars(rtmp_add, false);
	__android_log_print(ANDROID_LOG_INFO, "yuyong", "from %s to %s", _file_path, _rtmp_add);
}