package com.mschwartz.rtl_sdr_flutter;

import android.os.Binder;

import com.sdrtouch.core.SdrArguments;
import com.sdrtouch.core.devices.SdrDevice;
import com.sdrtouch.tools.Log;

public class SdrBinder extends Binder {

    private SdrService service;

    public SdrBinder(SdrService service) {
        this.service = service;
    }

    public void startWithDevice(SdrDevice sdrDevice, SdrArguments sdrArguments) {
        Log.appendLine("SdrBinder: startWithDevice");
        //sdrDevice.addOnStatusListener(onStatusListener);
        sdrDevice.openAsync(sdrArguments);
        //service.startForeground();
    }

    public void stopWithDevice(SdrDevice device) {
        Log.appendLine("SdrBinder: stopWithDevice");
        device.close();
        //service.stopForeground();
    }


}
