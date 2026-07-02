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

import java.util.concurrent.atomic.AtomicInteger;

import com.plccom.console.PLCcomConsole;
import com.plccom.mqtt.client.MqttPublishResult;
import com.plccom.mqtt.client.PlccomMqttClient;
import com.plccom.mqtt.client.PlccomMqttClientFactory;
import com.plccom.mqtt.client.PlccomMqttConnectResult;
import com.plccom.mqtt.diagnostics.PlccomMqttDiagnosticEvent;
import com.plccom.mqtt.diagnostics.PlccomMqttDiagnosticLevel;

/**
 * Workshop 42 - Diagnostics and Error Handling (Security &amp; Operations)
 *
 * WHAT THIS WORKSHOP SHOWS
 *   How to observe what the client is doing and how to handle failures cleanly,
 *   without tying the SDK to a specific logging framework. It covers three
 *   things: a diagnostic listener that receives structured events, the
 *   diagnosticLevel(...) filter that controls how much is delivered, and the
 *   OperationResult/Quality that every high-level call returns.
 *
 * WHEN YOU NEED THIS IN REAL APPLICATIONS
 *   Production operation and support: forward diagnostics into your own logging
 *   or monitoring, react to recoverable problems (a failing message handler, a
 *   dropped connection) and turn library results into clear application errors.
 *
 * BROKER FOR THIS WORKSHOP
 *   This client workshop needs a running MQTT broker. Start Broker Workshop 11
 *   (First Broker) first, or point BROKER_ENDPOINT below at any MQTT broker of
 *   your choice (for example a local Mosquitto). No broker is embedded here, so
 *   a client-only PLCcom license is enough. Override with
 *   -Dplccom.mqtt.workshop.endpoint=mqtt://host:port .
 *
 * MQTT TERMS USED HERE
 *   - Diagnostic event   : a structured SDK-to-application signal with severity,
 *                          category, code and message. It never contains
 *                          passwords or payload bytes.
 *   - Diagnostic level   : OFF, FATAL, ERROR, WARNING, INFO or DEBUG - the
 *                          minimum severity delivered to the listener.
 *   - OperationResult    : the return value of connect/subscribe/publish, whose
 *                          Quality tells you whether the operation was Good.
 *
 * EXPECTED RESULT
 *   A deliberately failing message handler is reported as a diagnostic event
 *   (the connection keeps working), operation Quality is printed, and a
 *   deliberate misuse is caught as an exception.
 */
public final class _42_DiagnosticsAndErrorHandling {

    // === YOUR PLCcom LICENSE =================================================
    // Replace both values with the user name and serial from your PLCcom license
    // e-mail. A client-only license is sufficient for the client workshops.
    private static final String LICENSE_USER   = "<Enter your UserName here>";
    private static final String LICENSE_SERIAL = "<Enter your Serial here>";
    // =========================================================================

    // === BROKER TO CONNECT TO ================================================
    // Start Broker Workshop 11, or change this to any MQTT broker you like.
    // The scheme must be mqtt:// / mqtts:// / ws:// / wss:// - not tcp:// .
    private static final String BROKER_ENDPOINT = "mqtt://localhost:1883";
    // =========================================================================

    private static final String TOPIC = "plccom/workshop/42/diagnostics";

    private _42_DiagnosticsAndErrorHandling() {
    }

    /**
     * Opens the console and runs the workshop.
     *
     * @param args command-line arguments; not used by this workshop
     */
    public static void main(String[] args) {
        PLCcomConsole.open("PLCcom MQTT Workshop 42 - Diagnostics and Error Handling", 1000);
        try {
            start();
        } catch (Exception ex) {
            System.out.println();
            System.out.println("  This workshop needs a running MQTT broker at " + brokerEndpoint() + ".");
            System.out.println("  Start Broker Workshop 11 (or a local Mosquitto) and run again.");
            System.out.println("  Technical detail: " + ex.getMessage());
        } finally {
            PLCcomConsole.waitForEnter();
            PLCcomConsole.close();
        }
    }

