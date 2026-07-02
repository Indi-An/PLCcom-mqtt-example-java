# Broker · 2x · Embedded

Host the broker inside your own Java process and exchange data without a network client.

| Class | Teaches |
|---|---|
| `_21_EmbeddedBrokerInJavaApp` | own the lifecycle; in-process publish/listen; options, batch, JSON, command flow |
| `_22_RetainedMessagesAndSessions` | retained replay & deletion; session/queue behavior |

Run:

~~~powershell
mvn -f Workshops/Broker/pom.xml exec:java -Dexec.mainClass=com.plccom.mqtt.examples.broker.embedded._21_EmbeddedBrokerInJavaApp
~~~
