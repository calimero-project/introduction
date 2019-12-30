/*
    Calimero 2 - A library for KNX network access
    Copyright (c) 2019 B. Malinowsky

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

import tuwien.auto.calimero.DataUnitBuilder;
import tuwien.auto.calimero.Keyring;

/**
 * Prints the (decrypted) content of a keyring (*.knxkeys) resource. Minimum requirements are Calimero version &ge; 2.5
 * and Java SE 11 (module java.base).
 * <p>
 * You can safely run this example, the keyring is not modified, and any content is output to {@code System.out}.
 */
public class KeyringViewer {
	private final Keyring keyring;
	private char[] keyringPwd = new char[0];

	public static void main(final String... args) {
		if (args.length == 0) {
			System.out.println("Usage: [--pwd keyringPassword] <keyring resource (*.knxkeys)>");
			return;
		}
		new KeyringViewer(args).view();
	}

	public KeyringViewer(final String... args) {
		int argIdx = 0;
		if ("--pwd".equals(args[argIdx])) {
			keyringPwd = args[++argIdx].toCharArray();
			++argIdx;
		}
		keyring = Keyring.load(args[argIdx]);
	}

	private void view() {
		// check keyring signature if we got a password
		if (keyringPwd.length > 0) {
			final var valid = keyring.verifySignature(keyringPwd);
			final var result = valid ? "OK" : "FAILED!";
			System.out.println("Signature verification " + result);
			System.out.println();
		}

		System.out.println("Devices");
		System.out.println("-------");
		for (final var device : keyring.devices().values()) {
			System.out.print(device);
			if (keyringPwd.length > 0) {
				System.out.print(", management password " + decryptPwd(device.password()));
				System.out.print(", authentication " + decryptPwd(device.authentication()));
			}
			System.out.println();
		}

		for (final var entry : keyring.interfaces().entrySet()) {
			System.out.println("Interfaces of device " + entry.getKey());
			System.out.println("---------------------------");
			final var interfaces = entry.getValue();
			for (final var iface : interfaces) {
				System.out.print(iface);
				if (keyringPwd.length > 0) {
					System.out.print(", management password " + decryptPwd(iface.password()));
					System.out.print(", authentication " + decryptPwd(iface.authentication()));
				}
				System.out.println();
			}
		}

		System.out.println("Groups");
		System.out.println("------");
		for (final var group : keyring.groups().entrySet()) {
			if (keyringPwd.length > 0)
				System.out.println(group.getKey() + " key " + decryptKey(group.getValue()));
			else
				System.out.println(group.getKey());
		}
	}

	private String decryptPwd(final byte[] input) {
		return "'" + new String(keyring.decryptPassword(input, keyringPwd)) + "'";
	}

	private String decryptKey(final byte[] input) {
		return "'" + DataUnitBuilder.toHex(keyring.decryptKey(input, keyringPwd), "") + "'";
	}
}
