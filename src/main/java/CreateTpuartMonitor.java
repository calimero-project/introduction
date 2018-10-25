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

import tuwien.auto.calimero.CloseEvent;
import tuwien.auto.calimero.FrameEvent;
import tuwien.auto.calimero.KNXException;
import tuwien.auto.calimero.link.KNXNetworkMonitor;
import tuwien.auto.calimero.link.KNXNetworkMonitorTpuart;
import tuwien.auto.calimero.link.LinkListener;
import tuwien.auto.calimero.link.MonitorFrameEvent;

/**
 * This example shows how to establish a client network monitor ({@link KNXNetworkMonitor}) to a KNX TP1 network using
 * TP-UART serial communication. Minimum requirements are Calimero version &ge; 2.4 and Java SE 8 compact1 profile.
 * <p>
 * You can safely run this example; the (established) monitor connection is completely passive. No KNX messages are sent
 * to the KNX network. The network monitor will run for 10 seconds to let you monitor some KNX frames.
 *
 * @author B. Malinowsky
 */
public class CreateTpuartMonitor
{
	/** Specify the serial port of your KNX TP-UART device. */
	private static final String portId = "/dev/ttyACM0";

	public static void main(final String[] args) throws KNXException, InterruptedException
	{
		System.out.println("This example establishes a KNX monitor connection using TP-UART on port '" + portId + "'");

		// Create the TP-UART monitoring link to a TP1 KNX network.
		// The second argument indicates that we want the monitor to provide decoded raw frames for us.
		try (KNXNetworkMonitor knxMonitor = new KNXNetworkMonitorTpuart(portId, true)) {
			System.out.println("Connection established");

			// add listener to notify us of any indication, and provide us with the decoded raw frame
			knxMonitor.addMonitorListener(new LinkListener() {
				@Override
				public void indication(final FrameEvent e)
				{
					System.out.println(e.getFrame() + ": " + ((MonitorFrameEvent) e).getRawFrame());
				}

				@Override
				public void linkClosed(final CloseEvent e) {}
			});

			// let's wait some seconds to monitor KNX frames
			Thread.sleep(10000);
		}
	}
}
