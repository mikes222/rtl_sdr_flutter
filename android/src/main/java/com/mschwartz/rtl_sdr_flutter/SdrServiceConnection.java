package com.mschwartz.rtl_sdr_flutter;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;

import androidx.annotation.NonNull;

import com.mschwartz.rtl_sdr_flutter.devices.SdrDevice;
import com.mschwartz.rtl_sdr_flutter.tools.Check;
import com.mschwartz.rtl_sdr_flutter.tools.Log;

public class SdrServiceConnection implements ServiceConnection {

    @NonNull
    private final SdrDevice sdrDevice;

    /**
     * The startup-arguments
     */
    @NonNull
    private final SdrArguments sdrArguments;

    private SdrBinder binder;

    public SdrServiceConnection(@NonNull SdrDevice sdrDevice, @NonNull SdrArguments sdrArguments) {
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
        Log.appendLine("SdrServiceConnection: unbind");
        if (binder != null)
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

    public void announceOnClosed(Throwable e) {

    }
}
