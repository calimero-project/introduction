/*
    Calimero 2 - A library for KNX network access
    Copyright (c) 2015, 2016 B. Malinowsky

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

import java.time.LocalTime;

import tuwien.auto.calimero.DataUnitBuilder;
import tuwien.auto.calimero.DetachEvent;
import tuwien.auto.calimero.exception.KNXException;
import tuwien.auto.calimero.link.KNXNetworkLink;
import tuwien.auto.calimero.link.KNXNetworkLinkIP;
import tuwien.auto.calimero.link.medium.TPSettings;
import tuwien.auto.calimero.process.ProcessCommunicator;
import tuwien.auto.calimero.process.ProcessCommunicatorImpl;
import tuwien.auto.calimero.process.ProcessEvent;
import tuwien.auto.calimero.process.ProcessListenerEx;

/**
 * Example code showing how to use KNX process communication for group monitoring on a KNX Twisted
 * Pair 1 (TP1) network. On receiving group notifications, the KNX source and destination address
 * are printed to System.out, as well as any data part of the application service data unit (ASDU)
 * in hexadecimal format.
 * <p>
 * Requires calimero-core (master branch) and JSE8.
 * <p>
 * Note that this example does not exit, i.e., it monitors forever or until the connection of the
 * KNX network link is terminated. Hence, with KNX servers that have a limit on active tunneling
 * connections (usually 1 or 4), if the group monitor in connected state is terminated by the client
 * (you), the pending state of the open tunnel on the KNX server might temporarily cause an error on
 * subsequent connection attempts.
 *
 * @author B. Malinowsky
 */
public class GroupMonitor extends ProcessListenerEx
{
	/**
	 * Address of your KNXnet/IP server. Replace the host or IP address as necessary.
	 */
	private static final String remoteHost = "myKnxServer.myHome.com";

	// Main entry-level routine
	public static void main(final String[] args)
	{
		// create an instance of the group monitor and run it
		new GroupMonitor().run();
	}

	public void run()
	{
		KNXNetworkLink knxLink = null;
		try {
			// create the network link to the KNX network (TP1 medium)
			knxLink = new KNXNetworkLinkIP(remoteHost, TPSettings.TP1);
			// create a process communicator for monitoring, using our network link
			final ProcessCommunicator pc = new ProcessCommunicatorImpl(knxLink);
			System.out.println("Monitoring KNX network using KNXnet/IP server "
					+ remoteHost + " ...");
			// start listening to group notifications using a process listener
			pc.addProcessListener(this);
			while (true)
				Thread.sleep(1000);
		}
		catch (final KNXException | InterruptedException | RuntimeException e) {
			System.err.println(e);
		}
		finally {
			// close the KNX link
			if (knxLink != null)
				knxLink.close();
		}
	}

	public void groupWrite(final ProcessEvent e) { print("write.ind", e); }
	public void groupReadRequest(final ProcessEvent e) { print("read.req", e); }
	public void groupReadResponse(final ProcessEvent e) { print("read.res", e); }
	public void detached(final DetachEvent e) {}

	// This method is called on every group notification issued by a datapoint on the KNX network.
	// It prints the service primitive, KNX source and destination address, and Application Service
	// Data Unit (ASDU) to System.out.
	private void print(final String svc, final ProcessEvent e)
	{
		try {
			System.out.println(LocalTime.now() + " " + e.getSourceAddr() + "->"
					+ e.getDestination() + " " + svc + ": "
					+ DataUnitBuilder.toHex(e.getASDU(), ""));
		}
		catch (final RuntimeException ex) {
			System.err.println(ex);
		}
	}
}
