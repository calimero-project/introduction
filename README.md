Introduction to Calimero
========================

This repository contains additional documentation for Calimero and code examples using Java and Kotlin.

All examples require Java 17 and can be built using Gradle (`./gradlew build`). A single example can be executed via Gradle by specifying the class like `./gradlew run -DmainClass=GroupMonitor`.

For Kotlin, an example can be run via Gradle by appending "Kt" to the class name, e.g., for _DiscoverKnxServers_ this would be `./gradlew run -DmainClass=DiscoverKnxServersKt`

* [Discover KNXnet/IP servers](src/main/java/DiscoverKnxServers.java)
* [Discover KNX USB devices](src/main/java/DiscoverUsbDevices.java)
* [Create client-side KNXnet/IP tunneling network link](src/main/java/CreateTunnelingLink.java)
* [KNX IP Secure routing network link](src/main/java/KnxipSecure.java)
* [KNX IP Secure tunneling network link](src/main/java/SecureTunnelingLink.java)
* [Create client-side KNX USB network link](src/main/java/CreateUsbLink.java)
* [Create TPUART network monitor link](src/main/java/CreateTpuartMonitor.java)
* [Process communication](src/main/java/ProcessCommunication.java)
* [Process communication using KNX Secure](src/main/java/DataSecureProcessCommunication.java)
* [Group monitor](src/main/java/GroupMonitor.java)
* [KNX address converter](src/main/java/KnxAddressConverter.java), e.g., `./gradlew run -DmainClass=KnxAddressConverter --args="1/2/3"`
* [DPT translation](src/main/java/DptTranslation.java)
* [Network state buffering](src/main/java/NetworkStateBuffering.java)
* [KNX IP push-button device](src/main/java/PushButtonDevice.java)
* [LTE device for LTE-HEE runtime communication](src/main/java/LteDevice.java)
* [ETS keyring viewer](src/main/java/KeyringViewer.java), run it with Gradle using
	`./gradlew run -DmainClass=KeyringViewer --args="--pwd pwd '/path/to/keyring.knxkeys'"`
* [Basic programmable device](src/main/java/ProgrammableDevice.java)


#### Guide for the KNX push-button device example

- Run the example in your IDE, or command line using `./gradlew run -DmainClass=PushButtonDevice`

- Use process communication to read/write the push button state, for example in the ETS group monitor. Or with the Calimero [tools](https://github.com/calimero-project/calimero-tools) group monitor in a second terminal (`./gradlew run --args "groupmon 224.0.23.12"`) and enter the following commands:
 
 ~~~ sh	
	read 1/0/1 switch
	[response should be printed with switch state off]
	write 1/0/1 on
	r 1/0/1
	[response should be printed with switch state on]
	Ctrl^C
 ~~~

- Read device information of the Calimero KNX device, for example with the ETS device info diagnostics. Or, use the Calimero device info tool `./gradlew run --args="devinfo 224.0.23.12 1.1.10"`.

- Discover the KNX IP device. With the Calimero discover tool, `./gradlew run --args="discover"`

  Example output:

		Using 192.168.10.10 (en0)
		-------------------------
		"Push Button (KNX IP)" endpoint 192.168.10.17:3671 (IPv4 UDP)
		KNX address 1.1.10
		KNX medium KNX IP
		Installation 0 - Project 0 (ID 0)
		KNX IP multicast address 224.0.23.12
		MAC address f4:5c:89:8a:f4:9b
		Supported services: Core (v1)


### Archived examples

* Examples for Calimero v2.6 (requires Java 17) can be found on the [2.6 branch](https://github.com/calimero-project/introduction/tree/2.6)
* Examples for Calimero v2.4 (requires Java 8) can be found on the [release/2.4 branch](https://github.com/calimero-project/introduction/tree/release/2.4)
* Examples for Calimero v2.3 (or earlier) can be found on the [release/2.3 branch](https://github.com/calimero-project/introduction/tree/release/2.3)
