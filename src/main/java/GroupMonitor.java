/*
    Calimero 2 - A library for KNX network access
    Copyright (c) 2015, 2018 B. Malinowsky

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
import java.time.LocalTime;

import tuwien.auto.calimero.DataUnitBuilder;
import tuwien.auto.calimero.DetachEvent;
import tuwien.auto.calimero.KNXException;
import tuwien.auto.calimero.link.KNXNetworkLink;
import tuwien.auto.calimero.link.KNXNetworkLinkIP;
import tuwien.auto.calimero.link.medium.TPSettings;
import tuwien.auto.calimero.process.ProcessCommunicator;
import tuwien.auto.calimero.process.ProcessCommunicatorImpl;
import tuwien.auto.calimero.process.ProcessEvent;
import tuwien.auto.calimero.process.ProcessListener;

/**
 * Example code showing how to use KNX process communication for group monitoring on a KNX Twisted Pair 1 (TP1) network.
 * On receiving group notifications, the KNX source and destination address are printed to System.out, as well as any
 * data part of the application service data unit (ASDU) in hexadecimal format.
 * <p>
 * Note that this example does not exit, i.e., it monitors forever or until the KNX network link connection got
 * closed. Hence, with KNX servers that have a limit on active tunneling connections (usually 1 or 4), if the group
 * monitor in connected state is terminated by the client (you), the pending state of the open tunnel on the KNX server
 * might temporarily cause an error on subsequent connection attempts.
 *
 * @author B. Malinowsky
 */
public class GroupMonitor implements ProcessListener
{
	/**
	 * Address of your KNXnet/IP server. Replace the host or IP address as necessary.
	 */
	private static final String remoteHost = "192.168.10.10";

	public static void main(final String[] args)
	{
		new GroupMonitor().run();
	}

	public void run()
	{
		final InetSocketAddress remote = new InetSocketAddress(remoteHost, 3671);
		try (KNXNetworkLink knxLink = KNXNetworkLinkIP.newTunnelingLink(null, remote, false, TPSettings.TP1);
		     ProcessCommunicator pc = new ProcessCommunicatorImpl(knxLink)) {

			// start listening to group notifications using a process listener
			pc.addProcessListener(this);
			System.out.println("Monitoring KNX network using KNXnet/IP server " + remoteHost + " ...");

			while (knxLink.isOpen()) Thread.sleep(1000);
		}
		catch (final KNXException | InterruptedException | RuntimeException e) {
			System.err.println(e);
		}
	}

	@Override
	public void groupWrite(final ProcessEvent e) { print("write.ind", e); }
	@Override
	public void groupReadRequest(final ProcessEvent e) { print("read.req", e); }
	@Override
	public void groupReadResponse(final ProcessEvent e) { print("read.res", e); }
	@Override
	public void detached(final DetachEvent e) {}

	// Called on every group notification issued by a datapoint on the KNX network. It prints the service primitive,
	// KNX source and destination address, and Application Service Data Unit (ASDU) to System.out.
	private static void print(final String svc, final ProcessEvent e)
	{
		try {
			System.out.println(LocalTime.now() + " " + e.getSourceAddr() + "->" + e.getDestination() + " " + svc
					+ ": " + DataUnitBuilder.toHex(e.getASDU(), ""));
		}
		catch (final RuntimeException ex) {
			System.err.println(ex);
		}
	}
}
