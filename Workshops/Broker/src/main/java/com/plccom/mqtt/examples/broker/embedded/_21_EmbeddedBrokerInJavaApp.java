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

package com.plccom.mqtt.examples.broker.embedded;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.plccom.console.PLCcomConsole;
import com.plccom.mqtt.broker.MqttBrokerListenerHandle;
import com.plccom.mqtt.broker.PlccomMqttBroker;
import com.plccom.mqtt.broker.PlccomMqttBrokerFactory;

/**
 * Workshop 21 - Embedded Broker in a Java Application (Embedded)
 *
 * WHAT THIS WORKSHOP SHOWS
 *   How a Java application can host the broker itself and exchange data with it
 *   directly, without opening a network MQTT client back into its own process.
 *   It covers the whole embedded surface in one scenario: owning the broker
 *   lifecycle, publishing in-process values (text, numbers, JSON, with publish
 *   options and in a batch) and reacting to messages with an in-process listener
 *   (a command handler). External network MQTT clients keep working the whole
 *   time - the in-process API is just an additional, trusted local path.
 *
 * WHEN YOU NEED THIS IN REAL APPLICATIONS
 *   Self-contained products and edge gateways that already hold values in memory
 *   and want to expose them over MQTT, and that must react to incoming commands
 *   in the same process - all without the overhead of a loopback client.
 *
 * MATCHING CLIENT
 *   External participants can use Client Workshop 11 (basic) or 24 (JSON). This
 *   workshop stays broker-only and does not import the client API.
 *
 * MQTT TERMS USED HERE
 *   - Embedded broker      : the broker runs inside the application process; the
 *                            application owns start() and close().
 *   - In-process publish   : broker.publish(...) turns an application value into a
 *                            normal MQTT publication (no network client involved).
 *   - Publish options      : QoS, retain, and MQTT 5 metadata (Content Type,
 *                            Payload Format, User Properties).
 *   - In-process listener  : broker.listen(filter) delivers matching messages to
 *                            Java code directly (here a command handler).
 *
 * EXPECTED RESULT
 *   The in-process command handler receives a command published in-process, and
 *   several application values (telemetry, a batch, and JSON) are published into
 *   the broker.
 */
public final class _21_EmbeddedBrokerInJavaApp {

    // === YOUR PLCcom LICENSE =================================================
    // Replace both values with the user name and serial from your PLCcom license
    // e-mail. A broker (or broker+client) license is required.
    private static final String LICENSE_USER   = "<Enter your UserName here>";
    private static final String LICENSE_SERIAL = "<Enter your Serial here>";
    // =========================================================================

    private _21_EmbeddedBrokerInJavaApp() {
    }

    /**
     * Opens the console and runs the workshop.
     *
     * @param args command-line arguments; not used by this workshop
     */
    public static void main(String[] args) {
        PLCcomConsole.open("PLCcom MQTT Broker Workshop 21 - Embedded Broker in a Java Application", 1000);
        try {
            start();
        } catch (Exception ex) {
            System.out.println();
            System.out.println("  The embedded broker scenario failed.");
            System.out.println("  Technical detail: " + ex.getMessage());
        } finally {
            PLCcomConsole.waitForEnter();
            PLCcomConsole.close();
        }
    }

