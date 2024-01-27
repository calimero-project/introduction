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

import tuwien.auto.calimero.KNXException;
import tuwien.auto.calimero.link.KNXNetworkLink;
import tuwien.auto.calimero.link.KNXNetworkLinkUsb;
import tuwien.auto.calimero.link.LinkEvent;
import tuwien.auto.calimero.link.NetworkLinkListener;
import tuwien.auto.calimero.link.medium.TPSettings;
import tuwien.auto.calimero.serial.ConnectionStatus;

/**
 * This example shows how to establish a client network link ({@link KNXNetworkLink}) to a KNX TP1 network using a KNX
 * USB device. Minimum requirements are Calimero version 3.0-SNAPSHOT and Java SE 17 (java.base).
 * <p>
 * You can safely run this example; the established connection is closed 10 seconds after creation.
 * No KNX messages are sent to the KNX network.
 *
 * @author B. Malinowsky
 */
public class CreateUsbLink
{
	/**
	 * Specify your KNX USB device; you can either use the product or manufacturer name, or the USB vendor:product ID.
	 */
	private static final String device = "weinzierl";

	public static void main(final String[] args)
	{
		System.out.println("This example establishes a KNX connection using the KNX USB device '" + device + "'");

		// Create the USB-based link. The network link uses the KNX USB communication protocol. The second argument
		// indicates that the KNX installation uses twisted-pair (TP) medium, with TP1 being most common.
		try (KNXNetworkLink knxLink = new KNXNetworkLinkUsb(device, new TPSettings())) {
			System.out.println("Connection established using KNX USB device " + knxLink.getName());

			// Add a listener with a link event which notifies us in case the USB interface to KNX connection got disrupted.
			// (Note, this is not the connection-state of the USB network link attached to this host itself.)
			knxLink.addLinkListener(new NetworkLinkListener() {
				@LinkEvent
				void status(final ConnectionStatus status) { System.out.println("KNX connection status: " + status); }
			});

			Thread.sleep(10_000);
		}
		catch (KNXException | InterruptedException e) {
			// KNXException: all Calimero-specific checked exceptions are subtypes of KNXException

			// InterruptedException: longer tasks that might block are interruptible, e.g., connection procedures. In
			// such case, an instance of InterruptedException is thrown.
			// If a task got interrupted, Calimero will clean up its internal state and resources accordingly.
			// Any deviation of such behavior, e.g., where not feasible, is documented in the Calimero API.

			System.out.println("Error creating USB network link: " + e);
		}
	}
}