    /**
     * Demonstrates diagnostics, level filtering, quality checks and error
     * handling.
     *
     * @throws Exception if the broker cannot be reached
     */
    public static void start() throws Exception {
        System.out.println("╔════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║  What this workshop demonstrates                                           ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  Diagnostics and error handling: see what the client is doing and react to ║");
        System.out.println("║  problems without a fixed logging dependency.                              ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  A diagnostic listener receives structured events (severity, category,     ║");
        System.out.println("║  code, message). diagnosticLevel(...) controls how much is delivered:      ║");
        System.out.println("║  OFF, FATAL, ERROR, WARNING, INFO or DEBUG.                                ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  Every operation also returns an OperationResult with a Quality you can    ║");
        System.out.println("║  check explicitly, and failures surface as exceptions you can catch.       ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  Expected result: a deliberately failing message callback is reported as a ║");
        System.out.println("║  diagnostic event, while the connection keeps working.                     ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════════════╝");
        System.out.println();

        System.out.println("  Broker endpoint : " + brokerEndpoint());
        System.out.println();

        // Count delivered diagnostics so we can report at the end.
        final AtomicInteger diagnosticCount = new AtomicInteger();

        // -- Step 1: build a client with a diagnostic listener and a level ----
        // The listener turns SDK events into whatever the application needs; here
        // we just print them. diagnosticLevel(WARNING) means INFO/DEBUG events are
        // filtered out and only WARNING and worse are delivered. Set it to OFF to
        // silence diagnostics, or DEBUG while investigating a problem.
        PlccomMqttClient client = PlccomMqttClientFactory.builder(LICENSE_USER, LICENSE_SERIAL)
                .identifier("plccom-workshop-42")
                .endpoint(brokerEndpoint())
                .mqtt5()
                .keepAliveSeconds(30)
                .diagnosticLevel(PlccomMqttDiagnosticLevel.WARNING)
                .onDiagnostic(event -> {
                    diagnosticCount.incrementAndGet();
                    printDiagnostic(event);
                })
                .build();

        try {
            // -- Step 2: connect and check the result Quality explicitly ------
            // Every high-level call returns an OperationResult. Rather than assume
            // success, we inspect the Quality. On failure, getMessage() and
            // getInnerException() carry the details.
            PlccomMqttConnectResult connectResult = client.connect();
            System.out.println("  Connect quality : " + connectResult.getQuality()
                    + " (isGood=" + connectResult.isGood() + ")");
            if (!connectResult.isGood()) {
                throw new IllegalStateException("CONNECT failed: " + connectResult.getMessage());
            }

            // -- Step 3: provoke a recoverable diagnostic ---------------------
            // We subscribe with a message handler that throws. When a message
            // arrives, the SDK catches the exception so it does NOT break the MQTT
            // connection, and instead reports it as a diagnostic event. This is a
            // realistic failure: a bug in application callback code.
            System.out.println();
            System.out.println("  Subscribing with a message handler that deliberately throws ...");
            client.subscribe(TOPIC)
                    .qos1()
                    .onMessage(message -> {
                        throw new RuntimeException("simulated handler failure while processing a message");
                    })
                    .send();

            System.out.println("  Publishing a message to trigger the failing handler ...");
            client.publish(TOPIC, "trigger").qos1().payloadFormatUtf8().send();

            // Give the callback dispatcher a moment to run and report.
            Thread.sleep(800);
            System.out.println("  Diagnostic events delivered so far: " + diagnosticCount.get()
                    + " (the connection is still usable)");

            // -- Step 4: turn a library failure into a clear application error -
            // Here we deliberately misuse the API to show error handling: we close
            // the client and then try to publish. The SDK reports the problem
            // through the result/exception model, which we catch and report.
            System.out.println();
            System.out.println("  Demonstrating error handling on a deliberate misuse (publish after close):");
            client.close();
            try {
                MqttPublishResult afterClose = client.publish(TOPIC, "too late").qos1().send();
                // Depending on the implementation this either returns a bad-quality
                // result or throws; handle both paths clearly.
                if (!afterClose.isGood()) {
                    System.out.println("    handled as bad result : "
                            + afterClose.getQuality() + " - " + afterClose.getMessage());
                } else {
                    System.out.println("    unexpectedly succeeded (implementation-dependent).");
                }
            } catch (RuntimeException ex) {
                System.out.println("    handled as exception  : " + ex.getClass().getSimpleName()
                        + " - " + ex.getMessage());
            }

            System.out.println();
            System.out.println("  Success: diagnostics were delivered and the failure was handled cleanly.");
        } finally {
            // close() is safe to call again even if we already closed above.
            client.close();
        }
    }

    // Prints one diagnostic event. An application would instead forward this to
    // its logger, monitoring system or user interface.
    private static void printDiagnostic(PlccomMqttDiagnosticEvent event) {
        System.out.println("    [diagnostic] " + event.getSeverity()
                + " " + event.getCategory()
                + " " + event.getCode()
                + " : " + event.getMessage());
    }

    private static String brokerEndpoint() {
        return System.getProperty("plccom.mqtt.workshop.endpoint", BROKER_ENDPOINT);
    }
}
