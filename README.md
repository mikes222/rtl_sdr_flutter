# rtl_sdr_flutter

RTL-SDR (Software defined radio) Implementation for flutter.

RTL-SDR is a very cheap dongle that can be used as a computer based radio scanner for receiving live radio signals. Most dongles are based on the RTL2832U chipset. 

Android only currently! PullRequests are welcome!

This implementation accesses directly the USB stick via libusb library built into this plugin. No third-party software needed.


# Features

Direct access of the USB dongle.

Notification of USB device attach/detach event (optional)

optional: trim data packets

optional: calculate amplitudes and send them to flutter instead of I/Q pairs

# Introduction

Still beta version, documentation not yet completed.

Check the example application for the features. Also look into the github repository for the newest version. 

add the following into your ``android/app/src/main/AndroidManifest.xml`` directly in the ``manifest`` tag

    <uses-permission android:name="android.permission.FOREGROUND_SERVICE"/>

    <!-- for recognizing usb events -->
    <uses-feature android:name="android.hardware.usb.host" />

The first one lets the service run even if the app is not running.
The second on allows the service to access the USB Port.

Open your android/app/build.gradle and add the following lines:

        externalNativeBuild {
            cmake {
                // Enabling exceptions, RTTI
                // And setting C++ standard version
                cppFlags '-frtti -fexceptions -std=c++11'

                // Shared runtime for shared libraries
                arguments "-DANDROID_STL=c++_shared"
            }
        }


    externalNativeBuild {
        cmake {
            path "../../../../rtl_sdr_flutter/librtlsdr/CMakeLists.txt"
        }
    }

Note: Find a way to avoid this configuration and create a pullRequest.

Now you can access the device in your flutter widgets:

    final _rtlSdrFlutterPlugin = RtlSdrFlutter();

    List<String> devices = _rtlSdrFlutterPlugin.listDevices();
    _rtlSdrFlutterPlugin.startServer(devices.first);
    _rtlSdrFlutterPlugin.listen(...);

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
