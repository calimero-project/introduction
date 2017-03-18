/*
    Calimero 2 - A library for KNX network access
    Copyright (c) 2013, 2017 B. Malinowsky

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

import java.net.InetSocketAddress;

import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.KNXException;
import tuwien.auto.calimero.link.KNXNetworkLink;
import tuwien.auto.calimero.link.KNXNetworkLinkIP;
import tuwien.auto.calimero.link.medium.TPSettings;
import tuwien.auto.calimero.process.ProcessCommunicator;
import tuwien.auto.calimero.process.ProcessCommunicatorImpl;

/**
 * Example of Calimero process communication, we read (and write) a boolean datapoint in the KNX network. By default,
 * this example will not change any datapoint value in the network.
 */
public class ProcessCommunication
{
	// Address of your KNXnet/IP server. Replace the IP host or address as necessary.
	private static final String remoteHost = "192.168.10.10";

	// We will read a boolean from the KNX datapoint with this group address, replace the address as necessary.
	// Make sure this datapoint exists, otherwise you will get a read timeout!
	private static final String group = "1/0/2";

	public static void main(final String[] args)
	{
		final InetSocketAddress remote = new InetSocketAddress(remoteHost, 3671);
		// Create our network link, and pass it to a process communicator
		try (KNXNetworkLink knxLink = KNXNetworkLinkIP.newTunnelingLink(null, remote, false, TPSettings.TP1);
				ProcessCommunicator pc = new ProcessCommunicatorImpl(knxLink)) {

			System.out.println("read boolean value from datapoint " + group);
			final boolean value = pc.readBool(new GroupAddress(group));
			System.out.println("datapoint " + group + " value = " + value);

			// Uncomment the next line, if you want to write back the same value to the KNX network
			// pc.write(group, value);
		}
		catch (KNXException | InterruptedException e) {
			System.out.println("Error accessing KNX datapoint: " + e.getMessage());
		}
	}
}
