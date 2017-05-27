#include"com_thinking_ffmpegtest_FFmpegTools.h"
#include"tools.h"

//http://blog.csdn.net/leixiaohua1020/article/details/39803457

JNIEXPORT void JNICALL Java_com_thinking_ffmpegtest_FFmpegTools_getStreamFromFile
(JNIEnv * env, jclass j_class, jstring file_path, jstring rtmp_add){
	const char *   _file_path = env->GetStringUTFChars(file_path, false);
	const char *   _rtmp_add = env->GetStringUTFChars(rtmp_add, false);
	__android_log_print(ANDROID_LOG_INFO, "yuyong", "from %s to %s", _file_path, _rtmp_add);

	//状态指示
	int ret;
	//输出参数
	AVOutputFormat *ofmt = NULL;
	AVFormatContext *ofmt_ctx = NULL;
	//输入参数
	AVFormatContext *ifmt_ctx = NULL;
	//传输参数
	int frame_index = 0;
	int64_t start_time = 0;
	int videoindex = -1;

	//注册组件
	av_register_all();

	//初始化输出参数------------------------star
	avformat_alloc_output_context2(&ofmt_ctx, NULL, "flv", _rtmp_add);
	if (!ofmt_ctx) {
		__android_log_print(ANDROID_LOG_INFO, "yuyong", "unknown error fuck");
		ret = AVERROR_UNKNOWN;
		goto end;
	}
	ofmt = ofmt_ctx->oformat;
	//初始化输出参数------------------------end

	//初始化输入参数------------------------start
	if ((ret = avformat_open_input(&ifmt_ctx, _file_path, 0, 0)) < 0)
	{
		__android_log_print(ANDROID_LOG_INFO, "yuyong", "cannot open media file");
		goto end;
	}
	if ((ret = avformat_find_stream_info(ifmt_ctx, 0)) < 0) {
		__android_log_print(ANDROID_LOG_INFO, "yuyong", "cannot conn server");
		goto end;
	}
	for (int i = 0; i < ifmt_ctx->nb_streams; i++){
		__android_log_print(ANDROID_LOG_INFO, "yuyong", "codec_type for %i = %s", i, getAVMediaTypeName(ifmt_ctx->streams[i]->codec->codec_type).c_str());
		if (ifmt_ctx->streams[i]->codec->codec_type == AVMEDIA_TYPE_VIDEO){
			videoindex = i;
			break;
		}
	}
	av_dump_format(ifmt_ctx, 0, _file_path, 0);
	//初始化输入参数------------------------end

	//建立连接------------------------start
	avformat_network_init();
	for (int i = 0; i < ifmt_ctx->nb_streams; i++) {
		AVStream *in_stream = ifmt_ctx->streams[i];
		__android_log_print(ANDROID_LOG_INFO, "yuyong", "iostream for %i = %s", i, getAVMediaTypeName(in_stream->codec->codec_type).c_str());
		AVStream *out_stream = avformat_new_stream(ofmt_ctx, in_stream->codec->codec);
		if (!out_stream) {
			__android_log_print(ANDROID_LOG_INFO, "yuyong", "Failed allocating output stream");
			ret = AVERROR_UNKNOWN;
			goto end;
		}
		ret = avcodec_copy_context(out_stream->codec, in_stream->codec);
		if (ret < 0) {
			__android_log_print(ANDROID_LOG_INFO, "yuyong", "Failed to copy context from input to output stream codec context");
			goto end;
		}
		out_stream->codec->codec_tag = 0;
		if (ofmt_ctx->oformat->flags & AVFMT_GLOBALHEADER)
			out_stream->codec->flags |= CODEC_FLAG_GLOBAL_HEADER;
	}

	av_dump_format(ofmt_ctx, 0, _rtmp_add, 1);
	if (!(ofmt->flags & AVFMT_NOFILE)) {
		ret = avio_open(&ofmt_ctx->pb, _rtmp_add, AVIO_FLAG_WRITE);
		if (ret < 0) {
			__android_log_print(ANDROID_LOG_INFO, "yuyong", "Could not open output URL '%s'", _rtmp_add);
			goto end;
		}
	}
	//建立连接------------------------end


	//写入头部------------------------start
	ret = avformat_write_header(ofmt_ctx, NULL);
	if (ret < 0) {
		__android_log_print(ANDROID_LOG_INFO, "yuyong", "Error occurred when opening output URL");
		goto end;
	}
	//写入头部------------------------end

	//传输数据------------------------start
	AVPacket pkt;
	start_time = av_gettime();
	while (1) {
		AVStream *in_stream, *out_stream;
		ret = av_read_frame(ifmt_ctx, &pkt);
		if (ret < 0)
			break;
		if (pkt.pts == AV_NOPTS_VALUE){
			AVRational time_base1 = ifmt_ctx->streams[videoindex]->time_base;
			int64_t calc_duration = (double)AV_TIME_BASE / av_q2d(ifmt_ctx->streams[videoindex]->r_frame_rate);
			pkt.pts = (double)(frame_index*calc_duration) / (double)(av_q2d(time_base1)*AV_TIME_BASE);
			pkt.dts = pkt.pts;
			pkt.duration = (double)calc_duration / (double)(av_q2d(time_base1)*AV_TIME_BASE);
		}
		if (pkt.stream_index == videoindex){
			AVRational time_base = ifmt_ctx->streams[videoindex]->time_base;
			AVRational time_base_q = { 1, AV_TIME_BASE };
			int64_t pts_time = av_rescale_q(pkt.dts, time_base, time_base_q);
			int64_t now_time = av_gettime() - start_time;
			if (pts_time > now_time)
				av_usleep(pts_time - now_time);
		}
		in_stream = ifmt_ctx->streams[pkt.stream_index];
		out_stream = ofmt_ctx->streams[pkt.stream_index];
		pkt.pts = av_rescale_q_rnd(pkt.pts, in_stream->time_base, out_stream->time_base, (AVRounding)(AV_ROUND_NEAR_INF | AV_ROUND_PASS_MINMAX));
		pkt.dts = av_rescale_q_rnd(pkt.dts, in_stream->time_base, out_stream->time_base, (AVRounding)(AV_ROUND_NEAR_INF | AV_ROUND_PASS_MINMAX));
		pkt.duration = av_rescale_q(pkt.duration, in_stream->time_base, out_stream->time_base);
		pkt.pos = -1;
		if (pkt.stream_index == videoindex){
			__android_log_print(ANDROID_LOG_INFO, "yuyong", "Send %8d video frames to output URL", frame_index);
			frame_index++;
		}
		ret = av_interleaved_write_frame(ofmt_ctx, &pkt);
		if (ret < 0) {
			__android_log_print(ANDROID_LOG_INFO, "yuyong", "Error muxing packet");
			break;
		}
		av_free_packet(&pkt);
	}
	av_write_trailer(ofmt_ctx);
	//传输数据------------------------end

end:
	avformat_close_input(&ifmt_ctx);
	if (ofmt_ctx && !(ofmt->flags & AVFMT_NOFILE))
		avio_close(ofmt_ctx->pb);
	avformat_free_context(ofmt_ctx);
	if (ret < 0 && ret != AVERROR_EOF) {
		__android_log_print(ANDROID_LOG_INFO, "yuyong", "Error occurred.");
		return;
	}
	return;
}