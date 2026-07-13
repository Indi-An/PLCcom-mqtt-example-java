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
 * Workshop 14 - All Listener Types with Per-Port Certificates (Basics)
 *
 * WHAT THIS WORKSHOP SHOWS
 *   One broker serving all four MQTT transports at the same time - plain TCP,
 *   WebSocket (ws), TLS (mqtts) and secure WebSocket (wss) - all sharing the same
 *   broker state. It also shows per-port TLS certificates: the broker uses a
 *   default server certificate, but one specific TLS port is given its own
 *   certificate with assignTlsCertificateToPort(port, cert).
 *
 * WHEN YOU NEED THIS IN REAL APPLICATIONS
 *   A single broker often has to serve native MQTT clients, browser clients over
 *   WebSocket, and encrypted variants of both. Per-port certificates let one
 *   broker present different server identities on different ports (for example a
 *   public certificate on one port and an internal one on another).
 *
 * MATCHING CLIENT
 *   Client Workshop 11 connects over plain TCP or WS; Client Workshop 41 connects
 *   over TLS (mqtts) and secure WebSocket (wss). This workshop stays broker-only.
 *
 * MQTT TERMS USED HERE
 *   - Combined listeners   : several transports (TCP/WS/TLS/WSS) on one broker.
 *   - Default certificate  : the server identity used by TLS listeners that have
 *                            no port-specific assignment (defaultTlsCertificate).
 *   - Per-port certificate : a certificate bound to one concrete local port with
 *                            assignTlsCertificateToPort(port, cert); it wins over
 *                            the default for that port. The broker uses an
 *                            explicit certificate as-is; make sure its SAN list
 *                            matches the endpoint host clients will use.
 *
 * EXPECTED RESULT
 *   The broker reports its TCP, WS, TLS and WSS ports; the TLS port uses the
 *   assigned per-port certificate and the WSS port uses the default certificate.
 */
public final class _14_AllListenerTypes {

    // === YOUR PLCcom LICENSE =================================================
    // Replace both values with the user name and serial from your PLCcom license
    // e-mail. A broker (or broker+client) license is required.
    private static final String LICENSE_USER   = "<Enter your UserName here>";
    private static final String LICENSE_SERIAL = "<Enter your Serial here>";
    // =========================================================================

    private static final String BIND_ADDRESS = "127.0.0.1";

    // The standard MQTT ports for the four transports. Change them to fit your
    // deployment; if one is already in use locally (another broker), stop it first.
    private static final int TCP_PORT = 1883; // plain TCP  (mqtt)
    private static final int WS_PORT  = 8080; // plain WebSocket (ws)
    private static final int TLS_PORT = 8883; // TLS        (mqtts) - per-port certificate
    private static final int WSS_PORT = 8443; // secure WebSocket (wss) - default certificate

    // Auto-created certificates in the local PKI folder.
    private static final String PKI_FOLDER    = "./pki";
    private static final String DEFAULT_ALIAS = "workshop-broker-default";
    private static final String SPECIAL_ALIAS = "workshop-broker-tlsport";
    private static final String CERT_PASSWORD = "changeit";
    private static final int    CERT_VALID_DAYS = 365;

    private _14_AllListenerTypes() {
    }

    /**
     * Opens the console and runs the workshop.
     *
     * @param args command-line arguments; not used by this workshop
     */
    public static void main(String[] args) {
        PLCcomConsole.open("PLCcom MQTT Broker Workshop 14 - All Listener Types", 1000);
        try {
            start();
        } catch (Exception ex) {
            System.out.println();
            System.out.println("  The multi-listener broker could not be started.");
            System.out.println("  A configured port may already be in use. Technical detail: " + ex.getMessage());
        } finally {
            PLCcomConsole.waitForEnter();
            PLCcomConsole.close();
        }
    }

