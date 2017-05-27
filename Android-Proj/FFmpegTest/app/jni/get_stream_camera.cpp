#include"com_thinking_ffmpegtest_FFmpegTools.h"
#include"tools.h"

//http://blog.csdn.net/leixiaohua1020/article/details/12980423
//http://blog.csdn.net/u011485531/article/details/56013148

int fill_iobuffer(void * opaque, uint8_t *buf, int bufsize){

}

static int height;
static int width;
static string trmp_add;

static AVFormatContext *ofmt_ctx = NULL;
static AVCodec *pCodec = NULL;
static AVCodecContext *pCodecCtx = NULL;
static AVStream *out_stream = NULL;
static AVFrame *yuv_frame;
static int64_t start_time;

JNIEXPORT void JNICALL Java_com_thinking_ffmpegtest_FFmpegTools_getStreamFromCamera
(JNIEnv * env, jclass j_class)
{
	//----------------------------------------------------------------------------------------------------------------------------------------start
	jclass cache_class = env->FindClass("com/thinking/ffmpegtest/FrameNIOCache");
	//获取java缓冲区
	jfieldID j_buffer_id = env->GetStaticFieldID(cache_class, "mCache", "Ljava/nio/ByteBuffer;");
	jobject j_buffer = env->GetStaticObjectField(cache_class, j_buffer_id);
	unsigned char * _output = (unsigned char *)env->GetDirectBufferAddress(j_buffer);
	//获取缓冲区大小
	jmethodID j_get_size = env->GetStaticMethodID(cache_class, "getCacheSize", "()I");
	int size = env->CallStaticIntMethod(cache_class, j_get_size);
	__android_log_print(ANDROID_LOG_INFO, "yuyong", "camera to server size= %i*%i -> %i to %s", width, height, size, trmp_add.c_str());
	//----------------------------------------------------------------------------------------------------------------------------------------end

	AVFormatContext *ic = NULL;
	ic = avformat_alloc_context();
	AVIOContext *avio = avio_alloc_context(_output, size, 0, NULL, fill_iobuffer, NULL, NULL);
	ic->pb = avio;
	avformat_open_input(&ic, "nothing", NULL, NULL);


}

JNIEXPORT void JNICALL Java_com_thinking_ffmpegtest_FFmpegTools_setStreamFromCameraInit
(JNIEnv *env, jclass j_class, jint _height, jint _width, jstring _rtmp_add){
	trmp_add = env->GetStringUTFChars(_rtmp_add, false);
	height = _height;
	width = _width;
	//----------------------------------------------------------------------------------------------------------------------------------------start
	jclass cache_class = env->FindClass("com/thinking/ffmpegtest/FrameNIOCache");
	//设置java缓冲区大小
	jmethodID j_set_size = env->GetStaticMethodID(cache_class, "setCache", "(II)V");
	env->CallStaticVoidMethod(cache_class, j_set_size, width, height);
	//----------------------------------------------------------------------------------------------------------------------------------------end
	//状态指示
	int ret = 0;
	//设置日志打印回调
	av_log_set_callback(print_ffmpeg_log);
	//激活所有功能
	av_register_all();
	//初始化网路协议
	avformat_network_init();

	//初始化
	avformat_alloc_output_context2(&ofmt_ctx, NULL, "flv", trmp_add.c_str());
	if (!ofmt_ctx)
	{
		__android_log_print(ANDROID_LOG_INFO, "yuyong", "AVFormatContext init failed");
		return;
	}
	//选用H264作为编码器
	//如果找不到H264编译器，参考http://blog.csdn.net/PZ0605/article/details/52958918?locationNum=6&fps=1
	pCodec = avcodec_find_encoder(AV_CODEC_ID_H264);
	if (!pCodec)
	{
		__android_log_print(ANDROID_LOG_INFO, "yuyong", "pCodec init failed");
		return;
	}
	//初始化编码器的上下文
	pCodecCtx = avcodec_alloc_context3(pCodec);
	pCodecCtx->pix_fmt = AV_PIX_FMT_YUV420P;  //指定编码格式
	pCodecCtx->width = width;
	pCodecCtx->height = height;
	pCodecCtx->time_base.num = 1;
	pCodecCtx->time_base.den = 30;
	pCodecCtx->bit_rate = 800000;
	pCodecCtx->gop_size = 300;
	if (ofmt_ctx->oformat->flags & AVFMT_GLOBALHEADER)
	{
		pCodecCtx->flags |= CODEC_FLAG_GLOBAL_HEADER;
	}
	pCodecCtx->qmin = 10;
	pCodecCtx->qmax = 51;
	pCodecCtx->max_b_frames = 3;

	AVDictionary *dicParams = NULL;
	av_dict_set(&dicParams, "preset", "ultrafast", 0);
	av_dict_set(&dicParams, "tune", "zerolatency", 0);

	//打开编码器
	if (avcodec_open2(pCodecCtx, pCodec, &dicParams) < 0) {
		__android_log_print(ANDROID_LOG_INFO, "yuyong", "Failed to open encoder");
		return;
	}

	//建立输出流
	out_stream = avformat_new_stream(ofmt_ctx, pCodec);
	if (!out_stream) {
		__android_log_print(ANDROID_LOG_INFO, "yuyong", "Failed allocation output stream");
		return;
	}
	out_stream->time_base.num = 1;
	out_stream->time_base.den = 30;
	//复制编码器的配置给输出流
	avcodec_parameters_from_context(out_stream->codecpar, pCodecCtx);

	//打开输出流
	ret = avio_open(&ofmt_ctx->pb, trmp_add.c_str(), AVIO_FLAG_WRITE);
	if (ret < 0) {
		__android_log_print(ANDROID_LOG_INFO, "yuyong", "Could not open output URL %s", trmp_add.c_str());
		return;
	}

	ret = avformat_write_header(ofmt_ctx, NULL);
	if (ret < 0) {
		__android_log_print(ANDROID_LOG_INFO, "yuyong", "write header fail");
		return;
	}

	//初始化帧载体
	yuv_frame = av_frame_alloc();
	uint8_t *out_buffer = (uint8_t *)av_malloc(av_image_get_buffer_size(pCodecCtx->pix_fmt, width, height, 1));
	av_image_fill_arrays(yuv_frame->data, yuv_frame->linesize, out_buffer, pCodecCtx->pix_fmt, width, height, 1);

	start_time = av_gettime();
	__android_log_print(ANDROID_LOG_INFO, "yuyong", "init success");
}