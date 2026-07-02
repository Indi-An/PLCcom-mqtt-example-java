# PLCcom MQTT — Broker Tool (Reference Application)

A complete Swing desktop application for the **PLCcom MQTT broker**. It starts a configurable broker you can inspect and drive by hand — a perfect counterpart to the Client Tool or to any external MQTT client.

## What you can do

- Start a broker with **TCP, multiple TCP ports, WebSocket, TLS and secure WebSocket** listeners.
- Assign **per-port server certificates** and manage TLS material with a certificate manager dialog.
- **Publish** application values and **listen** to topics **in-process** — no network client needed.
- Watch **retained state**, sessions and **queued messages**.
- Enable **access validation** (username/password, topic authorization) and see denied connections.
- Read **statistics / admin snapshots** and **diagnostics** with a selectable log level.
- Switch the interface between **German and English**; integrated help is included.

## Run it

~~~powershell
mvn -f Reference_Applications/Broker/pom.xml exec:java
~~~

Or build a runnable jar and start the GUI without a console window with `javaw` (Windows; on Linux/macOS use `java`):

~~~powershell
mvn -f Reference_Applications/Broker/pom.xml package
javaw -jar Reference_Applications/Broker/target/plccom-mqtt-tools-broker-1.0.0.jar
~~~

Enter your PLCcom MQTT license in the application when prompted. This is a reference/testing application — see the [Safety Notice](../../README.md#-safety-notice) before using it against real equipment.
