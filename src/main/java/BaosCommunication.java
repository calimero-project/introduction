/*
    Calimero 3 - A library for KNX network access
    Copyright (c) 2021, 2023 B. Malinowsky

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

    Linking this library statically or dynamically with other modules is
    making a combined work based on this library. Thus, the terms and
    conditions of the GNU General Public License cover the whole
    combination.

    As a special exception, the copyright holders of this library give you
    permission to link this library with independent modules to produce an
    executable, regardless of the license terms of these independent
    modules, and to copy and distribute the resulting executable under terms
    of your choice, provided that you also meet, for each linked independent
    module, the terms and conditions of the license of that module. An
    independent module is a module which is not derived from or based on
    this library. If you modify this library, you may extend this exception
    to your version of the library, but you are not obligated to do so. If
    you do not wish to do so, delete this exception statement from your
    version.
*/

import java.util.HexFormat;

import io.calimero.KNXException;
import io.calimero.SerialNumber;
import io.calimero.baos.BaosLinkAdapter;
import io.calimero.baos.BaosService;
import io.calimero.baos.BaosService.ErrorCode;
import io.calimero.baos.BaosService.Item;
import io.calimero.baos.BaosService.Property;
import io.calimero.link.KNXNetworkLinkUsb;
import io.calimero.link.LinkEvent;
import io.calimero.link.NetworkLinkListener;
import io.calimero.link.medium.TPSettings;

/**
 * This example shows how to establish a BAOS client link using KNX USB. Minimum requirements are
 * Calimero version &ge; 3.0-M1 and Java SE 17 (module java.base).
 * <p>
 * You can safely run this example, the (established) link will only query some server items.
 */
public class BaosCommunication {

	public static void main(final String[] args) throws KNXException, InterruptedException {
		// establish a knx link like usual
		try (final var link = new KNXNetworkLinkUsb("weinzierl", new TPSettings())) {

			// create an adapter which puts a BAOS capable device into BAOS mode
			final var baosLink = BaosLinkAdapter.asBaosLink(link);

			// add a listener where we register for BAOS events happening on our link
			baosLink.addLinkListener(new NetworkLinkListener() {
				@LinkEvent
				void baosEvent(final BaosService svc) {
					if (svc.error() != ErrorCode.NoError)
						System.err.println(svc);
					else if (svc.subService() == BaosService.GetServerItem) {
						Object value;
						@SuppressWarnings("unchecked")
						final Item<Property> item = (Item<Property>) svc.items().get(0);
						if (item.info() == Property.SerialNumber)
							value = SerialNumber.from(item.data());
						else if (item.info() == Property.ConnectionState)
							value = item.data()[0] == 1 ? "connected" : "not connected";
						else
							value = HexFormat.of().formatHex(item.data());

						System.out.println(item.info() + " = " + value);
					}
				}
			});

			// query some BAOS server items
			baosLink.send(BaosService.getServerItem(Property.SerialNumber, 1));
			Thread.sleep(50);
			baosLink.send(BaosService.getServerItem(Property.ConnectionState, 1));
			Thread.sleep(500);

			// detach the BAOS link, this will reset the underlying knx link back into link-layer mode
			baosLink.detach();
		}
	}
}
