package com.mschwartz.rtl_sdr_flutter;

import android.os.Binder;

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

    /////////////////////////////////////////////////////////////////////////////

    public interface UsbListener {

        public void onUsbAttached();

        public void onUsbDetached();
    }

}
