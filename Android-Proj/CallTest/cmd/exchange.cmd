set device1=X560Y1ZR58110043
set device2=0123456789ABCDEF
echo %device1% %device2%

adb -s %device1% shell 'rm -f /sdcard/web_rtc_local.config'
adb -s %device2% shell 'rm -f /sdcard/web_rtc_local.config'

md .\%device1%
cd .\%device1%
adb -s %device1% pull /sdcard/web_rtc_local.config ./
copy .\web_rtc_local.config .\web_rtc_remote.config

cd ..
md %device2%
cd .\%device2%
adb -s %device2% pull /sdcard/web_rtc_local.config ./
copy .\web_rtc_local.config .\web_rtc_remote.config

cd ..
adb -s %device2% push .\%device1%\web_rtc_remote.config /sdcard/
adb -s %device1% push .\%device2%\web_rtc_remote.config /sdcard/
echo any key exit & pause>nul