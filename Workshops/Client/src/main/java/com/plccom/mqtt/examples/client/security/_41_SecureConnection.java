// MIT License
// Copyright (c) Indi.An GmbH
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.

package com.plccom.mqtt.examples.client.security;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;

import com.plccom.console.PLCcomConsole;
import com.plccom.mqtt.client.MqttMessage;
import com.plccom.mqtt.client.PlccomMqttClient;
import com.plccom.mqtt.client.PlccomMqttClientFactory;
import com.plccom.mqtt.client.PlccomMqttConnectResult;
import com.plccom.mqtt.client.PlccomMqttPublishMessage;
import com.plccom.mqtt.client.PlccomMqttTlsOptions;

/**
 * Workshop 41 - Secure Connection with TLS and mTLS (Security)
 *
 * WHAT THIS WORKSHOP SHOWS
 *   How to connect securely over TLS and what client-side PKI you configure. TLS
 *   encrypts the transport; the client uses a Trust Store to verify the broker's
 *   certificate. Mutual TLS (mTLS) additionally presents a client certificate
 *   from a Key Store so the broker can verify this client too. The same options
 *   apply to secure WebSockets (wss://).
 *
 * WHEN YOU NEED THIS IN REAL APPLICATIONS
 *   Any connection that leaves a trusted network, and any deployment where the
 *   broker must authenticate clients by certificate instead of (or in addition
 *   to) username and password.
 *
 * BROKER FOR THIS WORKSHOP
 *   This client workshop needs a running MQTT broker with a TLS listener. Start
 *   Broker Workshop 13 (Secure Listeners) first, or point BROKER_ENDPOINT below
 *   at any TLS-enabled broker of your choice (for example a Mosquitto configured
 *   with TLS). No broker is embedded here, so a client-only PLCcom license is
 *   enough. You must also point TRUST_STORE_PATH at a trust store that contains
 *   the broker's certificate (or its CA). Override the endpoint with
 *   -Dplccom.mqtt.workshop.endpoint=mqtts://host:8883 .
 *
 * MQTT TERMS USED HERE
 *   - TLS (mqtts://)        : encrypts the transport and lets the client verify
 *                             the broker certificate.
 *   - WSS (wss://)          : the same TLS, but over a WebSocket transport.
 *   - Trust Store           : holds the certificate(s) the client trusts for the
 *                             broker side.
 *   - Key Store / mTLS      : holds this client's private key and certificate,
 *                             presented so the broker can authenticate the client.
 *   - Hostname verification : checks that the broker certificate name matches the
 *                             endpoint host.
 *
 * EXPECTED RESULT
 *   An encrypted connection is established and a message round-trips over TLS.
 */
public final class _41_SecureConnection {

    // === YOUR PLCcom LICENSE =================================================
    // Replace both values with the user name and serial from your PLCcom license
    // e-mail. A client-only license is sufficient for the client workshops.
    private static final String LICENSE_USER   = "<Enter your UserName here>";
    private static final String LICENSE_SERIAL = "<Enter your Serial here>";
    // =========================================================================

    // === BROKER TO CONNECT TO ================================================
    // A TLS endpoint. Use mqtts:// for TLS over TCP or wss://host:443/mqtt for
    // secure WebSockets. Start Broker Workshop 13 (Secure Listeners) or a TLS
    // broker of your choice.
    private static final String BROKER_ENDPOINT = "mqtts://localhost:8883";
    // =========================================================================

    // === TLS MATERIAL (client-side PKI) ======================================
    // Trust Store: the certificate(s) the client trusts for the broker. Point
    // this at a PKCS#12 file that contains the broker certificate or its CA.
    private static final String TRUST_STORE_PATH     = "./tls/client-truststore.p12";
    private static final char[] TRUST_STORE_PASSWORD = "changeit".toCharArray();

    // Mutual TLS (optional): set to true and provide a Key Store with this
    // client's private key and certificate when the broker requires a client
    // certificate.
    private static final boolean USE_MUTUAL_TLS       = false;
    private static final String  CLIENT_KEY_STORE_PATH = "./tls/client-keystore.p12";
    private static final char[]  CLIENT_KEY_PASSWORD   = "changeit".toCharArray();

    // For a self-signed broker certificate whose name does not match "localhost"
    // you may need to disable hostname verification for LOCAL TESTING only. Keep
    // it enabled in production.
    private static final boolean VERIFY_HOSTNAME = true;
    // =========================================================================

    private static final String TOPIC = "plccom/workshop/41/secure";

    private _41_SecureConnection() {
    }

