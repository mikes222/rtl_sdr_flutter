# rtl_sdr_flutter

RTL-SDR (Software defined radio) Implementation for flutter.

RTL-SDR is a very cheap dongle that can be used as a computer based radio scanner for receiving live radio signals. Most dongles are based on the RTL2832U chipset. 

Android only currently! PullRequests are welcome!

## Introduction

Check the example application for the features.

## Connect device via WLAN manually

First connect the device via usb

Set the port to a defined number

    adb tcpip 5555

Now disconnect from USB and connect via wlan

    adb connect 10.10.20.64:5555

Verify that the device is connected

    adb devices

## Contribution

Highly welcome. Just create Pull requests.
