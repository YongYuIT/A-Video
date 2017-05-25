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