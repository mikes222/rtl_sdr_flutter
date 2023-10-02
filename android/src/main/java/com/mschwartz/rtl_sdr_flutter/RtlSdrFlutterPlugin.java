package com.mschwartz.rtl_sdr_flutter;

import static com.mschwartz.rtl_sdr_flutter.SdrDeviceProviderRegistry.SDR_DEVICE_PROVIDERS;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mschwartz.rtl_sdr_flutter.devices.SdrDeviceProvider;
import com.mschwartz.rtl_sdr_flutter.tools.Log;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodChannel;

/**
 * RtlSdrFlutterPlugin
 */
public class RtlSdrFlutterPlugin implements FlutterPlugin, ActivityAware {
    /// The EventChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private MethodChannel methodChannel;

    private EventChannel eventChannel;

    private Context context;

    private BinaryMessenger binaryMessenger;

    @Nullable
    private StreamHandlerImpl streamHandlerImpl;

    private MethodHandlerImpl methodHandlerImpl;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        Log.appendLine("onAttachedToEngine");
        binaryMessenger = flutterPluginBinding.getBinaryMessenger();
        boolean ok = loadNativeLibraries();
        if (!ok) {
            Log.appendLine("loadNativeLibraries failed");
        }
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        Log.appendLine("onDetachedFromEngine");
        binaryMessenger =null;
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        Log.appendLine("onAttachedToActivity");
        context = binding.getActivity().getApplicationContext();
        //activity = binding.getActivity();
        streamHandlerImpl = new StreamHandlerImpl();
        methodHandlerImpl = new MethodHandlerImpl(context, streamHandlerImpl);
        methodChannel = new MethodChannel(binaryMessenger, "rtl_sdr_flutter");
        methodChannel.setMethodCallHandler(methodHandlerImpl);
        eventChannel = new EventChannel(binaryMessenger, "rtl_sdr_flutter_stream");
        eventChannel.setStreamHandler(streamHandlerImpl);
    }

    @Override
    public void onDetachedFromActivity() {
        streamHandlerImpl = null;
        methodHandlerImpl = null;
        methodChannel.setMethodCallHandler(null);
        eventChannel.setStreamHandler(null);
        methodChannel = null;
        eventChannel = null;
        //activity = null;
        context = null;
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {

    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {

    }

    private boolean loadNativeLibraries() {
        for (SdrDeviceProvider sdrDeviceProvider : SDR_DEVICE_PROVIDERS) {
            if (!sdrDeviceProvider.loadNativeLibraries()) {
                return false;
            }
        }
        return true;
    }


}
