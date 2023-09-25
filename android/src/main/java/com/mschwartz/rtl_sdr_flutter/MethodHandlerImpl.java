package com.mschwartz.rtl_sdr_flutter;

import static com.sdrtouch.rtlsdr.SdrDeviceProviderRegistry.SDR_DEVICE_PROVIDERS;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sdrtouch.core.SdrArguments;
import com.sdrtouch.core.devices.SdrDevice;
import com.sdrtouch.core.devices.SdrDeviceProvider;
import com.sdrtouch.tools.Log;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;

class MethodHandlerImpl implements MethodCallHandler {

    @NonNull
    private final Context context;

    @Nullable
    private SdrServiceConnection mConnection;

    private ServiceConnectionImpl serviceConnection;

    private final StreamHandlerImpl streamHandler;


    MethodHandlerImpl(@NonNull Context context, StreamHandlerImpl streamHandler) {
        this.context = context;
        this.streamHandler = streamHandler;
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull MethodChannel.Result result) {
        try {
            switch (call.method) {
                case "listDevices": {
                    List<SdrDevice> devices = listDevices();
                    List<String> names = new LinkedList<>();
                    for (SdrDevice device : devices) {
                        names.add(device.getName());
                    }
                    result.success(names);
                    break;
                }
                case "startService": {
//                    Intent intent = new Intent(context, UsbService.class);
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                        context.startForegroundService(intent);
//                    } else {
//                        context.startService(intent);
//                    }
                    //context.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
                    if (serviceConnection != null) {
                        result.error("hin11", "proble", "proble");
                        return;
                    }
                    Intent intent = new Intent(context, UsbService.class);
                    serviceConnection = new ServiceConnectionImpl(streamHandler);
                    context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

                    result.success(null);
                    break;
                }
                case "stopService": {
//                    Intent intent = new Intent(context, UsbService.class);
//                    context.stopService(intent);
                    if (serviceConnection == null) {
                        result.error("hin11", "proble", "proble");
                        return;
                    }
                    Intent intent = new Intent(context, UsbService.class);
                    context.unbindService(serviceConnection);
                    serviceConnection = null;
                    result.success(null);
                    break;
                }
                case "startServer": {
                    if (mConnection != null) {
                        result.error("hin2", "proble", "proble");
                        return;
                    }
                    ArrayList args = (ArrayList) call.arguments;
                    String name = (String) args.get(0);
                    Map<String, Object> map = (Map<String, Object>) args.get(1);
                    SdrArguments sdrArguments = new SdrArguments((int) map.get("gain"), (int) map.get("samplerateHz"), (int) map.get("frequencyHz"), (int) map.get("ppm"));
                    SdrDevice device = null;
                    List<SdrDevice> devices = listDevices();
                    for (SdrDevice d : devices) {
                        if (d.getName().equals(name)) {
                            device = d;
                            break;
                        }
                    }
                    if (device == null) {
                        result.error("hin3", "proble", "proble");
                        return;
                    }
                    startServer(device, sdrArguments);
                    result.success("OK");
                    break;
                }
                case "stopServer": {
                    if (mConnection == null) {
                        result.error("hin4", "proble", "proble");
                        return;
                    }
                    stopServer();
                    result.success("OK");
                    break;
                }
                case "setFrequency": {
                    if (mConnection == null) {
                        result.error("hin5", "proble", "proble");
                        return;
                    }
                    long frequency = (long) call.arguments;
                    mConnection.getSdrDevice().setFrequency(frequency);
                    result.success("OK");
                    break;
                }
                case "setFrequencyCorrection": {
                    if (mConnection == null) {
                        result.error("hin5", "proble", "proble");
                        return;
                    }
                    int ppm = (int) call.arguments;
                    mConnection.getSdrDevice().setFrequencyCorrection(ppm);
                    result.success("OK");
                    break;
                }
                case "getFrequencyCorrection": {
                    if (mConnection == null) {
                        result.error("hin6", "proble", "proble");
                        return;
                    }
                    int frequency = mConnection.getSdrDevice().getFrequencyCorrection();
                    result.success(frequency);
                    break;
                }
                case "setSamplingrate": {
                    if (mConnection == null) {
                        result.error("hin6", "proble", "proble");
                        return;
                    }
                    long samplerate = (long) call.arguments;
                    mConnection.getSdrDevice().setSamplingrate(samplerate);
                    result.success("OK");
                    break;
                }
                case "setGainMode": {
                    if (mConnection == null) {
                        result.error("hin5", "proble", "proble");
                        return;
                    }
                    int gainMode = (int) call.arguments;
                    mConnection.getSdrDevice().setGainMode(gainMode);
                    result.success("OK");
                    break;
                }
                case "getFrequency": {
                    if (mConnection == null) {
                        result.error("hin6", "proble", "proble");
                        return;
                    }
                    long frequency = mConnection.getSdrDevice().getFrequency();
                    result.success(frequency);
                    break;
                }
                case "getRtlXtalFrequency": {
                    if (mConnection == null) {
                        result.error("hin6", "proble", "proble");
                        return;
                    }
                    long frequency = mConnection.getSdrDevice().getRtlXtalFrequency();
                    result.success(frequency);
                    break;
                }
                case "getTunerXtalFrequency": {
                    if (mConnection == null) {
                        result.error("hin6", "proble", "proble");
                        return;
                    }
                    long frequency = mConnection.getSdrDevice().getTunerXtalFrequency();
                    result.success(frequency);
                    break;
                }
                case "getSamplingrate": {
                    if (mConnection == null) {
                        result.error("hin6", "proble", "proble");
                        return;
                    }
                    long frequency = mConnection.getSdrDevice().getSamplingrate();
                    result.success(frequency);
                    break;
                }
                case "getTunergain": {
                    if (mConnection == null) {
                        result.error("hin6", "proble", "proble");
                        return;
                    }
                    int frequency = mConnection.getSdrDevice().getTunergain();
                    result.success(frequency);
                    break;
                }
                case "setTunergain": {
                    if (mConnection == null) {
                        result.error("hin6", "proble", "proble");
                        return;
                    }
                    int gainMode = (int) call.arguments;
                    mConnection.getSdrDevice().setTunergainMode(gainMode);
                    result.success("OK");
                    break;
                }
                case "getMargin": {
                    if (mConnection == null) {
                        result.error("hin6", "proble", "proble");
                        return;
                    }
                    int frequency = mConnection.getSdrDevice().getMargin();
                    result.success(frequency);
                    break;
                }
                case "setMargin": {
                    if (mConnection == null) {
                        result.error("hin6", "proble", "proble");
                        return;
                    }
                    int gainMode = (int) call.arguments;
                    mConnection.getSdrDevice().setMargin(gainMode);
                    result.success("OK");
                    break;
                }
                case "setAmplitude": {
                    if (mConnection == null) {
                        result.error("hin6", "proble", "proble");
                        return;
                    }
                    int gainMode = (int) call.arguments;
                    mConnection.getSdrDevice().setAmplitude(gainMode > 0);
                    result.success("OK");
                    break;
                }
                default:
                    result.notImplemented();
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.error("generalException", e.getMessage(), e.getLocalizedMessage());
        }

    }

    private List<SdrDevice> listDevices() {
        List<SdrDevice> availableSdrDevices = new ArrayList<>();
        for (SdrDeviceProvider sdrDeviceProvider : SDR_DEVICE_PROVIDERS) {
            List<SdrDevice> devicesForProvider = sdrDeviceProvider.listDevices(context, streamHandler, false);
            availableSdrDevices.addAll(devicesForProvider);
            Log.appendLine("%s: found %d device opening options", sdrDeviceProvider.getName(), devicesForProvider.size());
        }
        return availableSdrDevices;
    }

    public void startServer(final SdrDevice sdrDevice, SdrArguments sdrArguments) {
        try {
            //start the service
            mConnection = new SdrServiceConnection(sdrDevice, sdrArguments);
            Intent intent = new Intent(context, UsbService.class);
            context.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            mConnection = null;
            throw e;
        }
    }

    public void stopServer() {
        if (mConnection != null && mConnection.isBound()) {
            context.unbindService(mConnection);
        }
        mConnection = null;
    }

}
