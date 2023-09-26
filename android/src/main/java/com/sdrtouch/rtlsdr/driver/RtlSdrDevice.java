/*
 * rtl_tcp_andro is a library that uses libusb and librtlsdr to
 * turn your Realtek RTL2832 based DVB dongle into a SDR receiver.
 * It independently implements the rtl-tcp API protocol for native Android usage.
 * Copyright (C) 2022 by Signalware Ltd <driver@sdrtouch.com>
 *
 * This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.sdrtouch.rtlsdr.driver;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;

import com.mschwartz.rtl_sdr_flutter.StreamHandlerImpl;
import com.sdrtouch.core.SdrArguments;
import com.sdrtouch.core.devices.SdrDevice;
import com.sdrtouch.core.exceptions.SdrException;
import com.sdrtouch.tools.Log;
import com.sdrtouch.tools.UsbPermissionObtainer;

import java.util.concurrent.ExecutionException;

public class RtlSdrDevice extends SdrDevice {
    private final UsbDevice usbDevice;
    private Long nativeHandler;

    private UsbDeviceConnection deviceConnection;

    private final Context context;


    public RtlSdrDevice(Context context, StreamHandlerImpl streamHandler, UsbDevice usbDevice) {
        super(streamHandler);
        this.context = context;
        this.usbDevice = usbDevice;
        this.nativeHandler = initialize();
    }

    @Override
    public void openAsync(final SdrArguments sdrArguments) {
        new Thread() {
            @Override
            public void run() {
                try {
                    int fd = openSessionAndGetFd();
                    String path = usbDevice.getDeviceName();
                    if (!openAsync(nativeHandler, fd, sdrArguments.getGain(), sdrArguments.getSamplerateHz(), sdrArguments.getFrequencyHz(), sdrArguments.getPpm(), path)) {
                        announceOnClosed(new SdrException(SdrException.EXIT_UNKNOWN));
                    } else {
                        announceOnClosed(null);
                    }
                } catch (Throwable e) {
                    announceOnClosed(e);
                }
            }
        }.start();
    }

    @Override
    public void close() {
        close(nativeHandler);
        nativeHandler = null;
    }

    @Override
    public String getName() {
        return "rtl-sdr " + usbDevice.getDeviceName();
    }

    public void setFrequency(long frequency) {
        setFrequency(nativeHandler, frequency);
    }

    public long getFrequency() {
        return getFrequency(nativeHandler);
    }

    public void setSamplingrate(long samplingrate) {
        setSamplingrate(nativeHandler, samplingrate);
    }

    public long getSamplingrate() {
        return getSamplingrate(nativeHandler);
    }

    public void setFrequencyCorrection(int ppm) {
        setFrequencyCorrection(nativeHandler, ppm);
    }

    public int getFrequencyCorrection() {
        return getFrequencyCorrection(nativeHandler);
    }

    public long getRtlXtalFrequency() {
        return getRtlXtalFreq(nativeHandler);
    }

    public long getTunerXtalFrequency() {
        return getTunerXtalFreq(nativeHandler);
    }

    public int getTunergain() {
        return getTunergain(nativeHandler);
    }

    public int getMargin() {
        return getMargin(nativeHandler);
    }

    public void setMargin(int margin) {
        setMargin(nativeHandler, margin);
    }

    public void setTunergainMode(int gain) {
        setTunergainMode(nativeHandler, gain);
    }

    public void setTunergainByPercentage(int tunergain) {
        setTunergainByPercentage(nativeHandler, tunergain);
    }

    public boolean setAmplitude(boolean on) {
        return setAmplitude(nativeHandler, on ? 1 : 0);
    }

    private int openSessionAndGetFd() throws ExecutionException, InterruptedException {
        deviceConnection = UsbPermissionObtainer.obtainFdFor(context, usbDevice).get();
        if (deviceConnection == null) throw new RuntimeException("Could not get a connection");
        int fd = deviceConnection.getFileDescriptor();
        Log.appendLine("Opening fd " + fd);
        return fd;
    }

    @Override
    protected void finalize() throws Throwable {
        if (nativeHandler != null)
            dispose(nativeHandler);
        if (deviceConnection != null)
            deviceConnection.close();
        super.finalize();
    }


    private native long initialize();

    private native void close(long pointer);

    private native void dispose(long pointer);

    private native boolean openAsync(long pointer, int fd, int gain, long samplingrate, long frequency, int ppm, String devicePath) throws Exception;

    private native boolean setFrequency(long pointer, long frequency);

    private native boolean setSamplingrate(long pointer, long samplingrate);

    private native boolean setFrequencyCorrection(long pointer, int ppm);

    private native boolean setTestmode(long pointer, int on);

    private native boolean setAgcMode(long pointer, int on);

    private native boolean setDirectSampling(long pointer, int on);

    private native boolean setOffsetTuning(long pointer, int on);

    private native boolean setRtlXtalFreq(long pointer, long frequency);

    private native boolean setTunerXtalFreq(long pointer, long frequency);

    private native int getTunergain(long pointer);

    private native boolean setTunergainMode(long pointer, int gain);

    private native boolean setTunerGainByIndex(long pointer, int index);

    private native boolean setTunergainByPercentage(long pointer, int percentage);

    private native long getRtlXtalFreq(long pointer);

    private native long getTunerXtalFreq(long pointer);

    private native String getManufacturer(long pointer);

    private native String getProduct(long pointer);

    private native String getSerial(long pointer);

    private native long getFrequency(long pointer);

    private native int getFrequencyCorrection(long pointer);

    private native long getSamplingrate(long pointer);

    private native int getMargin(long pointer);

    private native boolean setMargin(long pointer, int margin);

    private native boolean setAmplitude(long pointer, int on);
}
