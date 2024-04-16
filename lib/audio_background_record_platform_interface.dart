import 'dart:ffi';

import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'audio_background_record_method_channel.dart';

abstract class AudioBackgroundRecordPlatform extends PlatformInterface {
  /// Constructs a AudioBackgroundRecordPlatform.
  AudioBackgroundRecordPlatform() : super(token: _token);

  static final Object _token = Object();

  static AudioBackgroundRecordPlatform _instance =
      MethodChannelAudioBackgroundRecord();

  /// The default instance of [AudioBackgroundRecordPlatform] to use.
  ///
  /// Defaults to [MethodChannelAudioBackgroundRecord].
  static AudioBackgroundRecordPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [AudioBackgroundRecordPlatform] when
  /// they register themselves.
  static set instance(AudioBackgroundRecordPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

   void Function(int status , String? errorMsg)? _onRecordStatusChangedCallback ;
   set onRecordStatusChangedCallback (void Function(int status , String? errorMsg)? cb ){
     _onRecordStatusChangedCallback = cb ;
   }
  void Function(int status , String? errorMsg)?  get  onRecordStatusChangedCallback => _onRecordStatusChangedCallback ;


  Future<bool?> startRecording() {
    throw UnimplementedError('startRecording() has not been implemented.');
  }

  Future<bool?>  stopRecording() {
    throw UnimplementedError('stopRecording() has not been implemented.');
  }

  Future<bool?> isRecording() {
    throw UnimplementedError('isRecording() has not been implemented.');
  }

  Future<void> startService(){
    throw UnimplementedError('startService() has not been implemented.');
  }

  Future<void> setConfiguration(String? savetoDirectory, int? maxDurationinMillis, Map<String,String>? texts) {
    throw UnimplementedError('setConfiguration has not been implemented.');
  }

  Future<void> stopService() {
    throw UnimplementedError('stopService() has not been implemented.');
  }

  Future<bool?> isServiceRunning() {
    throw UnimplementedError('isServiceRunning() has not been implemented.');
  }

  Future<String?> getRecordingDirFromConfig() {
    throw UnimplementedError('getRecordingDirFromConfig() has not been implemented.');
  }
  Future<int?> getMaxRecordDurationFromConfig() {
    throw UnimplementedError('getMaxRecordDuration() has not been implemented.');
  }

}
