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

package com.mschwartz.rtl_sdr_flutter.rtlsdrdevice;

import com.mschwartz.rtl_sdr_flutter.rtlsdrdevice.enums.RtlSdrExceptionCode;

import com.mschwartz.rtl_sdr_flutter.exceptions.SdrException;
import com.mschwartz.rtl_sdr_flutter.tools.Log;

public class RtlSdrException extends SdrException {
    public RtlSdrException(String exceptionCode) {
        super(RtlSdrExceptionCode.idFromExceptionCode(exceptionCode));
        Log.appendLine("RtlSdrException: "+exceptionCode);
    }
}
