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

package com.plccom.mqtt.examples.client.resilience;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.plccom.console.PLCcomConsole;
import com.plccom.mqtt.client.MqttMessage;
import com.plccom.mqtt.client.PlccomMqttAsyncClient;
import com.plccom.mqtt.client.PlccomMqttClient;
import com.plccom.mqtt.client.PlccomMqttClientFactory;

/**
 * Workshop 32 - Asynchronous Client API (Resilience)
 *
 * WHAT THIS WORKSHOP SHOWS
 *   The asynchronous face of the client. Every operation (connect, subscribe,
 *   publish, receive) returns a {@link java.util.concurrent.CompletableFuture}
 *   instead of blocking the calling thread, so MQTT work can be composed with a
 *   UI event loop, a service scheduler or other background tasks. The MQTT
 *   protocol is exactly the same as in the blocking workshops; only the Java
 *   programming style changes.
 *
 * WHEN YOU NEED THIS IN REAL APPLICATIONS
 *   Desktop or web UIs that must stay responsive, gateways that multiplex many
 *   connections, and services that want to chain MQTT steps with other
 *   asynchronous work without dedicating a blocking thread to each operation.
 *
 * BROKER FOR THIS WORKSHOP
 *   This client workshop needs a running MQTT broker. Start Broker Workshop 11
 *   (First Broker) first, or point BROKER_ENDPOINT below at any MQTT broker of
 *   your choice (for example a local Mosquitto). No broker is embedded here, so
 *   a client-only PLCcom license is enough. Override with
 *   -Dplccom.mqtt.workshop.endpoint=mqtt://host:port .
 *
 * MQTT TERMS USED HERE
 *   - Asynchronous API : the same publish/subscribe operations, returning
 *                        futures the caller completes or composes instead of
 *                        blocking.
 *   - Executor         : the thread pool the async client uses to complete the
 *                        futures. The application owns and shuts it down.
 *
 * EXPECTED RESULT
 *   Connect, subscribe, publish and receive all complete through futures, and the
 *   published message is received asynchronously.
 */
public final class _32_AsyncApi {

    // === YOUR PLCcom LICENSE =================================================
    // Important !!!!!!!!!!!!!!!!!!
    // Enter your Username + Serial here! Please note: passing two empty strings runs the library
    // for 15 minutes during a debug session. Both values can also come
    // from configuration or an environment variable.
    // A client-only license is sufficient for the client workshops.
    // Free trial license (14 days, uninterrupted): https://www.indi-an.com/en/plccom/mqtt/mqtt-download/
    private static final String LICENSE_USER   = "";
    private static final String LICENSE_SERIAL = "";
    // =========================================================================

    // === BROKER TO CONNECT TO ================================================
    // Start Broker Workshop 11, or change this to any MQTT broker you like.
    // The scheme must be mqtt:// / mqtts:// / ws:// / wss:// - not tcp:// .
    private static final String BROKER_ENDPOINT = "mqtt://localhost:1883";
    // =========================================================================

    private static final String TOPIC = "plccom/workshop/32/async";

    private _32_AsyncApi() {
    }

    /**
     * Opens the console and runs the workshop.
     *
     * @param args command-line arguments; not used by this workshop
     */
    public static void main(String[] args) {
        PLCcomConsole.open("PLCcom MQTT Workshop 32 - Asynchronous Client API", 1000);
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
     * Runs one publish/subscribe flow using the asynchronous API.
     *
     * @throws Exception if any asynchronous operation fails or times out
     */
    public static void start() throws Exception {
        System.out.println("╔════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║  What this workshop demonstrates                                           ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  The asynchronous client API: the same MQTT operations, but returning      ║");
        System.out.println("║  CompletableFuture values instead of blocking the calling thread.          ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  Use it in UIs, services and gateways that must keep doing other work      ║");
        System.out.println("║  while connect, subscribe, publish and receive are in progress.            ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  The MQTT protocol is identical - only the Java programming style differs. ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  Expected result: connect, subscribe, publish and receive all complete     ║");
        System.out.println("║  through futures, and the published message is received.                   ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════════════╝");
        System.out.println();

        System.out.println("  Broker endpoint : " + brokerEndpoint());
        System.out.println();

        // The application owns the executor that completes the futures. A single
        // thread is enough for this workshop; a real app would size it to its load
        // and shut it down when finished (see the finally block).
        ExecutorService executor = Executors.newSingleThreadExecutor();
        PlccomMqttClient client = PlccomMqttClientFactory.builder(LICENSE_USER, LICENSE_SERIAL)
                .identifier("plccom-workshop-32")
                .endpoint(brokerEndpoint())
                .mqtt5()
                .keepAliveSeconds(30)
                .build();

        // async(...) returns a non-blocking facade over the same client.
        PlccomMqttAsyncClient async = client.async(executor);
        try {
            // -- Step 1: connect asynchronously -------------------------------
            // Each call returns a future. We use get(...) here to keep the
            // workshop linear, but an application could attach thenApply/thenRun
            // callbacks instead of waiting.
            System.out.println("  connect() future ...");
            async.connect().get(3, TimeUnit.SECONDS);
            System.out.println("    connected");

            // -- Step 2: subscribe and publish asynchronously -----------------
            System.out.println("  subscribe() and publish() futures ...");
            async.subscribe(TOPIC).qos1().send().get(3, TimeUnit.SECONDS);
            async.publish(TOPIC, "future-based message")
                    .qos1()
                    .payloadFormatUtf8()
                    .send()
                    .get(3, TimeUnit.SECONDS);
            System.out.println("    subscribed and published");

            // -- Step 3: receive asynchronously -------------------------------
            System.out.println("  receive() future ...");
            MqttMessage message = async.receive(3000).get(4, TimeUnit.SECONDS);
            if (message == null) {
                throw new IllegalStateException("The async message was not received in time.");
            }
            System.out.println("    received: '" + message.payloadAsString() + "'");

            System.out.println();
            System.out.println("  Success: the full MQTT flow completed through futures.");
        } finally {
            // Close the client first, then the executor the application owns.
            client.close();
            executor.shutdownNow();
        }
    }

    private static String brokerEndpoint() {
        return System.getProperty("plccom.mqtt.workshop.endpoint", BROKER_ENDPOINT);
    }
}
