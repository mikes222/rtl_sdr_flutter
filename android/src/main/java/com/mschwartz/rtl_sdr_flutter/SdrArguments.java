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

package com.mschwartz.rtl_sdr_flutter;

import java.io.Serializable;

public class SdrArguments implements Serializable {

	private static final long serialVersionUID = -3265233304207469441L;

    private static final int MAX_STRING_LENGTH = 256;

    private static final int DEFAULT_GAIN = 24;
    private static final int DEFAULT_PPM = 0;
    private static final long DEFAULT_FREQUENCY = 100000000;
    private static final long DEFAULT_SAMPLING_RATE = 2048000;

	private final int gain;
	private final long samplerateHz;
    private final long frequencyHz;
	private final int ppm;

    private final int amplitude;

    public SdrArguments(int gain, long samplerateHz, long frequencyHz, int ppm, int amplitude) {
        this.gain = gain;
        this.samplerateHz = samplerateHz;
        this.frequencyHz = frequencyHz;
        this.ppm = ppm;
        this.amplitude = amplitude;
    }

    public int getGain() {
        return gain;
    }

    public long getSamplerateHz() {
        return samplerateHz;
    }

    public long getFrequencyHz() {
        return frequencyHz;
    }

    public int getPpm() {
        return ppm;
    }

    public int getAmplitude() {
        return amplitude;
    }
}
