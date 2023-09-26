package com.mschwartz.rtl_sdr_flutter;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
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

    /**
     * The startup-arguments
     */
    @NonNull
    private final SdrArguments sdrArguments;

    private SdrBinder binder;

    public  SdrServiceConnection(@NonNull SdrDevice sdrDevice, @NonNull SdrArguments sdrArguments) {
        Check.isNotNull(sdrDevice);
        Check.isNotNull(sdrArguments);
        this.sdrDevice = sdrDevice;
        this.sdrArguments = sdrArguments;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder ibinder) {
        Log.appendLine("SdrServiceConnection: onServiceConnnected");
        binder = (SdrBinder) ibinder;
        binder.startWithDevice(sdrDevice, sdrArguments);
    }

    public void unbind(Context context) {
        binder.stopWithDevice(sdrDevice);
        binder = null;
        context.unbindService(this);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.appendLine("SdrServiceConnection: onServiceDisconnected");
//        binder.stopWithDevice(sdrDevice);
        binder = null;
    }

    public boolean isBound() {
        return binder != null;
    }

    public @NonNull SdrDevice getSdrDevice() {
        return sdrDevice;
    }
}
