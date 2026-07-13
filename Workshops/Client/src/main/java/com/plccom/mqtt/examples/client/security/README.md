# Client · 4x · Security & Operations

Secure the transport and operate the client in production.

| Class | Teaches |
|---|---|
| `_41_SecureConnection` | TLS, WSS and mutual TLS with a client-side trust/key store (plus an optional custom remote-certificate validator) |
| `_42_DiagnosticsAndErrorHandling` | diagnostic listener, level filtering, Quality checks, error handling |
| `_43_EnhancedAuthentication` | client-side MQTT 5 enhanced authentication: method + token and AUTH challenges |

`_41` needs a TLS-enabled broker (see Broker workshops `_13`/`_14`) and a trust store that contains the broker certificate. `_43` needs a broker whose enhanced authenticator accepts its method/token — pair it with Broker workshop `_32`. For the full TLS/mTLS/PKI picture see the official [Security Guide](https://docs.plccom.net/help_mqtt/java/docs/en/appendix-a-security-guide-plccom-mqtt-java-en.pdf).

Run:

~~~powershell
mvn -f Workshops/Client/pom.xml exec:java -Dexec.mainClass=com.plccom.mqtt.examples.client.security._42_DiagnosticsAndErrorHandling
~~~
