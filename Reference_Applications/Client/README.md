# PLCcom MQTT — Client Tool (Reference Application)

A complete Swing desktop application for the **high-level PLCcom MQTT client**. Where the console workshops teach one feature at a time, this tool lets you click through the whole client surface against a real broker — a PLCcom broker workshop, the Broker Tool, Mosquitto or any compatible MQTT broker.

## What you can do

- Connect over **MQTT 3.1.1 or MQTT 5** to any broker (`mqtt`, `ws`, `mqtts`, `wss`).
- **Publish** and **subscribe** with QoS 0/1/2, retained messages and topic wildcards.
- Set and read **MQTT 5 properties**: content type, payload format, user properties, message expiry, response topic, correlation data, subscription identifiers.
- Try **request/response**, **retained deletion** and **Last Will**.
- Configure **TLS / mTLS / WSS** with trust and key stores, including a certificate manager dialog.
- Watch **diagnostics** (severity, category, code) with a selectable log level.
- Switch the interface between **German and English**; integrated help is included.

## Run it

~~~powershell
mvn -f Reference_Applications/Client/pom.xml exec:java
~~~

Or build a runnable jar and start the GUI without a console window with `javaw` (Windows; on Linux/macOS use `java`):

~~~powershell
mvn -f Reference_Applications/Client/pom.xml package
javaw -jar Reference_Applications/Client/target/plccom-mqtt-tools-client-1.0.0.jar
~~~

Enter your PLCcom MQTT license in the application when prompted. Passing two empty strings runs the application for 15 minutes during a debug session (evaluation mode); when that period ends, operation stops and the application is disconnected — the same applies when a time-limited license expires, and the library never terminates it. For uninterrupted work generate a free trial license (14 days) at https://www.indi-an.com/en/plccom/mqtt/mqtt-download/. This is a reference/testing application — see the [Safety Notice](../../README.md#-safety-notice) before using it against real equipment.
