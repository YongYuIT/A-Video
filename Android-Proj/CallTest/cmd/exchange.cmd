set device1=84B7N16708000561
set device2=0123456789ABCDEF
echo %device1% %device2%

adb -s %device1% shell 'rm -f /sdcard/web_rtc_remote.config'
adb -s %device2% shell 'rm -f /sdcard/web_rtc_remote.config'
adb -s %device1% shell 'rm -f /sdcard/web_rtc_remote_params.config'
adb -s %device2% shell 'rm -f /sdcard/web_rtc_remote_params.config'

md .\%device1%
cd .\%device1%
adb -s %device1% pull /sdcard/web_rtc_local.config ./
copy .\web_rtc_local.config .\web_rtc_remote.config
adb -s %device1% pull /sdcard/web_rtc_local_params.config ./
copy .\web_rtc_local_params.config .\web_rtc_remote_params.config

cd ..
md %device2%
cd .\%device2%
adb -s %device2% pull /sdcard/web_rtc_local.config ./
copy .\web_rtc_local.config .\web_rtc_remote.config
adb -s %device2% pull /sdcard/web_rtc_local_params.config ./
copy .\web_rtc_local_params.config .\web_rtc_remote_params.config

cd ..
adb -s %device2% push .\%device1%\web_rtc_remote.config /sdcard/
adb -s %device1% push .\%device2%\web_rtc_remote.config /sdcard/
adb -s %device2% push .\%device1%\web_rtc_remote_params.config /sdcard/
adb -s %device1% push .\%device2%\web_rtc_remote_params.config /sdcard/
echo any key exit & pause>nul