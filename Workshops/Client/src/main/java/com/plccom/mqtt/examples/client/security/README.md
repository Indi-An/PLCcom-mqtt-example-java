# Client · 4x · Security & Operations

Secure the transport and operate the client in production.

| Class | Teaches |
|---|---|
| `_41_SecureConnection` | TLS, WSS and mutual TLS with a client-side trust/key store |
| `_42_DiagnosticsAndErrorHandling` | diagnostic listener, level filtering, Quality checks, error handling |

`_41` needs a TLS-enabled broker (see Broker workshops `_13`/`_14`) and a trust store that contains the broker certificate. For the full TLS/mTLS/PKI picture see the official [Security Guide](https://docs.plccom.net/help_mqtt/java/docs/en/appendix-a-security-guide-plccom-mqtt-java-en.pdf).

Run:

~~~powershell
mvn -f Workshops/Client/pom.xml exec:java -Dexec.mainClass=com.plccom.mqtt.examples.client.security._42_DiagnosticsAndErrorHandling
~~~
