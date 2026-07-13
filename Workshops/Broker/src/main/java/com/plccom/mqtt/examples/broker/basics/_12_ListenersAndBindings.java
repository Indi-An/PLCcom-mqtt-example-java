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

import com.plccom.console.PLCcomConsole;
import com.plccom.mqtt.broker.PlccomMqttBroker;
import com.plccom.mqtt.broker.PlccomMqttBrokerFactory;

/**
 * Workshop 12 - Listeners and Bind Addresses (Setup)
 *
 * WHAT THIS WORKSHOP SHOWS
 *   Where and how a broker accepts clients. It explains the bind address (which
 *   network interface accepts connections) and shows that one broker can expose
 *   several listeners at once - here two TCP ports and a WebSocket listener - all
 *   sharing the same broker state (sessions, subscriptions, retained messages).
 *
 * WHEN YOU NEED THIS IN REAL APPLICATIONS
 *   Deployment and firewall control: bind only to loopback for local tests, bind
 *   to a specific interface in production, offer a plain TCP port for native
 *   clients and a WebSocket port for browser or proxy clients at the same time.
 *
 * MATCHING CLIENT
 *   Connect any MQTT client (Client Workshop 11, or a browser MQTT client for the
 *   WebSocket port) to one of the printed endpoints.
 *
 * MQTT TERMS USED HERE
 *   - Bind address     : the local interface the listener binds to. 127.0.0.1 is
 *                        loopback (local only); 0.0.0.0 accepts from any
 *                        interface (use with a firewall and authentication).
 *   - Multiple listeners: several entry points on ONE broker instance.
 *   - WebSocket listener: MQTT tunneled over WebSocket, useful for browsers and
 *                        HTTP-friendly infrastructure.
 *
 * EXPECTED RESULT
 *   One broker reports several listener endpoints (two TCP ports and one
 *   WebSocket port).
 */
public final class _12_ListenersAndBindings {

    // === YOUR PLCcom LICENSE =================================================
    // Replace both values with the user name and serial from your PLCcom license
    // e-mail. A broker (or broker+client) license is required.
    private static final String LICENSE_USER   = "<Enter your UserName here>";
    private static final String LICENSE_SERIAL = "<Enter your Serial here>";
    // =========================================================================

    // Loopback keeps the workshop local. Change to a real interface address (or
    // 0.0.0.0) once authentication is configured for a networked deployment.
    private static final String BIND_ADDRESS = "127.0.0.1";

    // Standard MQTT ports. A second TCP listener cannot reuse 1883, so it takes the
    // adjacent 1884. If a port is already in use locally, stop the other broker.
    private static final int TCP_PORT_1 = 1883; // first plain TCP listener  (standard)
    private static final int TCP_PORT_2 = 1884; // second plain TCP listener (1883 is taken)
    private static final int WS_PORT    = 8080; // WebSocket listener

    private _12_ListenersAndBindings() {
    }

    /**
     * Opens the console and runs the workshop.
     *
     * @param args command-line arguments; not used by this workshop
     */
    public static void main(String[] args) {
        PLCcomConsole.open("PLCcom MQTT Broker Workshop 12 - Listeners and Bind Addresses", 1000);
        try {
            start();
        } catch (Exception ex) {
            System.out.println();
            System.out.println("  The broker could not be started.");
            System.out.println("  Technical detail: " + ex.getMessage());
        } finally {
            PLCcomConsole.waitForEnter();
            PLCcomConsole.close();
        }
    }

    /**
     * Starts one broker with several listeners and prints all bound ports.
     *
     * @throws Exception if a listener cannot be started
     */
    public static void start() throws Exception {
        System.out.println("╔════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║  What this workshop demonstrates                                           ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  Listeners and bind addresses: where and how the broker accepts clients.   ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  tcpPort(p) / tcpListener(addr, p) choose the port and network interface.  ║");
        System.out.println("║  127.0.0.1 is loopback (local only); 0.0.0.0 would accept from anywhere.   ║");
        System.out.println("║  Several listeners (e.g. two TCP ports plus WebSocket) can run on ONE      ║");
        System.out.println("║  broker - they all share the same broker state.                            ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  Pair with any MQTT client on the printed ports.                           ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  Expected result: one broker reports several listener endpoints.           ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════════════╝");
        System.out.println();

        // -- Step 1: configure several listeners on one broker ----------------
        // tcpListener(...) sets the first listener; addTcpListener(...) appends a
        // second TCP entry point; webSocketListener(...) adds an MQTT-over-
        // WebSocket entry point. All of them belong to the SAME broker, so a
        // message published on one listener is visible to subscribers on another.
        System.out.println("  Starting one broker with two TCP listeners and one WebSocket listener ...");
        PlccomMqttBroker broker = PlccomMqttBrokerFactory.builder(LICENSE_USER, LICENSE_SERIAL)
                .tcpListener(BIND_ADDRESS, TCP_PORT_1)    // first TCP entry point
                .addTcpListener(BIND_ADDRESS, TCP_PORT_2) // second TCP entry point
                .webSocketListener(BIND_ADDRESS, WS_PORT) // WebSocket entry point
                .start();

        try {
            // -- Step 2: report every bound port ------------------------------
            // getBoundPorts() returns the ports in the order the listeners were
            // configured, so we can label them.
            java.util.List<Integer> ports = broker.getBoundPorts();
            System.out.println("  Broker is running with these endpoints:");
            System.out.println("    TCP #1    : mqtt://" + BIND_ADDRESS + ":" + ports.get(0));
            System.out.println("    TCP #2    : mqtt://" + BIND_ADDRESS + ":" + ports.get(1));
            System.out.println("    WebSocket : ws://" + BIND_ADDRESS + ":" + ports.get(2) + "/mqtt");
            System.out.println("    all ports : " + ports);
            System.out.println();
            System.out.println("  Success: one broker is serving three listeners over shared broker state.");
        } finally {
            broker.close();
            System.out.println("  Broker stopped.");
        }
    }
}
