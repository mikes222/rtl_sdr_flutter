import 'package:rtl_sdr_flutter/sdrarguments.dart';

import 'rtl_sdr_flutter_platform_interface.dart';

class RtlSdrFlutter {
  /// Returns a list of all known connected device-names
  Future<List<String>?> listDevices() {
    return RtlSdrFlutterPlatform.instance.listDevices();
  }

  /// Starts the server to listen to the device denoted by [name]
  Future<String> startServer(String name, SdrArguments sdrArguments) {
    return RtlSdrFlutterPlatform.instance.startServer(name, sdrArguments);
  }

  /// Sets the tuner frequency in Hz
  void setFrequency(int frequency) {
    return RtlSdrFlutterPlatform.instance.setFrequency(frequency);
  }

  /// Starts the service to listen to USB events. Will respond if an usb device is attached or detached. This service
  /// may show a notification icon in the notification bar.
  Future<void> startService() {
    return RtlSdrFlutterPlatform.instance.startService();
  }

  /// Stops the service to listen for USB events.
  Future<void> stopService() {
    return RtlSdrFlutterPlatform.instance.stopService();
  }

  /// Listens to the device.
  Stream<dynamic> listen() {
    return RtlSdrFlutterPlatform.instance.listen();
  }

  /// Stops the server
  Future<void> stopServer(String name) {
    return RtlSdrFlutterPlatform.instance.stopServer(name);
  }

  /// Returns the tuner frequency in Hz
  Future<int> getFrequency() {
    return RtlSdrFlutterPlatform.instance.getFrequency();
  }

  /// Returns the samplingrate in Hz. See librtlsdr.c, method rtlsdr_set_sample_rate for more informations
  Future<int> getSamplingrate() {
    return RtlSdrFlutterPlatform.instance.getSamplingrate();
  }

  /// Returns the frequency correction
  Future<int> getFrequencyCorrection() {
    return RtlSdrFlutterPlatform.instance.getFrequencyCorrection();
  }

  Future<int> getRtlXtalFrequency() async {
    return RtlSdrFlutterPlatform.instance.getRtlXtalFrequency();
  }

  Future<int> getTunerXtalFrequency() async {
    return RtlSdrFlutterPlatform.instance.getTunerXtalFrequency();
  }

  /// Returns the gain of the tuner. Zero means automatic gain
  Future<int> getTunergain() async {
    return RtlSdrFlutterPlatform.instance.getTunergain();
  }

  /// Sets the margin for the received data. If margin is not zero, packets will
  /// be trimmed by amplitudes not exceeding the margin (e.g. with margin == 1
  /// the leading and trailing bytes where the value is -1, 0 or +1 will be trimmed).
  /// This could save cpu since the trimming is done in C++
  Future<void> setMargin(int margin) async {
    return RtlSdrFlutterPlatform.instance.setMargin(margin);
  }

  Future<int> getMargin() async {
    return RtlSdrFlutterPlatform.instance.getMargin();
  }

  Future<void> setFrequencyCorrection(int frequencyCorrection) async {
    return RtlSdrFlutterPlatform.instance
        .setFrequencyCorrection(frequencyCorrection);
  }

  /// Sets the gain of the tuner. Zero means automatic gain
  Future<void> setTunergain(int tunergain) async {
    return RtlSdrFlutterPlatform.instance.setTunergain(tunergain);
  }
}
