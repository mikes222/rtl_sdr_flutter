import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:rtl_sdr_flutter/sdrarguments.dart';

import 'rtl_sdr_flutter_platform_interface.dart';

/// An implementation of [RtlSdrFlutterPlatform] that uses method channels.
class MethodChannelRtlSdrFlutter extends RtlSdrFlutterPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('rtl_sdr_flutter');

  final streamChannel = const EventChannel('rtl_sdr_flutter_stream');

  @override
  Future<List<String>?> listDevices() async {
    return await methodChannel.invokeListMethod("listDevices");
  }

  @override
  Future<String> startServer(String name, SdrArguments sdrArguments) async {
    Map map = {};
    map["frequencyHz"] = sdrArguments.frequencyHz;
    map["gain"] = sdrArguments.gain;
    map["ppm"] = sdrArguments.ppm;
    map["samplerateHz"] = sdrArguments.samplerateHz;
    return await methodChannel.invokeMethod("startServer", [name, map]);
  }

  @override
  void setFrequency(int frequency) {
    methodChannel.invokeMethod("setFrequency", frequency);
  }

  @override
  Future<void> startService() async {
    await methodChannel.invokeMethod("startService");
  }

  @override
  Future<void> stopService() async {
    await methodChannel.invokeMethod("stopService");
  }

  @override
  Stream<Map<String, dynamic>> listen() {
    return streamChannel.receiveBroadcastStream().map<Map<String, dynamic>>((event) => event);
  }

  @override
  Future<void> stopServer(String name) async {
    await methodChannel.invokeMethod("stopServer", [name]);
  }

  @override
  Future<int> getFrequency() async {
    return await methodChannel.invokeMethod("getFrequency");
  }

  @override
  Future<int> getSamplingrate() async {
    return await methodChannel.invokeMethod("getSamplingrate");
  }

  @override
  Future<int> getFrequencyCorrection() async {
    return await methodChannel.invokeMethod("getFrequencyCorrection");
  }

  @override
  Future<int> getRtlXtalFrequency() async {
    return await methodChannel.invokeMethod("getRtlXtalFrequency");
  }

  @override
  Future<int> getTunerXtalFrequency() async {
    return await methodChannel.invokeMethod("getTunerXtalFrequency");
  }

  @override
  Future<int> getTunergain() async {
    return await methodChannel.invokeMethod("getTunergain");
  }

  @override
  Future<int> getMargin() async {
    return await methodChannel.invokeMethod("getMargin");
  }

  @override
  Future<void> setMargin(int margin) async {
    await methodChannel.invokeMethod("setMargin", margin);
  }

  @override
  Future<void> setFrequencyCorrection(int frequencyCorrection) async {
    await methodChannel.invokeMethod("setFrequencyCorrection", frequencyCorrection);
  }

  @override
  Future<void> setTunergain(int tunergain) async {
    await methodChannel.invokeMethod("setTunergain", tunergain);
  }

  @override
  Future<void> setAmplitude(bool on) async {
    await methodChannel.invokeMethod("setAmplitude", on ? 1 : 0);
  }
}
