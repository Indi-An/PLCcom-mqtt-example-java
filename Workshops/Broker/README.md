# PLCcom MQTT — Broker Workshops

Ten hands-on workshops for the **PLCcom MQTT broker**, grouped into four topics. The broker workshops are broker-only: they never import the client API. Where a client is useful as a counterpart, the workshop names the matching client workshop.

## How a workshop is built

The same readable shape as the client workshops:

1. **MIT header** and a plain-English class comment (*what · when · MQTT terms · expected result*).
2. A **visible license block**, shipped blank (a broker or broker+client license is required for uninterrupted work):
   ~~~java
   private static final String LICENSE_USER   = "";
   private static final String LICENSE_SERIAL = "";
   ~~~

   Those two empty strings are what turns on evaluation mode: the workshop then runs for 15 minutes during a debug session with the full functional scope. When the evaluation period ends, operation stops: an open connection is disconnected and a running broker halts — the same applies when a time-limited license expires, and the library never terminates your application. Fill both values in for uninterrupted work — free trial license (14 days): https://www.indi-an.com/en/plccom/mqtt/mqtt-download/
3. A **framed explanation box** printed first.
4. Numbered steps with *why* comments and **visible result/quality checks**.

## The workshops

| Group (package) | Class | Teaches |
|---|---|---|
| **basics** (1x) | `_11_FirstBroker` | smallest standalone broker, bound port, clean stop |
| | `_12_ListenersAndBindings` | multiple TCP ports + WebSocket, bind addresses |
| | `_13_SecureListeners` | plain TCP + TLS (mqtts) + secure WebSocket (wss) |
| | `_14_AllListenerTypes` | all four transports on one broker + **per-port certificate** |
| **embedded** (2x) | `_21_EmbeddedBrokerInJavaApp` | own the lifecycle; in-process publish/listen, options, batch, JSON |
| | `_22_RetainedMessagesAndSessions` | retained replay & deletion; sessions/queues |
| **security** (3x) | `_31_AccessControl` | access validator, deny-by-default, topic authorization, denials |
| | `_32_AuthenticationAndPki` | username/password, MQTT 5 enhanced auth, PKI/mTLS |
| **service** (4x) | `_41_CustomStores` | durable retained/session storage you own |
| | `_42_AdminAndDiagnostics` | admin snapshot, counter reset, diagnostic listener |

## Running

Broker workshops start their own broker, so they run on their own:

~~~powershell
mvn -f Workshops/Broker/pom.xml exec:java -Dexec.mainClass=com.plccom.mqtt.examples.broker.basics._11_FirstBroker
~~~

To see access control or a first connection live, run a client workshop or a standard MQTT client against the printed address.

The full broker API is documented in the [Broker Guide](https://docs.plccom.net/help_mqtt/java/docs/en/appendix-c-broker-guide-plccom-mqtt-java-en.pdf) and the [broker Javadoc](https://docs.plccom.net/help_mqtt/java/javadoc/com.indi-an.plccom.plccom-mqtt-broker/javadoc/index.html).
