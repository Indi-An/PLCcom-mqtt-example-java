# PLCcom MQTT — Client Workshops

Twelve hands-on workshops for the **high-level PLCcom MQTT client**, grouped into four topics. Each class is a self-contained scenario you can read while it runs.

## How a workshop is built

Every workshop follows the same readable shape, so once you know one you know them all:

1. **MIT header** and a plain-English class comment (*what · when · MQTT terms · expected result*).
2. A **visible license block**, shipped blank — this is where your credentials go:
   ~~~java
   private static final String LICENSE_USER   = "";
   private static final String LICENSE_SERIAL = "";
   ~~~

   Those two empty strings are what turns on evaluation mode: the workshop then runs for 15 minutes during a debug session with the full functional scope. When the evaluation period ends, operation stops: an open connection is disconnected and a running broker halts — the same applies when a time-limited license expires, and the library never terminates your application. Fill both values in for uninterrupted work — free trial license (14 days): https://www.indi-an.com/en/plccom/mqtt/mqtt-download/

3. A **visible broker endpoint** you can change or override:
   ~~~java
   private static final String BROKER_ENDPOINT = "mqtt://localhost:1883";
   ~~~
4. A **framed explanation box** printed as the very first thing when it runs.
5. Clearly numbered steps with *why* comments and **visible result/quality checks**.

No client workshop starts its own broker — a **client-only license is enough**. Point each workshop at a broker workshop, the broker tool, or a local Mosquitto.

## The workshops

| Group (package) | Class | Teaches |
|---|---|---|
| **gettingstarted** (1x) | `_11_FirstConnection` | connect, protocol, Topic Name vs Filter, QoS 1 |
| | `_12_PublishSubscribeAndQos` | QoS 0/1/2, wildcards |
| | `_13_RetainedMessagesAndLastWill` | retained set/read/delete, Last Will |
| **mqtt5** (2x) | `_21_Mqtt5MessageProperties` | Content Type, Payload Format, User Properties, Message Expiry, Topic Alias |
| | `_22_Mqtt5SubscriptionOptions` | Subscription Identifier, No Local, Retain As Published, Retain Handling |
| | `_23_RequestResponse` | Response Topic, Correlation Data (visible responder client) |
| | `_24_JsonMapping` | publish/parse a Java object as JSON |
| **resilience** (3x) | `_31_SessionsReconnectAndLifecycle` | persistent session, offline queue, reconnect, callbacks |
| | `_32_AsyncApi` | non-blocking `CompletableFuture` API |
| | `_33_SharedSubscriptions` | load-balancing consumers |
| **security** (4x) | `_41_SecureConnection` | TLS, WSS, mutual TLS, trust/key store |
| | `_42_DiagnosticsAndErrorHandling` | diagnostic listener, level filter, Quality, error handling |

## Running

From the repository root, start a broker first (or a local Mosquitto), then a client:

~~~powershell
mvn -f Workshops/Broker/pom.xml exec:java -Dexec.mainClass=com.plccom.mqtt.examples.broker.basics._11_FirstBroker
mvn -f Workshops/Client/pom.xml exec:java -Dexec.mainClass=com.plccom.mqtt.examples.client.gettingstarted._11_FirstConnection
~~~

Override the broker address without editing the file:

~~~powershell
-Dplccom.mqtt.workshop.endpoint=mqtt://localhost:1883
~~~

The full client API is documented in the [Client Guide](https://docs.plccom.net/help_mqtt/java/docs/en/appendix-b-client-guide-plccom-mqtt-java-en.pdf) and the [client Javadoc](https://docs.plccom.net/help_mqtt/java/javadoc/com.indi-an.plccom.plccom-mqtt-client/javadoc/index.html).
