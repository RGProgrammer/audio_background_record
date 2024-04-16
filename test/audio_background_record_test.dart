import 'package:flutter_test/flutter_test.dart';
import 'package:audio_background_record/audio_background_record.dart';
import 'package:audio_background_record/audio_background_record_platform_interface.dart';
import 'package:audio_background_record/audio_background_record_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockAudioBackgroundRecordPlatform
    with MockPlatformInterfaceMixin
    implements AudioBackgroundRecordPlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final AudioBackgroundRecordPlatform initialPlatform = AudioBackgroundRecordPlatform.instance;

  test('$MethodChannelAudioBackgroundRecord is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelAudioBackgroundRecord>());
  });

  test('getPlatformVersion', () async {
    AudioBackgroundRecord audioBackgroundRecordPlugin = AudioBackgroundRecord();
    MockAudioBackgroundRecordPlatform fakePlatform = MockAudioBackgroundRecordPlatform();
    AudioBackgroundRecordPlatform.instance = fakePlatform;

    expect(await audioBackgroundRecordPlugin.getPlatformVersion(), '42');
  });
}
