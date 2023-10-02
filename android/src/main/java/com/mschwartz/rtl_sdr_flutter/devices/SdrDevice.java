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

package com.mschwartz.rtl_sdr_flutter.devices;

import com.mschwartz.rtl_sdr_flutter.MethodHandlerImpl;
import com.mschwartz.rtl_sdr_flutter.StreamHandlerImpl;
import com.mschwartz.rtl_sdr_flutter.SdrArguments;
import com.mschwartz.rtl_sdr_flutter.UsedByJni;
import com.mschwartz.rtl_sdr_flutter.tools.Log;

import java.io.Serializable;

/**
 * The base class for a device. The implementation could be either [HackRfSdrDevice] or [RtlSdrDevice].
 */
public abstract class SdrDevice implements Serializable {
	private static final long serialVersionUID = 6042726358096490615L;
	protected final StreamHandlerImpl streamHandler;

	protected  final MethodHandlerImpl methodhandler;

	protected SdrDevice(StreamHandlerImpl streamHandler, MethodHandlerImpl methodhandler) {
		this.streamHandler = streamHandler;
		this.methodhandler = methodhandler;
	}

	/**
	 * Always call this when the rtl-tcp is no longer running
	 * @param e if the rtl-tcp stopped due to an exception or null if it was successful
	 */
	protected void announceOnClosed(Throwable e) {
		Log.appendLine("SdrDevice: announceOnClosed");
		methodhandler.deviceClosed(this);
		streamHandler.onDeviceClose();
	}
	
	/**
	 * Always call this when the rtl-tcp is ready to accept connections
	 */
	@UsedByJni
	protected void announceOnOpen() {
		streamHandler.onDeviceOpen();
	}

	@UsedByJni
	protected void dataReceived(byte[] data, int dataLength) {
		//Log.appendLine("data: " + dataLength + " bytes");
		streamHandler.sendData(data, dataLength);
	}

	/**
	 * This should return as soon as possible.
	 * Your implementation should notify the listers asynchronously if this has succeeded or not.
	 * This method should not throw an exception!
	 * @param sdrArguments the startup arguments
	 */
	public abstract void openAsync(SdrArguments sdrArguments);
	
	/**
	 * When anyone asks to close the rtl-tcp. This implementation doesn't need to block until the device is closed.
	 * You must also call {@link #announceOnClosed(Throwable)} with a null argument to indicate successful closure.
	 */
	public abstract void close();
	
	/**
	 * Get a friendly name to be displayed to the user
	 */
	public abstract String getName();
	

	public void setFrequency(long frequency) {
		throw new RuntimeException("unimplemented");
	}

	public void setSamplingrate(long samplingrate) {
		throw new RuntimeException("unimplemented");
	}

	public void setGainMode(int gain) {
		throw new RuntimeException("unimplemented");
	}

	public void setFrequencyCorrection(int ppm) {
		throw new RuntimeException("unimplemented");
	}

	public long getFrequency() {
		throw new RuntimeException("unimplemented");
	}

	public long getSamplingrate()  {
		throw new RuntimeException("unimplemented");
	}

	public int getFrequencyCorrection() {
		throw new RuntimeException("unimplemented");
	}

	public long getRtlXtalFrequency() {
		throw new RuntimeException("unimplemented");
	}

	public long getTunerXtalFrequency() {
		throw new RuntimeException("unimplemented");
	}

	public int setTunergainByPercentage() {
		throw new RuntimeException("unimplemented");
	}

	public int getMargin() {
		throw new RuntimeException("unimplemented");
	}

	public void setMargin(int margin) {
		throw new RuntimeException("unimplemented");
	}

	public void setTunergainMode(int tunergain) {
		throw new RuntimeException("unimplemented");
	}

	public int getTunergain() {
		throw new RuntimeException("unimplemented");
	}

	public boolean setAmplitude(boolean on) {
		throw new RuntimeException("unimplemented");
	}
}
