# rtl_sdr_flutter

RTL-SDR (Software defined radio) Implementation for flutter.

RTL-SDR is a very cheap dongle that can be used as a computer based radio scanner for receiving live radio signals. Most dongles are based on the RTL2832U chipset. 

This implementation accesses directly the USB stick via libusb library built into this plugin. No third-party software needed.

Android only currently! PullRequests are welcome!

# Features

 - Direct access of the USB dongle. No RTL-TCP, no external programs needed.
 - Notification of USB device attach/detach event
 - optionally trim data packets, send only parts of the data and remove preceding/trailing noise
 - optionally calculate amplitudes and send them to flutter instead of I/Q pairs (half the payload)
 - Set gain, ppm, samplefrequency and a bunch of other properties of the SDR

# Introduction

Check the example application for the features. Also look into the github repository for the newest version. 

add the following into your ``android/app/src/main/AndroidManifest.xml`` directly in the ``manifest`` tag

    <uses-permission android:name="android.permission.FOREGROUND_SERVICE"/>

    <!-- for recognizing usb events -->
    <uses-feature android:name="android.hardware.usb.host" />

The first one lets the service run even if the app is not running.
The second on allows the service to access the USB Port.

    externalNativeBuild {
        cmake {
            path "../../../../rtl_sdr_flutter/librtlsdr/CMakeLists.txt"
        }
    }

Note: Find a way to avoid this configuration and create a pullRequest.

For production use add the following line into ``android/app/proguard-rules.pro``

    -keep class com.mschwartz.rtl_sdr_flutter.tools.**  { *; }

Now you can access the device in your flutter widgets:

    final _rtlSdrFlutterPlugin = RtlSdrFlutter();

    List<String> devices = _rtlSdrFlutterPlugin.listDevices();
    _rtlSdrFlutterPlugin.startServer(devices.first);
    _rtlSdrFlutterPlugin.listen(...);

A sample widget implementation could look like this:

    StreamBuilder(
      stream: _rtlSdrFlutterPlugin.listen(),
      builder:
          (BuildContext context, AsyncSnapshot<dynamic> snapshot) {
        if (snapshot.hasError) {
          return Text(
            "Error ${snapshot.error}",
            style: const TextStyle(color: Colors.red),
          );
        }
        if (!snapshot.hasData) {
          return const Text(
            "No data",
            style: TextStyle(color: Colors.blue),
          );
        }
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
            return Text("read ${data["length"]} bytes");
          default:
            return Text("Unknown command ${data["event"]}");
        }
      }),

## Future improvements

- read name and manufacturer of device (already partially implemented)
- iOS implementation (relying on the audience to do that)
- Test and support for more devices (HackRF)
- Find ways to save even more cpu since there are a lot of data to process
- Maybe some quick pause/resume command to pause processing if the app decides it does not need to listen for a moment.

## For debugging purposes: Connect device via WLAN manually

First connect the device via usb

Set the port to a defined number

    adb tcpip 5555

Now disconnect from USB and connect via wlan

    adb connect 10.10.20.64:5555

Verify that the device is connected

    adb devices

## Contribution

Highly welcome. Just create Pull requests.
