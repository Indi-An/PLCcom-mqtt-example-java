# Broker · 3x · Security

Decide who may connect and what they may do.

| Class | Teaches |
|---|---|
| `_31_AccessControl` | access validator (CONNECT/PUBLISH/SUBSCRIBE/UNSUBSCRIBE), deny-by-default, topic authorization, denials |
| `_32_AuthenticationAndPki` | username/password, MQTT 5 enhanced authentication, certificate / mTLS, PKI store |

A denied CONNECT is also reported as a `BROKER_ACCESS_CONNECT_DENIED` warning diagnostic — without the password. To see a live denial, connect a client (e.g. Client `_11`) with the wrong credentials.

Run:

~~~powershell
mvn -f Workshops/Broker/pom.xml exec:java -Dexec.mainClass=com.plccom.mqtt.examples.broker.security._31_AccessControl
~~~
