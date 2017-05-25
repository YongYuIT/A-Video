#include"com_thinking_ffmpegtest_FFmpegTools.h"
#include"tools.h"

//http://blog.csdn.net/leixiaohua1020/article/details/12980423

int fill_iobuffer(void * opaque, uint8_t *buf, int bufsize){

}

JNIEXPORT void JNICALL Java_com_thinking_ffmpegtest_FFmpegTools_getStreamFromCamera
(JNIEnv * env, jclass j_class, jstring rtmp_add)
{
	const char *   _rtmp_add = env->GetStringUTFChars(rtmp_add, false);

	//----------------------------------------------------------------------------------------------------------------------------------------start
	jclass cache_class = env->FindClass("com/thinking/ffmpegtest/FrameNIOCache");
	//获取java缓冲区
	jfieldID j_buffer_id = env->GetStaticFieldID(cache_class, "mCache", "Ljava/nio/ByteBuffer;");
	jobject j_buffer = env->GetStaticObjectField(cache_class, j_buffer_id);
	unsigned char * _output = (unsigned char *)env->GetDirectBufferAddress(j_buffer);
	//获取缓冲区大小
	jmethodID j_get_size = env->GetStaticMethodID(cache_class, "getCacheSize", "()I");
	int size = env->CallStaticIntMethod(cache_class, j_get_size);

	__android_log_print(ANDROID_LOG_INFO, "yuyong", "camera to server size=%i to:%s", size, _rtmp_add);

	//----------------------------------------------------------------------------------------------------------------------------------------end

	AVFormatContext *ic = NULL;
	ic = avformat_alloc_context();
	AVIOContext *avio = avio_alloc_context(_output, size, 0, NULL, fill_iobuffer, NULL, NULL);
	ic->pb = avio;
	avformat_open_input(&ic, "nothing", NULL, NULL);
}