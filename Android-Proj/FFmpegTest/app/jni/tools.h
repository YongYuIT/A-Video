#ifndef COM_THINKING_FFMPEGTEST_TOOLS_H
#define COM_THINKING_FFMPEGTEST_TOOLS_H

#include<iostream>
using namespace std;

string getAVMediaTypeName(int type);
void print_ffmpeg_log(void *ptr, int level, const char* fmt, va_list vl);

#endif