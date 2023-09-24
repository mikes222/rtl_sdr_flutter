package com.mschwartz.rtl_sdr_flutter;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

import androidx.annotation.NonNull;

import com.sdrtouch.core.SdrArguments;
import com.sdrtouch.core.devices.SdrDevice;
import com.sdrtouch.tools.Check;
import com.sdrtouch.tools.Log;

public class SdrServiceConnection implements ServiceConnection {

    @NonNull
    private final SdrDevice sdrDevice;

    @NonNull
    private final SdrArguments sdrArguments;

    private volatile boolean isBound;

    private UsbBinder binder;

    public  SdrServiceConnection(@NonNull SdrDevice sdrDevice, @NonNull SdrArguments sdrArguments) {
        Check.isNotNull(sdrDevice);
        Check.isNotNull(sdrArguments);
        this.sdrDevice = sdrDevice;
        this.sdrArguments = sdrArguments;
        this.isBound = false;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder ibinder) {
        Log.appendLine("SdrServiceConnection: onServiceConnnected");
        isBound = true;
        binder = (UsbBinder) ibinder;
        binder.startWithDevice(sdrDevice, sdrArguments);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.appendLine("SdrServiceConnection: onServiceDisconnected");
        binder.stopWithDevice(sdrDevice);
        binder = null;
        isBound = false;
    }

    public boolean isBound() {
        return isBound;
    }

    public @NonNull SdrDevice getSdrDevice() {
        return sdrDevice;
    }
}
