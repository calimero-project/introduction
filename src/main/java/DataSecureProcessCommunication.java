import java.time.LocalTime;
import java.util.HexFormat;

import io.calimero.DetachEvent;
import io.calimero.KNXException;
import io.calimero.link.KNXNetworkLinkUsb;
import io.calimero.link.medium.TPSettings;
import io.calimero.process.ProcessCommunicatorImpl;
import io.calimero.process.ProcessEvent;
import io.calimero.process.ProcessListener;
import io.calimero.secure.Keyring;
import io.calimero.secure.Security;

/**
 * This example shows how to use KNX Data Secure process communication (using a KNX USB device link). For communicating
 * with a single KNX installation, providing the necessary keys to {@code Security.defaultInstallation()} is sufficient
 * for any subsequent communication via KNX Data Secure.<br>
 * Because a client application is usually not configured as part of a KNX Data Secure installation, it is
 * not recognized as KNX Data Secure device. Therefore, secure process communication relies on Group Object Diagnostics.
 * <p>
 * Minimum requirements are Calimero version 3.0-SNAPSHOT and Java SE 17 (java.base).
 * <p>
 * You can safely run this example; the established connection is closed 10 seconds after creation. No KNX messages are
 * sent to the KNX network.
 */
public class DataSecureProcessCommunication {
	// Specify your KNX USB device; either use the product or manufacturer name, or the USB vendor:product ID
	private static final String device = "weinzierl";

	// URI pointing to a keyring with key information for KNX Secure
	private static final String keyringUri = "my-keys.knxkeys";
	private static final char[] keyringPwd = "keyring-pwd".toCharArray();

	public static void main(final String... args) throws KNXException, InterruptedException {
		System.out.println("Establish KNX Data Secure process communication using the KNX USB device '" + device + "'");

		// Provide the keyring to use by default for KNX Data Secure
		Security.defaultInstallation().useKeyring(Keyring.load(keyringUri), keyringPwd);

		// Create the KNX USB device link as you would for plain communication
		try (var knxLink = new KNXNetworkLinkUsb(device, new TPSettings());
			// This process communicator constructor uses the keys of the default installation
			var pc = new ProcessCommunicatorImpl(knxLink)) {

			// Add a process listener which prints (decrypted) process events
			pc.addProcessListener(new ProcessListener() {
				@Override
				public void groupWrite(final ProcessEvent e) { print("write.ind", e); }

				@Override
				public void groupReadRequest(final ProcessEvent e) { print("read.req", e); }

				@Override
				public void groupReadResponse(final ProcessEvent e) { print("read.res", e); }

				@Override
				public void detached(final DetachEvent e) {}
			});

			System.out.println("KNX Data Secure is ready");

			// Writing datapoint values will use KNX Data Secure if indicated by the used keyring
//			pc.write(...);

			Thread.sleep(10_000);
		}
	}

	private static void print(final String svc, final ProcessEvent pe) {
		try {
			System.out.format("%s %s->%s %s: %s%n", LocalTime.now(), pe.getSourceAddr(), pe.getDestination(), svc,
					HexFormat.of().formatHex(pe.getASDU()));
		}
		catch (final RuntimeException rte) {
			System.err.println(rte);
		}
	}
}
