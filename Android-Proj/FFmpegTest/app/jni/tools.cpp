#include"tools.h"
#include"com_thinking_ffmpegtest_FFmpegTools.h"

string getAVMediaTypeName(int type){
	switch (type)
	{
	case AVMEDIA_TYPE_UNKNOWN:
		return "AVMEDIA_TYPE_UNKNOWN";
	case AVMEDIA_TYPE_VIDEO:
		return "AVMEDIA_TYPE_VIDEO";
	case AVMEDIA_TYPE_AUDIO:
		return "AVMEDIA_TYPE_AUDIO";
	case AVMEDIA_TYPE_DATA:
		return "AVMEDIA_TYPE_DATA";
	case AVMEDIA_TYPE_SUBTITLE:
		return "AVMEDIA_TYPE_SUBTITLE";
	case AVMEDIA_TYPE_ATTACHMENT:
		return "AVMEDIA_TYPE_ATTACHMENT";
	case AVMEDIA_TYPE_NB:
		return "AVMEDIA_TYPE_NB";
	default:
		return "ERROR_TYPR";
	}
}

void print_ffmpeg_log(void *ptr, int level, const char* fmt, va_list vl){
	__android_log_print(ANDROID_LOG_INFO, "yuyong ffmpeg", fmt, vl);
}

int encode(AVCodecContext *pCodecCtx, AVPacket* pPkt, AVFrame *pFrame, int *got_packet){

	int ret;
	*got_packet = 0;
	ret = avcodec_send_frame(pCodecCtx, pFrame);
	__android_log_print(ANDROID_LOG_INFO, "yuyong_push", "avcodec_send_frame %i", ret);
	if (ret < 0 && ret != AVERROR_EOF) {
		return ret;
	}

	ret = avcodec_receive_packet(pCodecCtx, pPkt);
	__android_log_print(ANDROID_LOG_INFO, "yuyong_push", "avcodec_receive_packet %i of (%i %i %i)", ret, AVERROR(EAGAIN), AVERROR_EOF, AVERROR(EINVAL));
	if (ret < 0 && ret != AVERROR(EAGAIN)) {
		return ret;
	}

	if (ret >= 0) {
		__android_log_print(ANDROID_LOG_INFO, "yuyong_push", "encode success");
		*got_packet = 1;
	}
	__android_log_print(ANDROID_LOG_INFO, "yuyong_push", "encode go back %i", ret);
	return 0;
}