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

package com.plccom.mqtt.examples.broker.basics;

import java.nio.file.Paths;
import java.util.Arrays;

import com.plccom.console.PLCcomConsole;
import com.plccom.mqtt.broker.PlccomMqttBroker;
import com.plccom.mqtt.broker.PlccomMqttBrokerFactory;
import com.plccom.mqtt.pki.PlccomMqttOwnCertificate;

/**
 * Workshop 13 - Secure Listeners: TLS and WSS (Setup)
 *
 * WHAT THIS WORKSHOP SHOWS
 *   How to run encrypted listeners on the broker: a TLS listener ({@code mqtts})
 *   and a secure WebSocket listener ({@code wss}), next to a plain TCP listener
 *   for migration. It also shows where the broker's server certificate comes
 *   from - here auto-created in a local PKI folder so the workshop runs without
 *   external files.
 *
 * WHEN YOU NEED THIS IN REAL APPLICATIONS
 *   Any broker reachable outside a fully trusted network. TLS protects the
 *   transport and gives the broker a verifiable server identity before MQTT
 *   authentication even begins.
 *
 * MATCHING CLIENT
 *   Pair this with Client Workshop 41 (Secure Connection). The client needs to
 *   trust this broker's certificate (export it from the ./pki folder into the
 *   client's trust store).
 *
 * MQTT TERMS USED HERE
 *   - TLS listener (mqtts) : encrypted MQTT over TCP.
 *   - Secure WebSocket (wss): encrypted MQTT over WebSocket.
 *   - Server certificate   : the broker's TLS identity. Production uses the
 *                            company PKI; this workshop auto-creates a self-signed
 *                            certificate in ./pki via PlccomMqttOwnCertificate.
 *                            The certificate must contain the listener host as
 *                            DNS/IP SAN; otherwise strict clients reject it.
 *   - Per-port certificate : a certificate can be assigned to a specific port
 *                            with assignTlsCertificateToPort(...) (see comments).
 *   - Client certificates  : each TLS/WSS listener carries its own
 *                            clientCertificateMode(...) in its TLS options:
 *                            NONE never requests a client certificate, OPTIONAL
 *                            requests one but accepts connections without it
 *                            (the access validator can then deny at CONNECT with
 *                            reason 0x87), REQUIRED aborts the TLS handshake
 *                            without one (strict mTLS). One broker can expose
 *                            an OPTIONAL and a REQUIRED port side by side.
 *
 * EXPECTED RESULT
 *   The broker starts a plain TCP, a TLS and a secure WebSocket listener and
 *   prints all three bound ports.
 */
public final class _13_SecureListeners {

    // === YOUR PLCcom LICENSE =================================================
    // Important !!!!!!!!!!!!!!!!!!
    // Enter your Username + Serial here! Please note: passing two empty strings runs the library
    // for 15 minutes during a debug session. Both values can also come
    // from configuration or an environment variable.
    // A broker (or broker+client) license is required for the broker workshops.
    // Free trial license (14 days, uninterrupted): https://www.indi-an.com/en/plccom/mqtt/mqtt-download/
    private static final String LICENSE_USER   = "";
    private static final String LICENSE_SERIAL = "";
    // =========================================================================

    private static final String BIND_ADDRESS = "127.0.0.1";

    // Standard MQTT ports (plain 1883, TLS 8883, secure WebSocket 8443). If one is
    // already in use locally, stop the other broker or change the value.
    private static final int TCP_PORT = 1883; // plain TCP        (mqtt)
    private static final int TLS_PORT = 8883; // TLS              (mqtts)
    private static final int WSS_PORT = 8443; // secure WebSocket (wss)

    // Where the auto-created server certificate is stored. On the first run the
    // certificate and key are generated here; later runs reuse them.
    private static final String PKI_FOLDER   = "./pki";
    private static final String CERT_ALIAS   = "workshop-broker";
    private static final String CERT_PASSWORD = "changeit";
    private static final int    CERT_VALID_DAYS = 365;

    private _13_SecureListeners() {
    }

