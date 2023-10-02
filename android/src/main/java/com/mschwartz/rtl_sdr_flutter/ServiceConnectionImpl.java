package com.mschwartz.rtl_sdr_flutter;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.mschwartz.rtl_sdr_flutter.tools.Log;

class ServiceConnectionImpl implements ServiceConnection {

    private final StreamHandlerImpl streamHandler;

    UsbBinder usbBinder;

    public ServiceConnectionImpl(StreamHandlerImpl streamHandler) {
        Log.appendLine("ServiceConnectionImpl: constructor");
        this.streamHandler = streamHandler;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.appendLine("ServiceConnectionImpl: onServiceConnected");
        usbBinder = (UsbBinder) service;
        usbBinder.usbListener = new UsbBinder.UsbListener() {

            @Override
            public void onUsbAttached() {
                streamHandler.onUsbAttached();
            }

            @Override
            public void onUsbDetached() {
                streamHandler.onUsbDetached();
            }
        };
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

        Log.appendLine("ServiceConnectionImpl: onServiceDisconnected");
        usbBinder = null;
    }

    @Override
    public void onBindingDied(ComponentName name) {
        Log.appendLine("ServiceConnectionImpl: onBindingDied");
        usbBinder = null;
        ServiceConnection.super.onBindingDied(name);
    }

    @Override
    public void onNullBinding(ComponentName name) {
        Log.appendLine("ServiceConnectionImpl: onNullBinding");
        usbBinder = null;
        ServiceConnection.super.onNullBinding(name);
    }

    public void unbind(Context context) {
        usbBinder.usbListener = null;
        usbBinder = null;
        context.unbindService(this);
    }
}
