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

  bool amplitude = false;

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
              Row(
                children: [
                  MaterialButton(
                    onPressed: amplitude
                        ? () async {
                            await _rtlSdrFlutterPlugin.setAmplitude(false);
                            amplitude = false;
                            setState(() {});
                          }
                        : null,
                    child: const Text("Raw I/Q"),
                  ),
                  MaterialButton(
                    onPressed: amplitude
                        ? null
                        : () async {
                            await _rtlSdrFlutterPlugin.setAmplitude(true);
                            amplitude = true;
                            setState(() {});
                          },
                    child: const Text("Amplitude"),
                  ),
                ],
              ),
              if (devices != null)
                ...devices!
                    .map((e) => InkWell(
                          child: Text(
                            e,
                            style: const TextStyle(color: Colors.blue),
                          ),
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
                    if (snapshot.hasError)
                      return Text(
                        "Error ${snapshot.error}",
                        style: const TextStyle(color: Colors.red),
                      );
                    if (!snapshot.hasData)
                      return const Text(
                        "No data",
                        style: TextStyle(color: Colors.blue),
                      );
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

  int ages = 0;

  Widget _getDataWidget(List<int> content, int length) {
    ++ages;
    if (amplitude) {
      return _buildAdsbData(content);
    }
    List<int> normalized = [];
    for (var element in content.getRange(0, length < 1000 ? length : 1000)) {
      normalized.add(element - 127);
    }
    return Column(
      children: [
        Text("read $length bytes, iteration $ages"),
        Text("$normalized"),
      ],
    );
  }

  /// The default implementation of this example uses 1090MHz which is the frequency of the
  /// ADSB transmitters of airplanes. ADSB uses pulse-code modulation so if the demo switches
  /// from I/Q to amplitude-responses we try to find ADSB packets in the data. Of course no complete
  /// implementation is done here since this is only a demo.
  Widget _buildAdsbData(List<int> content) {
    int min = 255;
    int max = 0;
    for (var element in content) {
      if (min > element) min = element;
      if (max < element) max = element;
    }
    // The Mode S preamble is made of impulses of 0.5 microseconds at
    // the following time offsets:
    //
    // 0   - 0.5 usec: first impulse.
    // 1.0 - 1.5 usec: second impulse.
    // 3.5 - 4   usec: third impulse.
    // 4.5 - 5   usec: last impulse.
    //
    // Since we are sampling at 2 Mhz every sample in our magnitude vector
    // is 0.5 usec, so the preamble will look like this, assuming there is
    // an impulse at offset 0 in the array:
    //
    // 0   -----------------
    // 1   -
    // 2   ------------------
    // 3   --
    // 4   -
    // 5   --
    // 6   -
    // 7   ------------------
    // 8   --
    // 9   -------------------
    // 10  --
    // 11  --
    // 12  --
    // 13  --
    // 14  --
    // 15  --
    // After this preamble comes the data block of at most 112 bit
    // 5 bit downlink format
    // 3 bit transponder capability
    // 24 bit A/C Address
    // 56 bit ADS Message
    // 24 bit parity
    // We do analyze only the preamble since this is a demo and not a fully working ADSB receiver.
    //
    // see https://mode-s.org/decode/content/ads-b/1-basics.html
    // see https://www.radartutorial.eu/13.ssr/sr24.en.html
    List<_Adsb> adsbs = [];
    for (int i = 0; i < content.length - 16; ++i) {
      // first quick test if the data are a preamble for the ads-packets
      if (content[i] > content[i + 1] &&
          content[i + 1] < content[i + 2] &&
          content[i + 2] > content[i + 3] &&
          content[i + 4] < content[i + 7] &&
          content[i + 5] < content[i + 7] &&
          content[i + 6] < content[i + 7] &&
          content[i + 7] > content[i + 8] &&
          content[i + 8] < content[i + 9]) {
        // The samples between the two spikes must be < than the average
        // of the high spikes level. We don't test bits too near to
        // the high levels as signals can be out of phase so part of the
        // energy can be in the near samples
        int high = ((content[i + 0] +
                    content[i + 2] +
                    content[i + 7] +
                    content[i + 9]) /
                6)
            .round();

        if (content[i + 4] >= high ||
            content[i + 5] >= high ||
            content[i + 11] >= high ||
            content[i + 12] >= high ||
            content[i + 13] >= high ||
            content[i + 14] >= high) {
          // not a valid preamble
          continue;
        }
        // The next 56 or 112 bit could be the actual message
        _Adsb adsb = _Adsb(index: i);
        adsbs.add(adsb);
        // do not test the rest of the (minimum valid) message for preambles
        i += 16  + 56;
      }
    }
    return Column(
      children: [
        Text(
            "read ${content.length} bytes, iteration $ages, min/max: $min/$max"),
        adsbs.isNotEmpty
            ? Column(
                children: adsbs
                    .map((e) =>
                        Text("Potential ADSB Mode-S at index ${e.index}"))
                    .toList(),
              )
            : Text("$content"),
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

/////////////////////////////////////////////////////////////////////////////

class _Adsb {
  int index;

  _Adsb({required this.index});
}