    /**
     * Opens the console and runs the workshop.
     *
     * @param args command-line arguments; not used by this workshop
     */
    public static void main(String[] args) {
        PLCcomConsole.open("PLCcom MQTT Broker Workshop 13 - Secure Listeners (TLS / WSS)", 1000);
        try {
            start();
        } catch (Exception ex) {
            System.out.println();
            System.out.println("  The secure broker could not be started.");
            System.out.println("  Technical detail: " + ex.getMessage());
        } finally {
            PLCcomConsole.waitForEnter();
            PLCcomConsole.close();
        }
    }

    /**
     * Starts a broker with plain TCP, TLS and secure WebSocket listeners.
     *
     * @throws Exception if the certificate or a listener cannot be created
     */
    public static void start() throws Exception {
        System.out.println("╔════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║  What this workshop demonstrates                                           ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  Secure listeners: TLS (mqtts) and secure WebSocket (wss) on one broker,   ║");
        System.out.println("║  next to a plain TCP listener for migration.                               ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  The broker needs a server certificate. This workshop auto-creates a       ║");
        System.out.println("║  self-signed one in a local ./pki folder (production uses company PKI).    ║");
        System.out.println("║  A certificate can also be assigned per port (assignTlsCertificateToPort). ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  Pair with Client Workshop 41 (Secure Connection).                         ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  Expected result: the broker reports plain, TLS and WSS listener ports.    ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════════════╝");
        System.out.println();

        // -- Step 1: obtain a server certificate ------------------------------
        // PlccomMqttOwnCertificate loads an existing identity from the PKI 'own'
        // folder, or creates a self-signed one there if none exists yet. This is
        // the visible source of the broker's TLS identity; a production broker
        // would instead point the PKI at company-issued material.
        System.out.println("  Preparing the broker server certificate in " + PKI_FOLDER + " ...");
        PlccomMqttOwnCertificate serverCertificate = new PlccomMqttOwnCertificate(
                Paths.get(PKI_FOLDER), CERT_ALIAS, CERT_PASSWORD, CERT_VALID_DAYS);
        serverCertificate.loadOrCreateForServerHosts(Arrays.asList(BIND_ADDRESS));

        // -- Step 2: start plain, TLS and secure WebSocket listeners ----------
        // defaultTlsCertificate(...) supplies the identity for the TLS-based
        // listeners. The 2-argument tlsListener / secureWebSocketListener then
        // resolve their certificate from that default. The certificate was
        // prepared above with BIND_ADDRESS as SAN so strict clients can verify
        // both trust and endpoint identity. To give a specific port its own
        // certificate instead, use assignTlsCertificateToPort(port, cert).
        System.out.println("  Starting plain TCP, TLS and secure WebSocket listeners ...");
        PlccomMqttBroker broker = PlccomMqttBrokerFactory.builder(LICENSE_USER, LICENSE_SERIAL)
                .defaultTlsCertificate(serverCertificate)
                .tcpListener(BIND_ADDRESS, TCP_PORT)             // plain TCP (migration)
                .tlsListener(BIND_ADDRESS, TLS_PORT)             // mqtts:// (uses default cert)
                .secureWebSocketListener(BIND_ADDRESS, WSS_PORT) // wss:// (uses default cert)
                .start();

        try {
            // -- Step 3: report the bound endpoints ---------------------------
            java.util.List<Integer> ports = broker.getBoundPorts();
            System.out.println("  Broker is running with these endpoints:");
            System.out.println("    plain TCP        : mqtt://" + BIND_ADDRESS + ":" + ports.get(0));
            System.out.println("    TLS              : mqtts://" + BIND_ADDRESS + ":" + ports.get(1));
            System.out.println("    secure WebSocket : wss://" + BIND_ADDRESS + ":" + ports.get(2) + "/mqtt");
            System.out.println();
            System.out.println("  A client must trust this broker's certificate (see ./pki). Client");
            System.out.println("  Workshop 41 shows the client-side trust configuration.");
            System.out.println("  Success: encrypted listeners are ready alongside the plain TCP port.");
        } finally {
            broker.close();
            System.out.println("  Broker stopped.");
        }
    }
}

