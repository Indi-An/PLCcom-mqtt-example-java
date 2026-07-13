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

package com.plccom.mqtt.examples.broker.service;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.plccom.console.PLCcomConsole;
import com.plccom.mqtt.broker.MqttBrokerAdminSnapshot;
import com.plccom.mqtt.broker.MqttBrokerListenerHandle;
import com.plccom.mqtt.broker.MqttBrokerPublishResult;
import com.plccom.mqtt.broker.PlccomMqttBroker;
import com.plccom.mqtt.broker.PlccomMqttBrokerFactory;
import com.plccom.mqtt.diagnostics.PlccomMqttDiagnosticLevel;

/**
 * Workshop 42 - Admin Snapshot and Diagnostics (Operations)
 *
 * WHAT THIS WORKSHOP SHOWS
 *   How to operate and observe a running broker. The read-only admin snapshot
 *   reports running state, listener ports, retained messages, sessions and
 *   counters without changing anything. resetCounters() starts a fresh
 *   measurement window while keeping retained messages and sessions intact. A
 *   diagnostic listener receives structured events; a failing in-process
 *   listener is contained and reported instead of stopping the broker.
 *
 * WHEN YOU NEED THIS IN REAL APPLICATIONS
 *   Status pages, health endpoints and support logging for an embedded broker,
 *   plus forwarding recoverable broker problems into your own monitoring without
 *   tying the SDK to a specific logging framework.
 *
 * MATCHING CLIENT
 *   Client Workshop 42 (Diagnostics and Error Handling) shows the same idea on
 *   the client side. This workshop stays broker-only.
 *
 * MQTT TERMS USED HERE
 *   - Admin snapshot   : an immutable, read-only view of broker state.
 *   - Counters         : diagnostic activity counts (not durable audit data).
 *   - Diagnostic event : a structured severity/category/code/message signal.
 *   - Diagnostic level : OFF, FATAL, ERROR, WARNING, INFO or DEBUG.
 *
 * EXPECTED RESULT
 *   The snapshot and counters are printed, a counter reset keeps the retained
 *   message, and a deliberately failing listener is reported as a diagnostic
 *   while the broker stays alive.
 */
public final class _42_AdminAndDiagnostics {

    // === YOUR PLCcom LICENSE =================================================
    // Replace both values with the user name and serial from your PLCcom license
    // e-mail. A broker (or broker+client) license is required.
    private static final String LICENSE_USER   = "<Enter your UserName here>";
    private static final String LICENSE_SERIAL = "<Enter your Serial here>";
    // =========================================================================

    private static final String STATE_TOPIC = "admin/workshop/state";
    private static final String PROBE_TOPIC = "admin/workshop/probe";

    private _42_AdminAndDiagnostics() {
    }

    /**
     * Opens the console and runs the workshop.
     *
     * @param args command-line arguments; not used by this workshop
     */
    public static void main(String[] args) {
        PLCcomConsole.open("PLCcom MQTT Broker Workshop 42 - Admin Snapshot and Diagnostics", 1000);
        try {
            start();
        } catch (Exception ex) {
            System.out.println();
            System.out.println("  The admin/diagnostics scenario failed.");
            System.out.println("  Technical detail: " + ex.getMessage());
        } finally {
            PLCcomConsole.waitForEnter();
            PLCcomConsole.close();
        }
    }

    /**
     * Reads a snapshot, resets counters and reports a provoked listener failure.
     *
     * @throws Exception if the broker cannot be started or the diagnostic is not
     *         delivered
     */
    public static void start() throws Exception {
        System.out.println("╔════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║  What this workshop demonstrates                                           ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  Operating the broker: read-only admin snapshots, safe counter reset and   ║");
        System.out.println("║  structured diagnostics.                                                   ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  admin().snapshot() reports running state, ports, retained messages,       ║");
        System.out.println("║  sessions and counters without changing anything. resetCounters() starts a ║");
        System.out.println("║  fresh measurement window but keeps retained messages and sessions intact. ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  A diagnostic listener receives structured events; a failing in-process    ║");
        System.out.println("║  listener is contained and reported instead of stopping the broker.        ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  Expected result: snapshot and counters are shown, and a provoked listener ║");
        System.out.println("║  failure is reported as a diagnostic while the broker stays alive.         ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════════════╝");
        System.out.println();

        // -- Step 1: start the broker with a diagnostic listener --------------
        final CountDownLatch listenerFailureReported = new CountDownLatch(1);
        PlccomMqttBroker broker = PlccomMqttBrokerFactory.builder(LICENSE_USER, LICENSE_SERIAL)
                .tcpPort(1883) // 1883 is the standard MQTT-over-TCP port
                .diagnosticLevel(PlccomMqttDiagnosticLevel.WARNING)
                .onDiagnostic(event -> {
                    System.out.println("    [diagnostic] " + event.getSeverity()
                            + " " + event.getCategory() + " " + event.getCode()
                            + " : " + event.getMessage());
                    if ("BROKER_APPLICATION_LISTENER_FAILED".equals(event.getCode())) {
                        listenerFailureReported.countDown();
                    }
                })
                .start();

        MqttBrokerListenerHandle failingListener = null;
        try {
            // -- Step 2: create some state and read a snapshot ----------------
            broker.publish(STATE_TOPIC, "operational").qos1().retain().payloadFormatUtf8().send();

            MqttBrokerAdminSnapshot snapshot = broker.admin().snapshot();
            System.out.println("  Admin snapshot:");
            System.out.println("    running            : " + snapshot.getStatistics().isRunning());
            System.out.println("    bound ports        : " + snapshot.getBoundPorts());
            System.out.println("    retained messages  : " + snapshot.getRetainedMessages().size());
            System.out.println("    application publishes: " + snapshot.getCounters().getApplicationPublishes());

            // -- Step 3: reset counters (state is preserved) ------------------
            System.out.println();
            System.out.println("  Resetting counters (retained messages and sessions are kept) ...");
            broker.admin().resetCounters();
            MqttBrokerAdminSnapshot afterReset = broker.admin().snapshot();
            System.out.println("    application publishes after reset: "
                    + afterReset.getCounters().getApplicationPublishes());
            System.out.println("    retained messages after reset    : "
                    + afterReset.getRetainedMessages().size() + " (unchanged)");

            // -- Step 4: provoke and observe a recoverable diagnostic ---------
            // A local listener that throws simulates a bug in application callback
            // code. The broker contains the exception (so the broker keeps running)
            // and reports it as a diagnostic event.
            System.out.println();
            System.out.println("  Registering a listener that deliberately throws, then publishing ...");
            failingListener = broker.listen(PROBE_TOPIC)
                    .qos1()
                    .onMessage(message -> {
                        throw new IllegalStateException("simulated broker application listener failure");
                    })
                    .start();

            MqttBrokerPublishResult result = broker.publish(PROBE_TOPIC, "probe").qos1().send();
            System.out.println("    in-process listener failures reported by publish: "
                    + result.getApplicationListenerFailures());

            if (!listenerFailureReported.await(3, TimeUnit.SECONDS)) {
                throw new IllegalStateException("The expected listener-failure diagnostic was not delivered.");
            }
            System.out.println("    broker still running: " + broker.isRunning());

            System.out.println();
            System.out.println("  Success: state was observed safely and a failure was reported, not fatal.");
        } finally {
            if (failingListener != null) {
                failingListener.close();
            }
            broker.close();
            System.out.println("  Broker stopped.");
        }
    }
}
