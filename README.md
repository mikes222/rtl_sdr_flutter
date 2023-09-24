# rtl_sdr_flutter

RTL-SDR Implementation for flutter

## Connect device via WLAN manually

First connect the device via usb

Set the port to a defined number

    adb tcpip 5555

Now disconnect from USB and connect via wlan

    adb connect 10.10.20.64:5555

Verify that the device is connected

    adb devices

## Getting Started

This project is a starting point for a Flutter
[plug-in package](https://flutter.dev/developing-packages/),
a specialized package that includes platform-specific implementation code for
Android and/or iOS.

For help getting started with Flutter development, view the
[online documentation](https://flutter.dev/docs), which offers tutorials,
samples, guidance on mobile development, and a full API reference.

