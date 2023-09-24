import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:rtl_sdr_flutter/rtl_sdr_flutter_method_channel.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  MethodChannelRtlSdrFlutter platform = MethodChannelRtlSdrFlutter();
  const MethodChannel channel = MethodChannel('rtl_sdr_flutter');

  setUp(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger.setMockMethodCallHandler(
      channel,
      (MethodCall methodCall) async {
        return '42';
      },
    );
  });

  tearDown(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger.setMockMethodCallHandler(channel, null);
  });

}
