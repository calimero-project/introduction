/*
    Calimero 2 - A library for KNX network access
    Copyright (c) 2016, 2018 B. Malinowsky

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

import static java.util.stream.Collectors.joining;

import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.usb.UsbDevice;
import javax.usb.UsbDeviceDescriptor;
import javax.usb.UsbDisconnectedException;
import javax.usb.UsbException;

import tuwien.auto.calimero.serial.usb.UsbConnection;

/**
 * This example lists all found KNX USB and USB virtual serial devices. Only devices for KNX communication are listed
 * (if Calimero is able to identify them as such). Minimum requirements are Calimero version &ge; 2.4 and Java SE 8
 * compact1 profile.
 * <p>
 * You can safely run this example, no KNX messages are sent to the KNX network.
 *
 * @author B. Malinowsky
 */
public class DiscoverUsbDevices
{
	public static void main(final String[] args)
	{
		System.out.println("List of KNX USB & USB virtual serial devices");
		System.out.println("KNX USB devices: " + list(UsbConnection.getKnxDevices()));
		System.out.println("KNX serial devices: " + list(UsbConnection.getVirtualSerialKnxDevices()));
	}

	private static String list(final List<UsbDevice> d)
	{
		return d.stream().map(DiscoverUsbDevices::deviceInfo).collect(joining(", "));
	}

	// returns a short string with usb device infos
	private static String deviceInfo(final UsbDevice d)
	{
		String description = "description n/a";
		final UsbDeviceDescriptor dd = d.getUsbDeviceDescriptor();
		try {
			description = truncateAtNull(d.getManufacturerString()) + " - " + truncateAtNull(d.getProductString());
		}
		catch (UnsupportedEncodingException | UsbDisconnectedException | UsbException e) {}
		return String.format("%s [%04x:%04x]", description, dd.idVendor(), dd.idProduct());
	}

	// necessary because usb lib does not correctly cut off C strings at NULL character
	private static String truncateAtNull(final String s)
	{
		final int end = s.indexOf((char) 0);
		return end > -1 ? s.substring(0, end) : s;
	}
}
