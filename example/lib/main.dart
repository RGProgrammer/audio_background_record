import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:audio_background_record/audio_background_record.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';
  final _audioBackgroundRecordPlugin = AudioBackgroundRecord.getInstance();
  String dir = "";
  int maxRecordDuration = 0;

  String serviceStatus = "checking";
  bool isServiceStarted = false;

  bool isRecording = false;

  @override
  void initState() {
    super.initState();
    _audioBackgroundRecordPlugin.configure(
        maxDurationInMillis: 5000);
    _getServiceStatus();
    _audioBackgroundRecordPlugin.setOnRecordStatusChangedCallback((status, errorMsg) {
      _getServiceStatus(); setState(() {
        print("a message from the callback status = $status");
      });
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
          appBar: AppBar(
            title: const Text('Plugin example app'),
          ),
          body: Center(
            child: Column(
                crossAxisAlignment: CrossAxisAlignment.center,
                mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                mainAxisSize: MainAxisSize.max,
                children: [
                  Text("Directory : $dir"),
                  Text("status : $serviceStatus"),
                  Text("isRecording : $isRecording"),
                  Text("maxDuration  : $maxRecordDuration ms"),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                    children: [
                      ElevatedButton(
                          onPressed: _startService,
                          child: Text("Start Service")),
                      ElevatedButton(
                          onPressed: _stopService, child: Text("Stop Service")),
                    ],
                  ),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                    children: [
                      ElevatedButton(

                          onPressed: isServiceStarted ? _startRecording : null,
                          child: Text("Start Recording")),
                      ElevatedButton(
                          onPressed: isServiceStarted ? _stopRecording : null,
                          child: Text("Stop recording")),
                    ],
                  )
                ]),
          )),
    );
  }

  void _getServiceStatus() async {
    if (mounted) {
      dir = (await _audioBackgroundRecordPlugin.getRecordingDestination())!;

      isRecording = (await _audioBackgroundRecordPlugin.isRecording())!;
      isServiceStarted =
      (await _audioBackgroundRecordPlugin.isRecordingServiceRunning())!;
      serviceStatus = isServiceStarted

          ? "Running"
          : "Stopped";
      maxRecordDuration =
      (await _audioBackgroundRecordPlugin.getMaxRecordDuration())!;
      setState(() {});
    }
  }

  void _startService() async {
    await _audioBackgroundRecordPlugin.startRecordingService();
    _getServiceStatus();
  }

  void _stopService() async {
    await _audioBackgroundRecordPlugin.stopRecordingService();
    _getServiceStatus();
  }

  void _startRecording() async {
    await _audioBackgroundRecordPlugin.startRecording();
    _getServiceStatus();
  }

  void _stopRecording() async {
    await _audioBackgroundRecordPlugin.stopRecording();
    _getServiceStatus();
  }
}
