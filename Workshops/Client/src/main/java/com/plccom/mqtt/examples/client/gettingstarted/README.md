# Client · 1x · Getting Started

The MQTT basics every application needs. Read these first.

| Class | Teaches |
|---|---|
| `_11_FirstConnection` | connect, protocol selection, Topic Name vs Topic Filter, one QoS 1 round trip |
| `_12_PublishSubscribeAndQos` | QoS 0/1/2 and topic wildcards on one subscription |
| `_13_RetainedMessagesAndLastWill` | retained set/read/delete and the Last Will |

Run (start a broker first, or use Mosquitto):

~~~powershell
mvn -f Workshops/Client/pom.xml exec:java -Dexec.mainClass=com.plccom.mqtt.examples.client.gettingstarted._11_FirstConnection
~~~