    /**
     * Starts one broker with TCP, WS, TLS and WSS listeners and a per-port
     * certificate on the TLS port.
     *
     * @throws Exception if a certificate or a listener cannot be created
     */
    public static void start() throws Exception {
        System.out.println("╔════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║  What this workshop demonstrates                                           ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  One broker serving all four transports at once: plain TCP, WebSocket (ws),║");
        System.out.println("║  TLS (mqtts) and secure WebSocket (wss) - sharing the same broker state.   ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  Per-port certificates: the broker uses a default server certificate, but  ║");
        System.out.println("║  one specific TLS port gets its OWN certificate via                        ║");
        System.out.println("║  assignTlsCertificateToPort(port, cert). That is how one broker can present║");
        System.out.println("║  different certificates on different ports.                                ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  Certificates are auto-created in ./pki (production uses company PKI).     ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  Expected result: TCP, WS, TLS and WSS ports are reported; the TLS port    ║");
        System.out.println("║  uses its assigned per-port certificate, the WSS port the default one.     ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════════════╝");
        System.out.println();

        // -- Step 1: obtain two server certificates ---------------------------
        // A default certificate for the "normal" TLS listeners, and a separate
        // certificate that we will bind to one specific TLS port. Both are loaded
        // from the PKI 'own' folder, or created there on first run.
        System.out.println("  Preparing certificates in " + PKI_FOLDER + " ...");
        PlccomMqttOwnCertificate defaultCertificate = new PlccomMqttOwnCertificate(
                Paths.get(PKI_FOLDER), DEFAULT_ALIAS, CERT_PASSWORD, CERT_VALID_DAYS);
        PlccomMqttOwnCertificate perPortCertificate = new PlccomMqttOwnCertificate(
                Paths.get(PKI_FOLDER), SPECIAL_ALIAS, CERT_PASSWORD, CERT_VALID_DAYS);
        defaultCertificate.loadOrCreateForServerHosts(Arrays.asList(BIND_ADDRESS));
        perPortCertificate.loadOrCreateForServerHosts(Arrays.asList(BIND_ADDRESS));

        // -- Step 2: start all four listeners on one broker -------------------
        // defaultTlsCertificate(...) provides the identity for TLS listeners with
        // no port-specific assignment. assignTlsCertificateToPort(TLS_PORT, ...)
        // overrides the identity for exactly that port. The 2-argument
        // tlsListener / secureWebSocketListener then resolve their certificate
        // from these settings (per-port assignment wins, otherwise the default).
        // Because explicit certificates are used as-is, this workshop prepares
        // both certificate files above with BIND_ADDRESS in the SAN list.
        System.out.println("  Starting TCP, WS, TLS and WSS listeners on one broker ...");
        PlccomMqttBroker broker = PlccomMqttBrokerFactory.builder(LICENSE_USER, LICENSE_SERIAL)
                .defaultTlsCertificate(defaultCertificate)
                .assignTlsCertificateToPort(TLS_PORT, perPortCertificate)
                .addTcpListener(BIND_ADDRESS, TCP_PORT)          // plain TCP
                .webSocketListener(BIND_ADDRESS, WS_PORT)        // plain WebSocket
                .tlsListener(BIND_ADDRESS, TLS_PORT)             // mqtts (per-port certificate)
                .secureWebSocketListener(BIND_ADDRESS, WSS_PORT) // wss   (default certificate)
                .start();

        try {
            // -- Step 3: report the endpoints ---------------------------------
            java.util.List<Integer> ports = broker.getBoundPorts();
            System.out.println("  Broker is running with all four listener types:");
            System.out.println("    plain TCP        : mqtt://" + BIND_ADDRESS + ":" + ports.get(0));
            System.out.println("    plain WebSocket  : ws://" + BIND_ADDRESS + ":" + ports.get(1) + "/mqtt");
            System.out.println("    TLS              : mqtts://" + BIND_ADDRESS + ":" + ports.get(2)
                    + "   (per-port certificate '" + SPECIAL_ALIAS + "')");
            System.out.println("    secure WebSocket : wss://" + BIND_ADDRESS + ":" + ports.get(3) + "/mqtt"
                    + "  (default certificate '" + DEFAULT_ALIAS + "')");
            System.out.println();
            System.out.println("  Success: one broker serves TCP/WS/TLS/WSS, and the TLS port presents its");
            System.out.println("  own per-port certificate while the WSS port uses the default one.");
        } finally {
            broker.close();
            System.out.println("  Broker stopped.");
        }
    }
}