    /**
     * Opens the console and runs the workshop.
     *
     * @param args command-line arguments; not used by this workshop
     */
    public static void main(String[] args) {
        PLCcomConsole.open("PLCcom MQTT Workshop 41 - Secure Connection (TLS / mTLS)", 1000);
        try {
            start();
        } catch (Exception ex) {
            System.out.println();
            System.out.println("  This workshop needs a TLS broker at " + brokerEndpoint()
                    + " and a trust store at " + TRUST_STORE_PATH + ".");
            System.out.println("  Start Broker Workshop 13 (Secure Listeners) or a TLS broker, and make");
            System.out.println("  sure the trust store contains the broker certificate. Then run again.");
            System.out.println("  Technical detail: " + ex.getMessage());
        } finally {
            PLCcomConsole.waitForEnter();
            PLCcomConsole.close();
        }
    }

    /**
     * Builds client-side TLS options and performs a secure publish/subscribe.
     *
     * @throws Exception if the TLS material cannot be loaded or the secure
     *         connection or message exchange fails
     */
    public static void start() throws Exception {
        System.out.println("╔════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║  What this workshop demonstrates                                           ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  How to connect securely over TLS, and the client-side PKI you configure.  ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  TLS encrypts the transport (mqtts:// or wss://). You provide a Trust Store║");
        System.out.println("║  so the client can verify the broker's certificate.                        ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  Mutual TLS (mTLS) adds a client certificate: set a Key Store so the broker║");
        System.out.println("║  can also verify THIS client. Toggle USE_MUTUAL_TLS below.                 ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  Hostname verification checks the broker certificate matches the address.  ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  Expected result: an encrypted connection is established and a message     ║");
        System.out.println("║  round-trips over TLS.                                                     ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════════════╝");
        System.out.println();

        System.out.println("  Broker endpoint : " + brokerEndpoint());
        System.out.println();

        // -- Step 1: build the client-side TLS options --------------------------
        // This is the actual lesson: which PKI material the client configures.
        // The Trust Store lets the client verify the broker. mTLS adds a Key
        // Store so the broker can verify the client. None of this is hidden - it
        // is the visible security configuration a real application must own.
        System.out.println("  Loading trust store from " + TRUST_STORE_PATH + " ...");
        KeyStore trustStore = loadPkcs12(TRUST_STORE_PATH, TRUST_STORE_PASSWORD);

        PlccomMqttTlsOptions.Builder tls = PlccomMqttTlsOptions.builder()
                .trustStore(trustStore)                       // verify the broker
                .hostnameVerificationEnabled(VERIFY_HOSTNAME);

        if (USE_MUTUAL_TLS) {
            System.out.println("  Loading client key store for mTLS from " + CLIENT_KEY_STORE_PATH + " ...");
            KeyStore keyStore = loadPkcs12(CLIENT_KEY_STORE_PATH, CLIENT_KEY_PASSWORD);
            tls.keyStore(keyStore, CLIENT_KEY_PASSWORD);      // let the broker verify us
        }

        PlccomMqttTlsOptions tlsOptions = tls.build();

        // -- Step 2: build the client and enable TLS ----------------------------
        // tls(tlsOptions) turns the connection into an encrypted mqtts:// (or
        // wss://) connection using exactly the options built above.
        PlccomMqttClient client = PlccomMqttClientFactory.builder(LICENSE_USER, LICENSE_SERIAL)
                .identifier("plccom-workshop-41")
                .endpoint(brokerEndpoint())
                .mqtt5()
                .tls(tlsOptions)
                .keepAliveSeconds(30)
                .build();

        try {
            // -- Step 3: connect over TLS --------------------------------------
            System.out.println("  Performing the TLS handshake and MQTT CONNECT ...");
            PlccomMqttConnectResult connectResult = client.connect();
            if (!connectResult.isGood()) {
                throw new IllegalStateException("Secure CONNECT failed: "
                        + connectResult.getQuality() + " - " + connectResult.getMessage());
            }
            System.out.println("  Connected securely. Negotiated protocol: "
                    + connectResult.getNegotiatedProtocolVersion());

            // -- Step 4: a normal publish/subscribe, now encrypted -------------
            client.subscribe(TOPIC).qos1().send();
            client.publish(TOPIC, "secret over TLS").qos1().payloadFormatUtf8().send();
            PlccomMqttPublishMessage received = client.receive(3000);
            if (received == null) {
                throw new IllegalStateException("The message did not arrive over the secure connection.");
            }
            System.out.println("  Received over TLS: '" + new MqttMessage(received).payloadAsString() + "'");
            System.out.println();
            System.out.println("  Success: the message round-tripped over an encrypted connection.");
        } finally {
            client.close();
        }
    }

    // Loads a PKCS#12 (.p12) key/trust store from a file. Kept visible in this
    // class because loading PKI material is part of the security lesson.
    private static KeyStore loadPkcs12(String path, char[] password) throws Exception {
        KeyStore store = KeyStore.getInstance("PKCS12");
        try (InputStream input = new FileInputStream(path)) {
            store.load(input, password);
        }
        return store;
    }

    private static String brokerEndpoint() {
        return System.getProperty("plccom.mqtt.workshop.endpoint", BROKER_ENDPOINT);
    }
}
