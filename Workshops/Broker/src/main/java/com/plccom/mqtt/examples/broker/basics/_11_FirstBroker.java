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
 * Workshop 11 - First Broker (Setup)
 *
 * WHAT THIS WORKSHOP SHOWS
 *   The smallest standalone MQTT broker: build it, start a plain TCP listener,
 *   read the bound port, keep it running and stop it cleanly. This is the broker
 *   counterpart to Client Workshop 11 and the right file to read first on the
 *   broker side.
 *
 * WHEN YOU NEED THIS IN REAL APPLICATIONS
 *   Whenever an application should host its own MQTT broker - for a self-contained
 *   product, an edge gateway or a test environment - instead of depending on an
 *   external broker installation.
 *
 * MATCHING CLIENT
 *   Pair this with Client Workshop 11 (First Connection), or connect any MQTT
 *   client (for example mosquitto_sub / MQTT Explorer) to the printed address.
 *
 * MQTT TERMS USED HERE
 *   - Broker         : the server that accepts MQTT connections and routes
 *                      messages between publishers and subscribers.
 *   - TCP listener   : the network entry point clients connect to.
 *   - Bound port     : the actual local port the listener is using (port 0 asks
 *                      the operating system to pick a free one).
 *   - Default policy : with no access validator configured, the broker allows
 *                      anonymous clients and all topic operations.
 *
 * EXPECTED RESULT
 *   The broker starts and prints the TCP address it is listening on, then stops
 *   cleanly when you press ENTER.
 */
public final class _11_FirstBroker {

    // === YOUR PLCcom LICENSE =================================================
    // Replace both values with the user name and serial from your PLCcom license
    // e-mail. A broker (or broker+client) license is required for the broker
    // workshops.
    private static final String LICENSE_USER   = "<Enter your UserName here>";
    private static final String LICENSE_SERIAL = "<Enter your Serial here>";
    // =========================================================================

    // The bind address and port for the TCP listener. 127.0.0.1 keeps the broker
    // local to this machine; port 0 lets the operating system choose a free port.
    private static final String BIND_ADDRESS = "127.0.0.1";
    private static final int    TCP_PORT     = 0;

    private _11_FirstBroker() {
    }

    /**
     * Opens the console and runs the workshop.
     *
     * @param args command-line arguments; not used by this workshop
     */
    public static void main(String[] args) {
        PLCcomConsole.open("PLCcom MQTT Broker Workshop 11 - First Broker", 1000);
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
     * Starts a minimal standalone broker and prints its bound address.
     *
     * @throws Exception if the broker cannot be started
     */
    public static void start() throws Exception {
        System.out.println("╔════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║  What this workshop demonstrates                                           ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  The smallest standalone MQTT broker: start it, read the bound port,       ║");
        System.out.println("║  keep it running and stop it cleanly.                                      ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  With no access validator configured, the broker uses its default open     ║");
        System.out.println("║  policy (anonymous clients allowed) - convenient for local tests.          ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  Pair with Client Workshop 11 (First Connection), or connect any MQTT      ║");
        System.out.println("║  client to the printed address.                                            ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  Expected result: the broker reports the TCP port it is listening on.      ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════════════╝");
        System.out.println();

        // -- Step 1: build and start the broker -------------------------------
        // The factory builder creates the broker with the license visible above.
        // tcpListener(...) adds one plain TCP entry point; start() binds it and
        // returns the running broker. No access validator is configured, so the
        // broker uses its default open policy.
        System.out.println("  Starting the broker ...");
        PlccomMqttBroker broker = PlccomMqttBrokerFactory.builder(LICENSE_USER, LICENSE_SERIAL)
                .tcpListener(BIND_ADDRESS, TCP_PORT)
                .start();

        try {
            // -- Step 2: report the bound endpoint ----------------------------
            // getBoundPort() returns the actual port (useful when TCP_PORT was 0).
            int port = broker.getBoundPort();
            System.out.println("  Broker is running.");
            System.out.println("  Listening on : mqtt://" + BIND_ADDRESS + ":" + port);
            System.out.println();
            System.out.println("  Connect Client Workshop 11 (or any MQTT client) to that address.");
            System.out.println("  Success: the standalone broker started and is accepting connections.");
        } finally {
            // -- Step 3: stop the broker cleanly ------------------------------
            // close() stops the listeners and releases the broker's resources.
            broker.close();
            System.out.println("  Broker stopped.");
        }
    }
}
