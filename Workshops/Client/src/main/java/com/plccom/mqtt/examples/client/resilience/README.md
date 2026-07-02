# Client · 3x · Resilience

Make a client survive unreliable networks and scale its processing.

| Class | Teaches |
|---|---|
| `_31_SessionsReconnectAndLifecycle` | persistent session, offline queue, automatic reconnect, lifecycle callbacks |
| `_32_AsyncApi` | the same flow with non-blocking `CompletableFuture` calls |
| `_33_SharedSubscriptions` | load-balance a job stream across two workers |

Run:

~~~powershell
mvn -f Workshops/Client/pom.xml exec:java -Dexec.mainClass=com.plccom.mqtt.examples.client.resilience._31_SessionsReconnectAndLifecycle
~~~
