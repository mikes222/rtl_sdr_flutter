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

package com.mschwartz.rtl_sdr_flutter.hackrfdevice;

import android.content.Context;
import android.hardware.usb.UsbDevice;

import com.mantz_it.hackrf_android.Hackrf;
import com.mantz_it.hackrf_android.HackrfCallbackInterface;
import com.mantz_it.hackrf_android.HackrfUsbException;
import com.mschwartz.rtl_sdr_flutter.MethodHandlerImpl;
import com.mschwartz.rtl_sdr_flutter.StreamHandlerImpl;
import com.mschwartz.rtl_sdr_flutter.SdrArguments;
import com.mschwartz.rtl_sdr_flutter.devices.SdrDevice;
import com.mschwartz.rtl_sdr_flutter.tools.Log;

import java.io.IOException;

public class HackRfSdrDevice extends SdrDevice {
    private final static double BUFF_TIME = 0.1;
    private final UsbDevice device;

    private Thread processingThread;
    private volatile HackRfTcp tcp;

    private final Context context;

    public HackRfSdrDevice(Context context, StreamHandlerImpl streamHandler, MethodHandlerImpl methodhandler, UsbDevice device) {
        super(streamHandler, methodhandler);
        this.device = device;
        this.context = context;
    }

    @Override
    public void openAsync(final SdrArguments sdrArguments) {
        processingThread = new Thread() {
            @Override
            public void run() {
                int queue_size = (int) (2 * BUFF_TIME * sdrArguments.getSamplerateHz());
                try {
                    Log.appendLine("Opening HackRF");
                    Hackrf.initHackrf(context, device, new HackrfCallbackInterface() {
                        @Override
                        public void onHackrfReady(Hackrf hackrf) {
                            try {
                                Log.appendLine("HackRF ready");
                                tcp = new HackRfTcp(hackrf, sdrArguments);

                                Log.appendLine("Initialising TCP");
                                tcp.initDevice();
                                tcp.prepareToAcceptConnections();

                                // ready to accept connections
                                announceOnOpen();
                                tcp.serveAndBlock();

                                // close device
                                Log.appendLine("Closing HackRF");
                                hackrf.stop();
                                announceOnClosed(null);
                            } catch (Exception e) {
                                announceOnClosed(e);
                                if (hackrf != null) {
                                    try {
                                        Log.appendLine("Closing HackRF due to "+e.getMessage());
                                        hackrf.stop();
                                    } catch (HackrfUsbException ee) {
                                        Log.appendLine("Failed to close HackRF due to "+ee.getMessage());
                                    }
                                }
                            }
                        }

                        @Override
                        public void onHackrfError(String message) {
                            announceOnClosed(new IOException(message));
                        }
                    }, queue_size);
                } catch (Exception e) {
                    announceOnClosed(e);
                } finally {
                    processingThread = null;
                }
                Log.appendLine("HackRF device thread finished.");
            }
        };
        processingThread.start();
    }

    @Override
    public void close() {
        if (tcp != null) {
            tcp.close();
        }
        if (processingThread != null && processingThread.isAlive()) {
            processingThread.interrupt();
            try {
                Log.appendLine("Attempting to join HackRF thread");
                processingThread.join();
                Log.appendLine("HackRF thread is dead");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String getName() {
        return "HackRF";
    }
}
