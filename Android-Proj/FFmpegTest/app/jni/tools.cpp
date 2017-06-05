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
	if (ret <0 && ret != AVERROR_EOF) {
		return ret;
	}
	ret = avcodec_receive_packet(pCodecCtx, pPkt);
	if (ret < 0 && ret != AVERROR(EAGAIN)) {
		return ret;
	}
	if (ret >= 0) {
		*got_packet = 1;
	}
	return 0;
}