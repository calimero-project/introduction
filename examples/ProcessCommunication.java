/*
    Calimero 2 - A library for KNX network access
    Copyright (c) 2013 B. Malinowsky

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

import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.exception.KNXException;
import tuwien.auto.calimero.link.KNXNetworkLink;
import tuwien.auto.calimero.link.KNXNetworkLinkIP;
import tuwien.auto.calimero.link.medium.TPSettings;
import tuwien.auto.calimero.process.ProcessCommunicator;
import tuwien.auto.calimero.process.ProcessCommunicatorImpl;

/**
 * Example code showing how to use KNX process communication in Calimero. Process communication lets
 * you query and set data points in the KNX network, using a group data point address and the data
 * type of that data point.
 * 
 * @author B. Malinowsky
 */
public class ProcessCommunication
{
	/**
	 * Address of your KNXnet/IP server. Replace the IP address as necessary.
	 */
	private static final String remoteHost = "myKnxServer.myHome.com";

	/**
	 * We will read a boolean from this KNX datapoint group address, replace the address string with
	 * one of yours. Make sure this datapoint exists, otherwise you will get a read timeout!
	 */
	private static final String group = "1/1/1";

	/**
	 * @param args
	 */
	public static void main(final String[] args)
	{
		KNXNetworkLink knxLink = null;
		ProcessCommunicator pc = null;
		try {
			// Create our network link. See other constructors if this one assumes too many
			// default settings.
			knxLink = new KNXNetworkLinkIP(remoteHost, TPSettings.TP1);

			// create a process communicator using that network link
			pc = new ProcessCommunicatorImpl(knxLink);

			System.out.println("read the group value from datapoint " + group);
			// this is a blocking method to read a boolean from a KNX datapoint
			final boolean value = pc.readBool(new GroupAddress(group));
			System.out.println("value read from datapoint " + group + ": " + value);

			// this would write to the KNX datapoint, if you want to write back the same value we
			// just read, uncomment the next line
			// pc.write(group, value);

		}
		catch (final KNXException e) {
			System.out.println("Error reading KNX datapoint: " + e.getMessage());
		}
		catch (final InterruptedException e) {
			System.out.println("Interrupted: " + e.getMessage());
		}
		finally {
			// we don't need the process communicator anymore, detach it from the link
			if (pc != null)
				pc.detach();
			// close the KNX link
			if (knxLink != null)
				knxLink.close();
		}
	}
}
