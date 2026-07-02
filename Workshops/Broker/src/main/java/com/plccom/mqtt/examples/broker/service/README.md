# Broker · 4x · Service

Run the broker in production: durable storage, monitoring and diagnostics.

| Class | Teaches |
|---|---|
| `_41_CustomStores` | move retained/session state into your own durable storage; performance & responsibility |
| `_42_AdminAndDiagnostics` | admin snapshot, safe counter reset, diagnostic listener with a level filter |

Run:

~~~powershell
mvn -f Workshops/Broker/pom.xml exec:java -Dexec.mainClass=com.plccom.mqtt.examples.broker.service._41_CustomStores
~~~
