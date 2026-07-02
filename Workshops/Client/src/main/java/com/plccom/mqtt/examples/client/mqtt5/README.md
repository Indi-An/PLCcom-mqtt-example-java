# Client · 2x · MQTT 5

The features that make MQTT 5 more than pub/sub: rich metadata, precise subscription control, request/response and JSON.

| Class | Teaches |
|---|---|
| `_21_Mqtt5MessageProperties` | Content Type, Payload Format, User Properties, Message Expiry, Topic Alias |
| `_22_Mqtt5SubscriptionOptions` | Subscription Identifier, No Local, Retain As Published, Retain Handling |
| `_23_RequestResponse` | Response Topic + Correlation Data (responder is a second visible client) |
| `_24_JsonMapping` | publish a Java object as JSON and read it back |

Run:

~~~powershell
mvn -f Workshops/Client/pom.xml exec:java -Dexec.mainClass=com.plccom.mqtt.examples.client.mqtt5._21_Mqtt5MessageProperties
~~~
