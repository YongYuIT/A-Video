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
//static AVPacket pkt;

static int frame_count;

JNIEXPORT void JNICALL Java_com_thinking_ffmpegtest_FFmpegTools_getStreamFromCamera
(JNIEnv * env, jclass j_class)
{
	__android_log_print(ANDROID_LOG_INFO, "yuyong_push", "push start");
	//----------------------------------------------------------------------------------------------------------------------------------------start
	jclass cache_class = env->FindClass("com/thinking/ffmpegtest/FrameNIOCache");
	//��ȡjava������
	jfieldID j_buffer_id = env->GetStaticFieldID(cache_class, "mCache", "Ljava/nio/ByteBuffer;");
	jobject j_buffer = env->GetStaticObjectField(cache_class, j_buffer_id);
	unsigned char * _output = (unsigned char *)env->GetDirectBufferAddress(j_buffer);
	//----------------------------------------------------------------------------------------------------------------------------------------end
	int y_length = width*height;
	int uv_lenth = y_length / 4;
	memcpy(yuv_frame->data[0], _output, y_length);//����Y��������
	for (int i = 0; i < uv_lenth; i++){
		//����UV��������
		*(yuv_frame->data[2] + i) = *(_output + y_length + i * 2);
		*(yuv_frame->data[1] + i) = *(_output + y_length + i * 2 + 1);
	}
	__android_log_print(ANDROID_LOG_INFO, "yuyong_push", "copy frame data finish");
	yuv_frame->format = pCodecCtx->pix_fmt;
	yuv_frame->width = width;
	yuv_frame->height = height;
	yuv_frame->pts = (1.0 / 30) * 90 * frame_count;
	__android_log_print(ANDROID_LOG_INFO, "yuyong_push", "frame settings finish");

	AVPacket pkt;
	av_init_packet(&pkt);
	pkt.data = NULL;
	pkt.size = 0;
	__android_log_print(ANDROID_LOG_INFO, "yuyong_push", "going to pack");

	if (1 == 0)
		return;

	//���б���
	int got_packet = 0;
	int ret = encode(pCodecCtx, &pkt, yuv_frame, &got_packet);
	if (ret < 0) {
		__android_log_print(ANDROID_LOG_INFO, "yuyong_push", "encode error");
		return;
	}


	if (1 == 1)
		return;

	__android_log_print(ANDROID_LOG_INFO, "yuyong_push", "encode finish %i", got_packet);

	if (got_packet) {
		__android_log_print(ANDROID_LOG_INFO, "yuyong_push", "encode frame : %i size : %i", frame_count, pkt.size);
		frame_count++;
		pkt.stream_index = out_stream->index;

		//дPTS/DTS
		AVRational time_base1 = ofmt_ctx->streams[0]->time_base;
		AVRational r_frame_rate1 = { 60, 2 };
		AVRational time_base_q = { 1, AV_TIME_BASE };
		int64_t calc_duration = (double)(AV_TIME_BASE)* (1 / av_q2d(r_frame_rate1));

		pkt.pts = av_rescale_q(frame_count * calc_duration, time_base_q, time_base1);
		pkt.dts = pkt.pts;
		pkt.duration = av_rescale_q(calc_duration, time_base_q, time_base1);
		pkt.pos = -1;

		//�����ӳ�
		int64_t pts_time = av_rescale_q(pkt.dts, time_base1, time_base_q);
		int64_t now_time = av_gettime() - start_time;
		if (pts_time > now_time) {
			av_usleep(pts_time - now_time);
		}

		ret = av_interleaved_write_frame(ofmt_ctx, &pkt);
		if (ret < 0) {
			__android_log_print(ANDROID_LOG_INFO, "yuyong_push", "error muxing packet");
			return;
		}
		av_packet_unref(&pkt);
		__android_log_print(ANDROID_LOG_INFO, "yuyong_push", "push success");
	}

}

