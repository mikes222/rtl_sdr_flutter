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
 *  aint with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

class SdrArguments {
  static const int DEFAULT_GAIN = 0;
  static const int DEFAULT_PPM = 0;
  static const int DEFAULT_FREQUENCY = 100000000;
  static const int DEFAULT_SAMPLING_RATE = 2048000;

  /// gain mode: 0=automatic, else gain in /10th of dB, e.g. 24 = 2.4dB
  final int gain;
  final int samplerateHz;
  final int frequencyHz;
  final int ppm;

  SdrArguments(
      {this.gain = DEFAULT_GAIN,
      this.samplerateHz = DEFAULT_SAMPLING_RATE,
      this.frequencyHz = DEFAULT_FREQUENCY,
      this.ppm = DEFAULT_PPM});
}
