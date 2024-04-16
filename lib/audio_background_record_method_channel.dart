import 'dart:ffi';
import 'dart:async' ;
import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'audio_background_record_platform_interface.dart';

/// An implementation of [AudioBackgroundRecordPlatform] that uses method channels.
class MethodChannelAudioBackgroundRecord extends AudioBackgroundRecordPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('audio_background_record');

  @override
  Future<bool?> startRecording() {
    return methodChannel.invokeMethod("startRecording") ;
  }

  @override
  Future<bool?> stopRecording() {
    return methodChannel.invokeMethod("stopRecording") ;
  }

  @override
  Future<bool?> isRecording() {
    return methodChannel.invokeMethod("isRecording");
  }

  @override
  Future<void> startService(){
     var result  = methodChannel.invokeMethod("startService");
    methodChannel.setMethodCallHandler((call) async {
      if(call.method == "recordStoppedCallBack"){
        if(onRecordStatusChangedCallback!=null){
          onRecordStatusChangedCallback!(call.arguments["status"],call.arguments["error"]) ;
        }
      }
    });
    return result ;
  }

  @override
  Future<void> setConfiguration(String? savetoDirectory,int? maxDurationinMillis, Map<String,String>? texts) {
    return methodChannel.invokeMethod("setConfiguration",
        {"directory": savetoDirectory,"duration":maxDurationinMillis , "notificationText":texts});
  }

  @override
  Future<void> stopService() {
    methodChannel.setMethodCallHandler(null);
    return methodChannel.invokeMethod("stopService");
  }

  @override
  Future<bool?> isServiceRunning() {

    return methodChannel.invokeMethod("isServiceRunning") ;
  }

  @override
  Future<String?> getRecordingDirFromConfig() {
     return methodChannel.invokeMethod("getRecordingDirectory");
  }
  @override
  Future<int?> getMaxRecordDurationFromConfig() {
     return methodChannel.invokeMethod("getMaxRecordDuration");
  }
}