JNIEXPORT void JNICALL Java_com_thinking_ffmpegtest_FFmpegTools_setStreamFromCameraInit
(JNIEnv *env, jclass j_class, jint _height, jint _width, jstring _rtmp_add)
{
	trmp_add = env->GetStringUTFChars(_rtmp_add, false);
	height = _height;
	width = _width;
	__android_log_print(ANDROID_LOG_INFO, "yuyong", "camera to server size= %i*%i to %s", width, height, trmp_add.c_str());
	//----------------------------------------------------------------------------------------------------------------------------------------start
	//ͨ��java��������NIO������
	jclass cache_class = env->FindClass("com/thinking/ffmpegtest/FrameNIOCache");
	//����java��������С
	jmethodID j_set_size = env->GetStaticMethodID(cache_class, "setCache", "(II)V");
	env->CallStaticVoidMethod(cache_class, j_set_size, width, height);
	//----------------------------------------------------------------------------------------------------------------------------------------end
	//������־��ӡ�ص�
	av_log_set_callback(print_ffmpeg_log);
	//�������й���
	av_register_all();
	//��ʼ����·Э��
	avformat_network_init();

	//�ڻ���ffmpeg������Ƶ��������У��������ͨ���ǵ�һ�����õĺ���������ע���������av_register_all�⣩����������������ǣ�
	//��ʼ��һ�����������AVFormatContext
	avformat_alloc_output_context2(&ofmt_ctx, NULL, "flv", trmp_add.c_str());
	if (!ofmt_ctx)
	{
		__android_log_print(ANDROID_LOG_INFO, "yuyong", "failed to init output context");
		return;
	}
	//ѡ��H264��Ϊ������
	//����Ҳ���H264���������ο�http://blog.csdn.net/PZ0605/article/details/52958918?locationNum=6&fps=1
	//�ο�http://zhengxiaoyong.me/2016/11/13/%E5%88%9D%E8%AF%86FFmpeg%E7%BC%96%E8%AF%91%E9%82%A3%E4%BA%9B%E4%BA%8B/
	pCodec = avcodec_find_encoder(AV_CODEC_ID_H264);
	if (!pCodec)
	{
		__android_log_print(ANDROID_LOG_INFO, "yuyong", "can not found h264 encoder");
		return;
	}


	//���ñ�������������
	pCodecCtx = avcodec_alloc_context3(pCodec);      //ָ��������
	pCodecCtx->pix_fmt = AV_PIX_FMT_YUV420P;         //ָ�������ʽ
	pCodecCtx->width = width;                        //��Ƶ��px
	pCodecCtx->height = height;                      //��Ƶ�ߣ�px
	pCodecCtx->time_base.num = 1;                    //time_base�ķ���
	pCodecCtx->time_base.den = 30;                   //time_base�ķ�ĸ��time_base = 1/30����ʾ��������30Hz
	pCodecCtx->bit_rate = 800000;                    //ƽ�������ʣ�
	pCodecCtx->gop_size = 300;                       //��������������ÿ300��֡ʱ�����һ֡����
	if (ofmt_ctx->oformat->flags & AVFMT_GLOBALHEADER)
	{
		pCodecCtx->flags |= CODEC_FLAG_GLOBAL_HEADER;
	}
	pCodecCtx->qmin = 10;                            //��С����ϵ��
	pCodecCtx->qmax = 51;                            //�������ϵ��
	pCodecCtx->max_b_frames = 3;                     //������B��֡�����ؼ�֡��֮������B֡��Ŀ

	//AVDictionary��ֵ�����飬�������ñ�����
	AVDictionary *dicParams = NULL;
	av_dict_set(&dicParams, "preset", "ultrafast", 0);//Ԥ�裬����
	av_dict_set(&dicParams, "tune", "zerolatency", 0);//�ٶȣ�����ʱ

	//�򿪱�����
	if (avcodec_open2(pCodecCtx, pCodec, &dicParams) < 0) {
		__android_log_print(ANDROID_LOG_INFO, "yuyong", "failed to open encoder");
		return;
	}

	//���������
	out_stream = avformat_new_stream(ofmt_ctx, pCodec);
	if (!out_stream) {
		__android_log_print(ANDROID_LOG_INFO, "yuyong", "failed allocation output stream");
		return;
	}
	out_stream->time_base.num = 1;
	out_stream->time_base.den = 30;
	//���Ʊ����������ø������
	avcodec_parameters_from_context(out_stream->codecpar, pCodecCtx);


	//�������
	int ret = avio_open(&ofmt_ctx->pb, trmp_add.c_str(), AVIO_FLAG_WRITE);//״ָ̬ʾ
	if (ret < 0) {
		__android_log_print(ANDROID_LOG_INFO, "yuyong", "can not open output URL %s", trmp_add.c_str());
		return;
	}

	ret = avformat_write_header(ofmt_ctx, NULL);
	if (ret < 0) {
		__android_log_print(ANDROID_LOG_INFO, "yuyong", "write header failed");
		return;
	}

	//��ʼ��֡����
	yuv_frame = av_frame_alloc();
	uint8_t *out_buffer = (uint8_t *)av_malloc(av_image_get_buffer_size(pCodecCtx->pix_fmt, width, height, 1));
	av_image_fill_arrays(yuv_frame->data, yuv_frame->linesize, out_buffer, pCodecCtx->pix_fmt, width, height, 1);

	//��¼��ʼʱ��
	start_time = av_gettime();
	frame_count = 0;
	__android_log_print(ANDROID_LOG_INFO, "yuyong", "init success %i", start_time);
}
