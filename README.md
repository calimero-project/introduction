Introduction to Calimero
========================

This repository contains additional documentation for Calimero and code examples.

### Examples for Calimero 2.3 (or earlier)

* [Create a KNX link (using default settings)](examples/CreateKnxLink.java)
* [Create a KNX link (with more options)](examples/CreateKnxLink2.java)
* [Process communication (read/write datapoints)](examples/ProcessCommunication.java)
* [Use logging output](examples/Logging.java)
* [KNX address converter](examples/KnxAddressConverter.java)

### Examples for Calimero version 2.4 snapshots (requires Java 8)

All examples can be built (not executed) using Gradle (`./gradlew build`)

* [Discover KNXnet/IP servers](src/main/java/DiscoverKnxServers.java)
* [Discover KNX USB devices](src/main/java/DiscoverUsbDevices.java)
* [Create client-side KNXnet/IP tunneling network link](src/main/java/CreateTunnelingLink.java)
* [Create client-side KNX USB network link](src/main/java/CreateUsbLink.java)
* [Create TPUART network monitor link](src/main/java/CreateTpuartMonitor.java)
* [Process communication](src/main/java/ProcessCommunication.java)
* [Group monitor](src/main/java/GroupMonitor.java)
* [DPT translation](src/main/java/DptTranslation.java)
* [Network state buffering](src/main/java/NetworkStateBuffering.java)
* [KNX push-button device](src/main/java/PushButtonDevice.java)
* [KNX IP push-button device supporting discovery & self description](src/main/java/PushButtonDeviceWithDiscovery.java)

#### Guide for the KNX push-button device example

- Run the example in your IDE. Or, from the terminal (put the required `jar` dependencies in the introduction directory: calimero-core, calimero-device, slf4j-api, optionally slf4j-simple):

 ~~~ sh
	# Compile the Java file
	$ javac -cp "./*" src/main/java/PushButtonDevice.java
	# Start the KNX device
	$ java -cp "./*:src/main/java/" PushButtonDevice
 ~~~

- Use process communication to read/write the push button state. For example, start the Calimero [process communication tool](https://github.com/calimero-project/calimero-tools/blob/master/src/tuwien/auto/calimero/tools/ProcComm.java)) in group monitor mode (`monitor`) in a second terminal. Using maven:

 ~~~ sh
	$ mvn exec:java -Dexec.args="groupmon 224.0.23.12"
 ~~~
 and enter the following commands:
 
 ~~~ sh	
	read 1/0/3 switch
	[response should be printed with switch state off]
	write 1/0/3 on
	r 1/0/3
	[response should be printed with switch state on]
	Ctrl^C
 ~~~

- Read device information of the Calimero KNX device. For example, using the Calimero Device Info tool
 
 ~~~ sh 
	$ mvn exec:java -Dexec.args="devinfo 224.0.23.12 1.1.10"
 ~~~

- Discover KNX IP device (for the example which supports KNXnet/IP Discovery & Self Description)

  The device should show up in the ETS. Otherwise, you can also use the Calimero discover tool. Using Gradle

		./gradlew run -Dexec.args="discover"

  Example output:

		Using /192.168.10.10 at en0
		---------------------------
		Control endpoint 192.168.10.17:3671 (IPv4 UDP) "Push Button (KNX IP)"
		KNX address 1.1.10
		KNX medium KNX IP
		installation 0 - project 0 (ID 0)
		routing multicast address 224.0.23.12
		MAC address f4:5c:89:8a:f4:9b
		S/N 0x000000000000
		Supported services: Core (v1)


### Java ME Embedded 8 Example

* [Java ME Embedded 8 Midlet](examples/midlet/)
