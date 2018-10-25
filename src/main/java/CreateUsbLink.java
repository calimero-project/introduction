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

import tuwien.auto.calimero.KNXException;
import tuwien.auto.calimero.link.KNXNetworkLink;
import tuwien.auto.calimero.link.KNXNetworkLinkUsb;
import tuwien.auto.calimero.link.medium.TPSettings;

/**
 * This example shows how to establish a client network link ({@link KNXNetworkLink}) to a KNX TP1 network using a KNX
 * USB device. Minimum requirements are Calimero version &ge; 2.4 and Java SE 8 compact1 profile.
 * <p>
 * You can safely run this example; the (established) connection is closed directly afterwards. No KNX messages are sent
 * to the KNX network.
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
		try (KNXNetworkLink knxLink = new KNXNetworkLinkUsb(device, TPSettings.TP1)) {
			System.out.println("Connection established using KNX USB device " + knxLink.getName());
			System.out.println("Link status: connected=" + knxLink.isOpen());
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
