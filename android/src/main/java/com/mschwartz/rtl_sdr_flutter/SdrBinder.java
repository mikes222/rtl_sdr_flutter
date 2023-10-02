package com.mschwartz.rtl_sdr_flutter;

import android.os.Binder;

import com.mschwartz.rtl_sdr_flutter.devices.SdrDevice;

public class SdrBinder extends Binder {

    private final SdrService sdrService;

    public SdrBinder(SdrService service) {
        this.sdrService = service;
    }

    public void startWithDevice(SdrDevice sdrDevice, SdrArguments sdrArguments) {
        sdrService.startWithDevice(sdrDevice, sdrArguments);
    }

    public void stopWithDevice(SdrDevice sdrDevice) {
        sdrService.stopWithDevice(sdrDevice);
    }


}
