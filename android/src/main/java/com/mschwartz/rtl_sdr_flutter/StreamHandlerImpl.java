package com.mschwartz.rtl_sdr_flutter;

import android.os.Handler;
import android.os.Looper;

import com.sdrtouch.tools.Log;

import java.util.HashMap;
import java.util.Map;

import io.flutter.plugin.common.EventChannel;

public class StreamHandlerImpl implements EventChannel.StreamHandler {

    private EventChannel.EventSink eventSink;

    private final Handler uiThreadHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onListen(Object arguments, EventChannel.EventSink events) {
        Log.appendLine("StreamHandlerImpl: Start listening");
        uiThreadHandler.post(() -> eventSink = events);
    }

    @Override
    public void onCancel(Object arguments) {
        Log.appendLine("StreamHandlerImpl: Stop listening");
        uiThreadHandler.post(() -> eventSink = null);
    }

    public void sendData(byte[] data, int dataLength) {
        if (eventSink == null) return;
        Map<String, Object> map = new HashMap<>();
        map.put("event", "Data");
        map.put("content", data);
        map.put("length", dataLength);
        uiThreadHandler.post(() -> {
            if (eventSink != null) eventSink.success(map);
        });
    }

    /// sends a notification to flutter if a new device is attached.
    /// [MethodHandlerImpl] binds the UsbService, the UsbService will create a new instance of
    /// UsbBinder and the ServiceConntectionImpl which is created by [MethodHandlerImpl] will
    /// receive the binder and forward the notification to this method.
    public void onUsbAttached() {
        if (eventSink == null) return;
        Map<String, Object> map = new HashMap<>();
        map.put("event", "UsbAttached");
       uiThreadHandler.post(() -> {
          if (eventSink != null) eventSink.success(map);
       });
    }

    public void onUsbDetached() {
        if (eventSink == null) return;
        Map<String, Object> map = new HashMap<>();
        map.put("event", "UsbDetached");
       uiThreadHandler.post(() -> {
          if (eventSink != null) eventSink.success(map);
       });
    }

    public void onDeviceOpen() {
        if (eventSink == null) return;
        Map<String, Object> map = new HashMap<>();
        map.put("event", "DeviceOpen");
       uiThreadHandler.post(() -> {
          if (eventSink != null) eventSink.success(map);
       });
    }

    public void onDeviceClose() {
        if (eventSink == null) return;
        Map<String, Object> map = new HashMap<>();
        map.put("event", "DeviceClose");
       uiThreadHandler.post(() -> {
          if (eventSink != null) eventSink.success(map);
       });
    }

}