    /**
     * Runs the embedded publish/listen scenario inside one process.
     *
     * @throws Exception if the broker cannot be started or the in-process
     *         delivery path fails
     */
    public static void start() throws Exception {
        System.out.println("╔════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║  What this workshop demonstrates                                           ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  An MQTT broker embedded in your own Java application: your code owns the  ║");
        System.out.println("║  broker lifecycle and exchanges data WITHOUT any network client.           ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  In-process publish: turn values already in memory into MQTT publications  ║");
        System.out.println("║  (text, numbers, JSON; with QoS / retain / MQTT 5 metadata; in batches).   ║");
        System.out.println("║  In-process listen: react to matching topics directly in Java (e.g. a      ║");
        System.out.println("║  command handler), while external MQTT clients still work normally.        ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  Pair with Client Workshops 11 and 24 for the external side.               ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  Expected result: an in-process command is received by the Java listener,  ║");
        System.out.println("║  and application values are published into the broker.                     ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════════════╝");
        System.out.println();

        // -- Step 1: the application owns the broker --------------------------
        System.out.println("  Starting the embedded broker (owned by this application) ...");
        PlccomMqttBroker broker = PlccomMqttBrokerFactory.builder(LICENSE_USER, LICENSE_SERIAL)
                .tcpPort(0)
                .start();

        MqttBrokerListenerHandle commandListener = null;
        try {
            System.out.println("  Broker running on mqtt://127.0.0.1:" + broker.getBoundPort());

            // -- Step 2: react to commands with an in-process listener --------
            // broker.listen(...) delivers matching messages to Java code directly.
            // A '+' matches exactly one topic level, so this handles a command on
            // any line. Keep the callback fast; offload slow work to an executor.
            final CountDownLatch commandReceived = new CountDownLatch(1);
            commandListener = broker.listen("plant/+/command")
                    .qos1()
                    .onMessage(message -> {
                        System.out.println("    [command handler] topic=" + message.topic()
                                + " payload=" + message.payloadAsString());
                        commandReceived.countDown();
                    })
                    .start();
            System.out.println("  In-process command handler is listening on plant/+/command.");

            // -- Step 3: publish application telemetry with publish options ----
            // These values already exist in the process; broker.publish(...) turns
            // them into MQTT publications. Here we add QoS, retain and MQTT 5
            // metadata so subscribers get context and the latest value is kept.
            System.out.println();
            System.out.println("  Publishing telemetry with QoS 1, retain and MQTT 5 metadata ...");
            broker.publish("plant/line1/temperature", 42.8)
                    .qos1()
                    .retain()
                    .payloadFormatUtf8()
                    .contentType("text/plain")
                    .userProperty("unit", "celsius")
                    .send();

            // -- Step 4: publish a batch of readings --------------------------
            // A "batch" is simply several publications; each stays an independent
            // MQTT message, so normal per-topic delivery and retain rules apply.
            System.out.println("  Publishing a batch of readings ...");
            double[] readings = { 41.9, 42.1, 42.4, 42.8 };
            for (int i = 0; i < readings.length; i++) {
                broker.publish("plant/line1/history/" + i, readings[i]).qos1().send();
            }
            System.out.println("    published " + readings.length + " readings, each as its own message.");

            // -- Step 5: publish a JSON payload -------------------------------
            // MQTT carries bytes; here the application formats a small JSON object
            // and marks it as application/json so any receiver recognizes it.
            System.out.println("  Publishing a JSON status payload ...");
            String json = "{\"machine\":\"line1\",\"running\":true,\"rpm\":1480}";
            broker.publish("plant/line1/status", json)
                    .qos1()
                    .retain()
                    .payloadFormatUtf8()
                    .contentType("application/json")
                    .send();

            // -- Step 6: drive the command flow in-process --------------------
            // To show the command handler working without an external client, the
            // application publishes a command in-process. In production this would
            // usually arrive from a network client; the handler code is the same.
            System.out.println();
            System.out.println("  Publishing an in-process command to trigger the handler ...");
            broker.publish("plant/line1/command", "start").qos1().send();

            if (!commandReceived.await(3, TimeUnit.SECONDS)) {
                throw new IllegalStateException("The in-process command handler did not receive the command.");
            }

            System.out.println();
            System.out.println("  Success: the application published values and handled a command in-process.");
        } finally {
            // -- Step 7: clean shutdown ---------------------------------------
            if (commandListener != null) {
                commandListener.close();
            }
            broker.close();
            System.out.println("  Broker stopped.");
        }
    }
}
