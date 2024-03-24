/*
    Calimero 2 - A library for KNX network access
    Copyright (c) 2016, 2024 B. Malinowsky

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

import tuwien.auto.calimero.serial.usb.UsbConnectionFactory;

/**
 * This example lists all found KNX USB and USB virtual serial devices. Only devices for KNX communication are listed
 * (if Calimero is able to identify them as such). Minimum requirements are Calimero version 2.6-rc1 and Java SE 17
 * (java.base).
 * <p>
 * You can safely run this example, no KNX messages are sent to the KNX network.
 *
 * @author B. Malinowsky
 */
public class DiscoverUsbDevices {
	public static void main(final String[] args) {
		final var devices = UsbConnectionFactory.attachedKnxUsbDevices();
		System.out.println("List of KNX USB devices:");
		if (devices.isEmpty())
			System.out.println("none found");
		else
			devices.forEach(System.out::println);
	}
}
