# Broker · 1x · Basics

Start a broker and expose it over the transports your clients need.

| Class | Teaches |
|---|---|
| `_11_FirstBroker` | smallest standalone broker; bound port; clean stop |
| `_12_ListenersAndBindings` | multiple TCP ports + WebSocket; bind addresses |
| `_13_SecureListeners` | plain TCP + TLS (mqtts) + secure WebSocket (wss) |
| `_14_AllListenerTypes` | all four transports on one broker + per-port certificate |

Run:

~~~powershell
mvn -f Workshops/Broker/pom.xml exec:java -Dexec.mainClass=com.plccom.mqtt.examples.broker.basics._11_FirstBroker
~~~
