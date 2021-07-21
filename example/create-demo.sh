cordova create demo io.hankers.trtc01 TRTC01
cd demo
cordova platform add ios android
cordova plugin add cordova-plugin-trtc --variable SDKAPPID=1400547367 --searchpath ../../
cp -r ../www .
cordova prepare
