package com.mschwartz.rtl_sdr_flutter;

import com.mschwartz.rtl_sdr_flutter.devices.SdrDeviceProvider;
import com.mschwartz.rtl_sdr_flutter.rtlsdrdevice.RtlSdrDeviceProvider;

public class SdrDeviceProviderRegistry {
    public final static SdrDeviceProvider[] SDR_DEVICE_PROVIDERS = new SdrDeviceProvider[] {
            new RtlSdrDeviceProvider(),
            //new HackRfDeviceProvider(),
    };
}
