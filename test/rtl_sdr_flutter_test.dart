import 'package:flutter_test/flutter_test.dart';
import 'package:rtl_sdr_flutter/rtl_sdr_flutter.dart';
import 'package:rtl_sdr_flutter/rtl_sdr_flutter_platform_interface.dart';
import 'package:rtl_sdr_flutter/rtl_sdr_flutter_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';
import 'package:rtl_sdr_flutter/sdrarguments.dart';

class MockRtlSdrFlutterPlatform
    with MockPlatformInterfaceMixin
    implements RtlSdrFlutterPlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');

  @override
  Future<List<String>?> listDevices() {
    // TODO: implement listDevices
    throw UnimplementedError();
  }

  @override
  Future<String> startServer(String name, SdrArguments sdrArguments) {
    // TODO: implement startServer
    throw UnimplementedError();
  }

  @override
  void setFrequency(int frequency) {
    // TODO: implement setFrequency
  }

  @override
  Stream<Map<String, dynamic>> listen() {
    // TODO: implement listen
    throw UnimplementedError();
  }

  @override
  Future<void> startService() {
    // TODO: implement startService
    throw UnimplementedError();
  }

  @override
  Future<void> stopServer(String name) {
    // TODO: implement stopServer
    throw UnimplementedError();
  }

  @override
  Future<void> stopService() {
    // TODO: implement stopService
    throw UnimplementedError();
  }

  @override
  Future<int> getFrequency() {
    // TODO: implement getFrequency
    throw UnimplementedError();
  }

  @override
  Future<int> getFrequencyCorrection() {
    // TODO: implement getFrequencyCorrection
    throw UnimplementedError();
  }

  @override
  Future<int> getMargin() {
    // TODO: implement getMargin
    throw UnimplementedError();
  }

  @override
  Future<int> getRtlXtalFrequency() {
    // TODO: implement getRtlXtalFrequency
    throw UnimplementedError();
  }

  @override
  Future<int> getSamplingrate() {
    // TODO: implement getSamplingrate
    throw UnimplementedError();
  }

  @override
  Future<int> getTunerXtalFrequency() {
    // TODO: implement getTunerXtalFrequency
    throw UnimplementedError();
  }

  @override
  Future<int> getTunergain() {
    // TODO: implement getTunergain
    throw UnimplementedError();
  }

  @override
  Future<void> setFrequencyCorrection(int frequencyCorrection) {
    // TODO: implement setFrequencyCorrection
    throw UnimplementedError();
  }

  @override
  Future<void> setMargin(int margin) {
    // TODO: implement setMargin
    throw UnimplementedError();
  }

  @override
  Future<void> setTunergain(int tunergain) {
    // TODO: implement setTunergain
    throw UnimplementedError();
  }

  @override
  Future<void> setAmplitude(bool on) {
    // TODO: implement setAmplitude
    throw UnimplementedError();
  }
}

void main() {
  final RtlSdrFlutterPlatform initialPlatform = RtlSdrFlutterPlatform.instance;

  test('$MethodChannelRtlSdrFlutter is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelRtlSdrFlutter>());
  });

  test('getPlatformVersion', () async {
    RtlSdrFlutter rtlSdrFlutterPlugin = RtlSdrFlutter();
    MockRtlSdrFlutterPlatform fakePlatform = MockRtlSdrFlutterPlatform();
    RtlSdrFlutterPlatform.instance = fakePlatform;

  });
}
