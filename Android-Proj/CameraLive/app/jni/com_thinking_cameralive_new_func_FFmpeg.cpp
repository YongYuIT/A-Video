#include"com_thinking_cameralive_new_func_FFmpeg.h"
using namespace std;
#include<iostream>

static int height;
static int width;
static string rtmp_add;

static AVFormatContext *ofmt_ctx = NULL;
static AVCodec *pCodec = NULL;
static AVCodecContext *pCodecCtx = NULL;
static AVStream *out_stream = NULL;
static AVFrame *yuv_frame;
static int64_t start_time;
static int frame_count;

static AVPacket pkt;

void print_log(void *ptr, int level, const char* fmt, va_list vl) {
	//__android_log_print(ANDROID_LOG_INFO, "yuyong ffmpeg", fmt, vl);
}

int do_encode(AVCodecContext *pCodecCtx, AVPacket* pPkt, AVFrame *pFrame, int *got_packet) {
	int ret;
	*got_packet = 0;
	ret = avcodec_send_frame(pCodecCtx, pFrame);
	if (ret < 0 && ret != AVERROR_EOF) {
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

JNIEXPORT jboolean JNICALL Java_com_thinking_cameralive_new_1func_FFmpeg_ffmpegInit
(JNIEnv * env, jclass, jint w, jint h, jstring address)
{
	rtmp_add = env->GetStringUTFChars(address, false);
	height = h;
	width = w;
	__android_log_print(ANDROID_LOG_INFO, "yuyong", "init ffmpeg size= %i*%i to %s", width, height, rtmp_add.c_str());

	//������־��ӡ�ص�
	av_log_set_callback(print_log);
	//�������й���
	av_register_all();
	//��ʼ����·Э��
	avformat_network_init();

	//�ڻ���ffmpeg������Ƶ��������У��������ͨ���ǵ�һ�����õĺ���������ע���������av_register_all�⣩����������������ǣ�
	//��ʼ��һ�����������AVFormatContext
	avformat_alloc_output_context2(&ofmt_ctx, NULL, "flv", rtmp_add.c_str());
	if (!ofmt_ctx)
	{
		__android_log_print(ANDROID_LOG_INFO, "yuyong", "failed to init output context");
		return false;
	}
	//ѡ��H264��Ϊ������
	//����Ҳ���H264���������ο�http://blog.csdn.net/PZ0605/article/details/52958918?locationNum=6&fps=1
	//                      �ο�http://zhengxiaoyong.me/2016/11/13/%E5%88%9D%E8%AF%86FFmpeg%E7%BC%96%E8%AF%91%E9%82%A3%E4%BA%9B%E4%BA%8B/
	pCodec = avcodec_find_encoder(AV_CODEC_ID_H264);
	if (!pCodec)
	{
		__android_log_print(ANDROID_LOG_INFO, "yuyong", "can not found h264 encoder");
		return false;
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
		return false;
	}

	//���������
	out_stream = avformat_new_stream(ofmt_ctx, pCodec);
	if (!out_stream) {
		__android_log_print(ANDROID_LOG_INFO, "yuyong", "failed allocation output stream");
		return false;
	}
	out_stream->time_base.num = 1;
	out_stream->time_base.den = 30;
	//���Ʊ����������ø������
	avcodec_parameters_from_context(out_stream->codecpar, pCodecCtx);


	//�������
	int ret = avio_open(&ofmt_ctx->pb, rtmp_add.c_str(), AVIO_FLAG_WRITE);//״ָ̬ʾ
	if (ret < 0) {
		__android_log_print(ANDROID_LOG_INFO, "yuyong", "can not open output URL %s", rtmp_add.c_str());
		return false;
	}

	ret = avformat_write_header(ofmt_ctx, NULL);
	if (ret < 0) {
		__android_log_print(ANDROID_LOG_INFO, "yuyong", "write header failed");
		return false;
	}

	//��ʼ��֡����
	yuv_frame = av_frame_alloc();
	uint8_t *out_buffer = (uint8_t *)av_malloc(av_image_get_buffer_size(pCodecCtx->pix_fmt, width, height, 1));
	av_image_fill_arrays(yuv_frame->data, yuv_frame->linesize, out_buffer, pCodecCtx->pix_fmt, width, height, 1);

	//��¼��ʼʱ��
	start_time = av_gettime();
	frame_count = 0;
	__android_log_print(ANDROID_LOG_INFO, "yuyong", "init success %i", start_time);
	return true;
}

JNIEXPORT jint JNICALL Java_com_thinking_cameralive_new_1func_FFmpeg_ffmpegPush
(JNIEnv * env, jclass, jbyteArray input_datas){

	__android_log_print(ANDROID_LOG_INFO, "yuyong", "push check size= %i*%i to %s", width, height, rtmp_add.c_str());
	int y_length = width*height;
	int uv_length = y_length / 4;
	jbyte *data = env->GetByteArrayElements(input_datas, NULL);

	memcpy(yuv_frame->data[0], data, y_length);//����Y��������
	for (int i = 0; i < uv_length; i++) {//����UV��������
		*(yuv_frame->data[2] + i) = *(data + y_length + i * 2);
		*(yuv_frame->data[1] + i) = *(data + y_length + i * 2 + 1);
	}

	yuv_frame->format = pCodecCtx->pix_fmt;
	yuv_frame->width = width;
	yuv_frame->height = height;
	yuv_frame->pts = (1.0 / 30) * 90 * frame_count;


	pkt.data = NULL;
	pkt.size = 0;
	av_init_packet(&pkt);

	//����
	int  resultCode = 0;
	int got_packet = 0;
	int ret = do_encode(pCodecCtx, &pkt, yuv_frame, &got_packet);
	if (ret < 0) {
		resultCode = -1;
		__android_log_print(ANDROID_LOG_INFO, "yuyong_push", "encode error");
		goto end;
	}

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
			resultCode = -1;
			goto end;
		}
		av_packet_unref(&pkt);
	}
end:
	env->ReleaseByteArrayElements(input_datas, data, 0);
	return resultCode;

}