package com.mschwartz.rtl_sdr_flutter;

import android.os.Binder;

import com.sdrtouch.core.SdrArguments;
import com.sdrtouch.core.devices.SdrDevice;
import com.sdrtouch.tools.Log;

class UsbBinder extends Binder {

    private final UsbService service;

    public UsbListener usbListener;

    UsbBinder(UsbService service) {
        this.service = service;
    }

    public void onUsbDeviceAttached() {
        if (usbListener != null)
            usbListener.onUsbAttached();
    }

    public void onUsbDeviceDetached() {
        if (usbListener != null)
            usbListener.onUsbDetached();
    }

    public void startWithDevice(SdrDevice sdrDevice, SdrArguments sdrArguments) {
        Log.appendLine("UsbBinder: startWithDevice");
        //sdrDevice.addOnStatusListener(onStatusListener);
        sdrDevice.openAsync(sdrArguments);
        //service.startForeground();
    }

    public void stopWithDevice(SdrDevice device) {
        Log.appendLine("UsbBinder: stopWithDevice");
        service.stopForeground();
    }

    /////////////////////////////////////////////////////////////////////////////

    public interface UsbListener {

        public void onUsbAttached();

        public void onUsbDetached();
    }

}
