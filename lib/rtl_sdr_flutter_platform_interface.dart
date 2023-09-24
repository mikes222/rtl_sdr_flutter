import 'package:plugin_platform_interface/plugin_platform_interface.dart';
import 'package:rtl_sdr_flutter/sdrarguments.dart';

import 'rtl_sdr_flutter_method_channel.dart';

abstract class RtlSdrFlutterPlatform extends PlatformInterface {
  /// Constructs a RtlSdrFlutterPlatform.
  RtlSdrFlutterPlatform() : super(token: _token);

  static final Object _token = Object();

  static RtlSdrFlutterPlatform _instance = MethodChannelRtlSdrFlutter();

  /// The default instance of [RtlSdrFlutterPlatform] to use.
  ///
  /// Defaults to [MethodChannelRtlSdrFlutter].
  static RtlSdrFlutterPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [RtlSdrFlutterPlatform] when
  /// they register themselves.
  static set instance(RtlSdrFlutterPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<List<String>?> listDevices() {
    throw UnimplementedError();
  }

  Future<String> startServer(String name, SdrArguments sdrArguments) {
    throw UnimplementedError();
  }

  void setFrequency(int frequency) {
    throw UnimplementedError();
  }

  /// Starts the service to listen to USB events. Will respond if an usb device is attached or detached. This service
  /// may show a notification icon in the notification bar.
  Future<void> startService() {
    throw UnimplementedError();
  }

  /// Stops the service to listen for USB events.
  Future<void> stopService() {
    throw UnimplementedError();
  }

  /// listen for events from the device
  Stream<dynamic> listen() {
    throw UnimplementedError();
  }

  Future<void> stopServer(String name) {
    throw UnimplementedError();
  }

  Future<int> getFrequency() {
    throw UnimplementedError();
  }

  Future<int> getSamplingrate() {
    throw UnimplementedError();
  }

  Future<int> getFrequencyCorrection() {
    throw UnimplementedError();
  }

  Future<int> getRtlXtalFrequency() async {
    throw UnimplementedError();
  }

  Future<int> getTunerXtalFrequency() async {
    throw UnimplementedError();
  }

  Future<int> getTunergain() async {
    throw UnimplementedError();
  }

  Future<int> getMargin() async {
    throw UnimplementedError();
  }

  Future<void> setMargin(int margin) async {
    throw UnimplementedError();
  }

  Future<void> setFrequencyCorrection(int frequencyCorrection) async {
    throw UnimplementedError();
  }

  Future<void> setTunergain(int tunergain) async {
    throw UnimplementedError();
  }
}
