import 'dart:ffi';

import 'audio_background_record_platform_interface.dart';

class AudioBackgroundRecord {

  static AudioBackgroundRecord? _instance = null ;
  static AudioBackgroundRecord getInstance()
  {
    if(_instance==null){
      _instance = AudioBackgroundRecord._();
    }
    return _instance! ;
  }
  AudioBackgroundRecord._(){} // private constructor

  Future<void> startRecordingService() {
    return  AudioBackgroundRecordPlatform.instance.startService();
  }
  Future<void> stopRecordingService(){
    return  AudioBackgroundRecordPlatform.instance.stopService();
  }
  Future<void> configure({String? savetoDirectory,int? maxDurationInMillis  ,Map<String,String>? text}){
    return AudioBackgroundRecordPlatform.instance.setConfiguration(savetoDirectory,maxDurationInMillis,text);
  }
  Future<bool?> isRecordingServiceRunning(){
    return AudioBackgroundRecordPlatform.instance.isServiceRunning() ;
  }
  Future<String?> getRecordingDestination(){
    return  AudioBackgroundRecordPlatform.instance.getRecordingDirFromConfig() ;
  }
  Future<int?> getMaxRecordDuration(){
    return  AudioBackgroundRecordPlatform.instance.getMaxRecordDurationFromConfig() ;
  }
  Future<bool?> startRecording() {
    return AudioBackgroundRecordPlatform.instance.startRecording();
  }

  Future<bool?> stopRecording() {
    return AudioBackgroundRecordPlatform.instance.stopRecording();
  }

  Future<bool?> isRecording() {
    return AudioBackgroundRecordPlatform.instance.isRecording();
  }
  void setOnRecordStatusChangedCallback(void Function(int status , String? errorMsg)? cb){
    AudioBackgroundRecordPlatform.instance.onRecordStatusChangedCallback = cb;
  }
}
