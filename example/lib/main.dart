import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:rtl_sdr_flutter/rtl_sdr_flutter.dart';
import 'package:rtl_sdr_flutter/sdrarguments.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  final _rtlSdrFlutterPlugin = RtlSdrFlutter();

  List<String>? devices;

  int? frequency;

  int? rtlXtalFrequency;

  int? tunerXtalFrequency;

  /// aka ppm
  int? frequencyCorrection;

  int? samplingrate;

  int? tunergain;

  int? margin;

  String? error;

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    try {
      devices = await _rtlSdrFlutterPlugin.listDevices();
      error = null;
    } on PlatformException catch (error, stacktrace) {
      devices = null;
      this.error = error.message;
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {});
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: SingleChildScrollView(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              if (error != null)
                Text(error!, style: const TextStyle(color: Colors.red)),
              Row(
                children: [
                  Text("Frequency: $frequency"),
                  MaterialButton(
                    onPressed: () async {
                      frequency = await _rtlSdrFlutterPlugin.getFrequency();
                      setState(() {});
                    },
                    child: const Text("Read"),
                  ),
                ],
              ),
              Row(
                children: [
                  Text("RtlXtalFrequency (Hz): $rtlXtalFrequency"),
                  MaterialButton(
                    onPressed: () async {
                      rtlXtalFrequency =
                          await _rtlSdrFlutterPlugin.getRtlXtalFrequency();
                      setState(() {});
                    },
                    child: const Text("Read"),
                  ),
                ],
              ),
              Row(
                children: [
                  Text("TunerXtalFrequency (Hz): $tunerXtalFrequency"),
                  MaterialButton(
                    onPressed: () async {
                      tunerXtalFrequency =
                          await _rtlSdrFlutterPlugin.getTunerXtalFrequency();
                      setState(() {});
                    },
                    child: const Text("Read"),
                  ),
                ],
              ),
              Row(
                children: [
                  Text("Frequency correction (ppm): $frequencyCorrection"),
                  MaterialButton(
                    onPressed: () async {
                      frequencyCorrection =
                          await _rtlSdrFlutterPlugin.getFrequencyCorrection();
                      setState(() {});
                    },
                    child: const Text("Read"),
                  ),
                ],
              ),
              Row(
                children: [
                  MaterialButton(
                    onPressed: () async {
                      await _rtlSdrFlutterPlugin.setFrequencyCorrection(-50);
                    },
                    child: const Text("Write -50"),
                  ),
                  MaterialButton(
                    onPressed: () async {
                      await _rtlSdrFlutterPlugin.setFrequencyCorrection(50);
                    },
                    child: const Text("Write 50"),
                  ),
                ],
              ),
              const Text(
                  "Low cost RTL-SDR DVB-T USB dongle uses a 28.8MHz crystal and the frequency that it generates is not exactly at 28.8MHz but at a slight offset. Deviation up to 90ppm has been reported. Software that uses RTL-SDR as receiver has a frequency ppm setting to adjust for this deviation.",
                  style: TextStyle(fontSize: 10)),
              Row(
                children: [
                  Text("Tunergain (1/10 dB): $tunergain"),
                  MaterialButton(
                    onPressed: () async {
                      tunergain = await _rtlSdrFlutterPlugin.getTunergain();
                      setState(() {});
                    },
                    child: const Text("Read"),
                  ),
                ],
              ),
              Row(
                children: [
                  MaterialButton(
                    onPressed: () async {
                      await _rtlSdrFlutterPlugin.setTunergain(0);
                    },
                    child: const Text("Write Auto"),
                  ),
                  MaterialButton(
                    onPressed: () async {
                      await _rtlSdrFlutterPlugin.setTunergain(40);
                    },
                    child: const Text("Write 4.0dB"),
                  ),
                ],
              ),
              Row(
                children: [
                  Text("Sampling rate: $samplingrate"),
                  MaterialButton(
                    onPressed: () async {
                      samplingrate =
                          await _rtlSdrFlutterPlugin.getSamplingrate();
                      setState(() {});
                    },
                    child: const Text("Read"),
                  ),
                ],
              ),
              Row(
                children: [
                  Text("Margin: $margin"),
                  MaterialButton(
                    onPressed: () async {
                      margin = await _rtlSdrFlutterPlugin.getMargin();
                      setState(() {});
                    },
                    child: const Text("Read"),
                  ),
                  MaterialButton(
                    onPressed: () async {
                      await _rtlSdrFlutterPlugin.setMargin(3);
                    },
                    child: const Text("Write 3"),
                  ),
                  MaterialButton(
                    onPressed: () async {
                      await _rtlSdrFlutterPlugin.setMargin(20);
                    },
                    child: const Text("Write 20"),
                  ),
                ],
              ),
              const Text(
                  "If margin is not zero, packets will be trimmed by amplitudes not exceeding the margin (e.g. with margin == 1 the leading and trailing bytes where the value is -1, 0 or +1 will be trimmed). This could save cpu since the trimming is done in C++",
                  style: TextStyle(fontSize: 10)),
              if (devices != null)
                ...devices!
                    .map((e) => InkWell(
                          child: Text(e),
                          onTap: () {
                            start(e);
                          },
                        ))
                    .toList(),
              MaterialButton(
                  onPressed: () {
                    initPlatformState();
                  },
                  child: const Text("List devices")),
              MaterialButton(
                onPressed: () {
                  _rtlSdrFlutterPlugin.stopServer(devices?.first ?? "unknown");
                },
                child: const Text("Stop server"),
              ),
              Row(
                children: [
                  MaterialButton(
                    onPressed: () {
                      _rtlSdrFlutterPlugin.startService();
                    },
                    child: const Text("Start service"),
                  ),
                  MaterialButton(
                    onPressed: () {
                      _rtlSdrFlutterPlugin.stopService();
                    },
                    child: const Text("Stop service"),
                  ),
                ],
              ),
              StreamBuilder(
                  stream: _rtlSdrFlutterPlugin.listen(),
                  builder:
                      (BuildContext context, AsyncSnapshot<dynamic> snapshot) {
                    if (snapshot.hasError) return const Text("Error");
                    if (!snapshot.hasData) return const Text("Empty");
                    Map<dynamic, dynamic> data = snapshot.data;
                    if (!data.containsKey("event")) {
                      return Text("${snapshot.data}");
                    }
                    switch (data["event"]) {
                      case "UsbAttached":
                        return const Text("USB attached");
                      case "UsbDetached":
                        return const Text("USB detached");
                      case "DeviceOpen":
                        return const Text("Device opened");
                      case "DeviceClose":
                        return const Text("Device closing");
                      case "Data":
                        return _getDataWidget(data["content"], data["length"]);
                      default:
                        return Text("Unknown command ${data["event"]}");
                    }
                  }),
            ],
          ),
        ),
      ),
    );
  }

  List<int> normalized = [];

  int ages = 0;

  Widget _getDataWidget(List<int> content, int length) {
    ++ages;
    normalized.clear();
    for (var element in content) {
      normalized.add(element - 127);
    }
    return Column(
      children: [
        Text("read $length ${content.length} bytes, iteration $ages"),
        Text("$normalized"),
      ],
    );
  }

  void start(e) {
    try {
      SdrArguments sdrArguments =
          SdrArguments(frequencyHz: 1090000000, samplerateHz: 2000000);
      // SdrArguments sdrArguments = SdrArguments(
      //     frequencyHz: 868000000, samplerateHz: 2000000);
      _rtlSdrFlutterPlugin.startServer(e, sdrArguments);
    } catch (error, stacktrace) {
      this.error = error.toString();
    }
  }
}
