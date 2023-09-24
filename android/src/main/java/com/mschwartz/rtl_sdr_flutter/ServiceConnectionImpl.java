package com.mschwartz.rtl_sdr_flutter;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

class ServiceConnectionImpl implements ServiceConnection {

    private final StreamHandlerImpl streamHandler;

    UsbBinder usbBinder;

    public ServiceConnectionImpl(StreamHandlerImpl streamHandler) {
        this.streamHandler = streamHandler;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
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
        usbBinder = null;
    }

    @Override
    public void onBindingDied(ComponentName name) {
        usbBinder = null;
        ServiceConnection.super.onBindingDied(name);
    }

    @Override
    public void onNullBinding(ComponentName name) {
        usbBinder = null;
        ServiceConnection.super.onNullBinding(name);
    }
}
