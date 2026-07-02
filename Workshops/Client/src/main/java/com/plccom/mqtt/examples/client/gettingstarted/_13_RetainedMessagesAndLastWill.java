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

package com.plccom.mqtt.examples.client.gettingstarted;

import com.plccom.console.PLCcomConsole;
import com.plccom.mqtt.client.MqttMessage;
import com.plccom.mqtt.client.MqttPublishResult;
import com.plccom.mqtt.client.MqttSubscribeResult;
import com.plccom.mqtt.client.PlccomMqttClient;
import com.plccom.mqtt.client.PlccomMqttClientFactory;
import com.plccom.mqtt.client.PlccomMqttConnectResult;
import com.plccom.mqtt.client.PlccomMqttPublishMessage;

/**
 * Workshop 13 - Retained Messages and Last Will (Getting Started)
 *
 * WHAT THIS WORKSHOP SHOWS
 *   The two mechanisms MQTT offers to communicate state instead of just events:
 *   a Retained Message (the broker keeps the last value of a topic) and a Last
 *   Will (the broker announces that a client dropped off unexpectedly). The
 *   workshop also shows how to DELETE a retained message and how a clean
 *   disconnect suppresses the Will.
 *
 * WHEN YOU NEED THIS IN REAL APPLICATIONS
 *   Retained messages are ideal for "current status" topics (a machine state, a
 *   configuration value) so a newly started dashboard sees the latest value
 *   immediately instead of waiting for the next update. A Last Will gives other
 *   participants a reliable "device X went offline" signal without polling.
 *
 * BROKER FOR THIS WORKSHOP
 *   This client workshop needs a running MQTT broker. Start Broker Workshop 11
 *   (First Broker) first, or point BROKER_ENDPOINT below at any MQTT broker of
 *   your choice (for example a local Mosquitto). No broker is embedded here, so
 *   a client-only PLCcom license is enough to run it. Override without editing
 *   the file with -Dplccom.mqtt.workshop.endpoint=mqtt://host:port .
 *
 * MQTT TERMS USED HERE
 *   - Retained Message : the broker stores the last message published to a topic
 *                        with the retain flag set. Every new subscriber to that
 *                        topic receives it immediately as the current value.
 *   - Retained deletion: publishing a ZERO-LENGTH payload with the retain flag
 *                        removes the stored value. This is the MQTT-defined way
 *                        to clear retained state.
 *   - Last Will        : a message the client hands to the broker during CONNECT.
 *                        The broker publishes it only if the connection is lost
 *                        WITHOUT a clean MQTT DISCONNECT (for example a crash or
 *                        a cable pull). A clean disconnect() suppresses it, so
 *                        receivers can tell a planned shutdown from a failure.
 *
 * EXPECTED RESULT
 *   A late subscriber receives the retained status value; after the retained
 *   value is deleted a further subscriber receives nothing; and an observer
 *   receives the Last Will after the watched client drops without disconnecting.
 */
public final class _13_RetainedMessagesAndLastWill {

    // === YOUR PLCcom LICENSE =================================================
    // Replace both values with the user name and serial from your PLCcom license
    // e-mail. A client-only license is sufficient for the client workshops.
    private static final String LICENSE_USER   = "<Enter your UserName here>";
    private static final String LICENSE_SERIAL = "<Enter your Serial here>";
    // =========================================================================

    // === BROKER TO CONNECT TO ================================================
    // Start Broker Workshop 11, or change this to any MQTT broker you like
    // (for example mqtt://localhost:1883 for a local Mosquitto). The scheme must
    // be mqtt:// (plain TCP) or mqtts:// / ws:// / wss:// - not tcp:// .
    private static final String BROKER_ENDPOINT = "mqtt://localhost:1883";
    // =========================================================================

    private static final String STATUS_TOPIC = "plccom/workshop/13/status";
    private static final String WILL_TOPIC   = "plccom/workshop/13/lastwill";

    private _13_RetainedMessagesAndLastWill() {
    }

    /**
     * Opens the console and runs the workshop.
     *
     * @param args command-line arguments; not used by this workshop
     */
    public static void main(String[] args) {
        PLCcomConsole.open("PLCcom MQTT Workshop 13 - Retained Messages and Last Will", 1000);
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
     * Executes the actual MQTT example flow.
     *
     * <p>This method deliberately throws on failure so the automated workshop
     * smoke check can detect a broken example. The friendly, customer-facing
     * error handling lives in {@link #main(String[])}.</p>
     *
     * @throws Exception if any part of the retained-message or Last-Will flow
     *         does not behave as expected
     */
    public static void start() throws Exception {

        // The explanation is the very first thing in the flow, written out in
        // full so anyone reading this source understands the workshop at a glance.
        System.out.println("╔════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║  What this workshop demonstrates                                           ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  Retained messages and the Last Will - two ways MQTT remembers state.      ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  Retained: the broker keeps the LAST message on a topic and hands it to    ║");
        System.out.println("║  every new subscriber at once. Publish an empty payload to clear it.       ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  Last Will: a message the broker sends automatically if a client drops     ║");
        System.out.println("║  WITHOUT a clean disconnect. A clean disconnect suppresses the Will.       ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  Expected result: a late subscriber gets the retained value; after         ║");
        System.out.println("║  deletion it gets nothing; the Will fires on an ungraceful close.          ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════════════╝");
        System.out.println();

        System.out.println("  Broker endpoint : " + brokerEndpoint());
        System.out.println();

        // The two parts are independent, so we run them one after another with
        // their own clients to keep each demonstration easy to follow.
        demonstrateRetainedMessages();
        System.out.println();
        demonstrateLastWill();
    }

    // ---------------------------------------------------------------------------
    // Part A: Retained messages (set, read by a late subscriber, then delete)
    // ---------------------------------------------------------------------------
    private static void demonstrateRetainedMessages() throws Exception {
        System.out.println("  === Part A: Retained messages ===");

        // The publisher stores a retained "online" status. Because it is retained,
        // the broker keeps it even after the publisher disconnects.
        PlccomMqttClient publisher = connectedClient("plccom-workshop-13-status-publisher");
        try {
            // -- Step 1: publish a retained status value ----------------------
            // retain() tells the broker to remember this exact message as the
            // current value of STATUS_TOPIC and replay it to future subscribers.
            System.out.println("  Publishing retained status 'online' to " + STATUS_TOPIC);
            requireGood("retained PUBLISH", publisher.publish(STATUS_TOPIC, "online")
                    .qos1()
                    .retain()
                    .send());

            // -- Step 2: a LATE subscriber still receives the retained value ---
            // This subscriber connects only now, after the message was published.
            // Thanks to the retained flag it still receives the current value
            // immediately, and the received message carries the retain flag.
            System.out.println("  A late subscriber connects and subscribes ...");
            PlccomMqttClient lateSubscriber = connectedClient("plccom-workshop-13-late-subscriber");
            try {
                requireGood("SUBSCRIBE", lateSubscriber.subscribe(STATUS_TOPIC).qos1().send());
                PlccomMqttPublishMessage retained = lateSubscriber.receive(3000);
                if (retained == null) {
                    throw new IllegalStateException("The retained status was not delivered to the late subscriber.");
                }
                MqttMessage message = new MqttMessage(retained);
                System.out.println("    received: '" + message.payloadAsString()
                        + "'  retain flag=" + retained.isRetain());
            } finally {
                lateSubscriber.close();
            }

            // -- Step 3: delete the retained value ----------------------------
            // Publishing a ZERO-LENGTH payload with the retain flag is the MQTT
            // way to clear the stored value. After this, new subscribers get
            // nothing until a fresh value is published.
            System.out.println("  Deleting the retained value (empty retained publish) ...");
            requireGood("retained deletion PUBLISH", publisher.publish(STATUS_TOPIC, new byte[0])
                    .qos1()
                    .retain()
                    .send());

            // -- Step 4: a further subscriber now receives nothing ------------
            System.out.println("  Another subscriber connects and should receive no retained value ...");
            PlccomMqttClient afterDeletion = connectedClient("plccom-workshop-13-after-deletion");
            try {
                requireGood("SUBSCRIBE", afterDeletion.subscribe(STATUS_TOPIC).qos1().send());
                PlccomMqttPublishMessage shouldBeNull = afterDeletion.receive(1500);
                if (shouldBeNull != null) {
                    throw new IllegalStateException("A deleted retained value must not be replayed.");
                }
                System.out.println("    received: nothing - retained state was cleared as expected.");
            } finally {
                afterDeletion.close();
            }
        } finally {
            publisher.close();
        }
    }

    // ---------------------------------------------------------------------------
    // Part B: Last Will (fires on ungraceful drop, suppressed on clean disconnect)
    // ---------------------------------------------------------------------------
    private static void demonstrateLastWill() throws Exception {
        System.out.println("  === Part B: Last Will ===");

        // The observer represents a supervision system that wants to know when a
        // device disappears unexpectedly.
        PlccomMqttClient observer = connectedClient("plccom-workshop-13-will-observer");
        try {
            requireGood("SUBSCRIBE", observer.subscribe(WILL_TOPIC).qos1().send());

            // -- Step 1: a client connects WITH a Last Will -------------------
            // The Will is handed to the broker during CONNECT. We keep it simple
            // here (topic, payload, QoS 1) so it works the same on MQTT 3.1.1.
            System.out.println("  A watched client connects with a Last Will on " + WILL_TOPIC);
            PlccomMqttClient watched = PlccomMqttClientFactory.builder(LICENSE_USER, LICENSE_SERIAL)
                    .identifier("plccom-workshop-13-watched-device")
                    .endpoint(brokerEndpoint())
                    .mqtt311()
                    .keepAliveSeconds(30)
                    .will(WILL_TOPIC, "device lost connection unexpectedly")
                    .qos1()
                    .done()
                    .build();
            requireGoodConnect(watched.connect());

            // -- Step 2: drop the client WITHOUT a clean disconnect -----------
            // close() shuts the socket without sending an MQTT DISCONNECT, which
            // looks to the broker exactly like a crash or a lost network. That is
            // the trigger for the broker to publish the Last Will.
            System.out.println("  Dropping the watched client ungracefully (socket close, no DISCONNECT) ...");
            watched.close();

            PlccomMqttPublishMessage will = observer.receive(3000);
            if (will == null) {
                throw new IllegalStateException("The broker did not publish the Last Will after an ungraceful drop.");
            }
            System.out.println("    observer received the Will: '"
                    + new MqttMessage(will).payloadAsString() + "'");

            // -- Step 3: a clean disconnect must NOT trigger a Will -----------
            // The same setup, but this time we call disconnect(), which sends a
            // proper MQTT DISCONNECT. The broker then discards the Will, so a
            // planned shutdown does not raise a false "device lost" alarm.
            System.out.println("  Now a client disconnects cleanly; no Will must be published ...");
            PlccomMqttClient graceful = PlccomMqttClientFactory.builder(LICENSE_USER, LICENSE_SERIAL)
                    .identifier("plccom-workshop-13-graceful-device")
                    .endpoint(brokerEndpoint())
                    .mqtt311()
                    .keepAliveSeconds(30)
                    .will(WILL_TOPIC, "this Will should never be seen")
                    .qos1()
                    .done()
                    .build();
            requireGoodConnect(graceful.connect());
            graceful.disconnect();

            PlccomMqttPublishMessage noWill = observer.receive(1500);
            if (noWill != null) {
                throw new IllegalStateException("A clean disconnect must suppress the Last Will.");
            }
            System.out.println("    no Will received - a clean disconnect suppressed it as expected.");

            System.out.println();
            System.out.println("  Success: retained state and Last Will both behaved as MQTT defines.");
        } finally {
            observer.close();
        }
    }

    // ---------------------------------------------------------------------------
    // Small private helpers, kept inside this same workshop class. They only
    // remove repetition (building/connecting a client, checking a result). The
    // MQTT actions themselves stay visible in the steps above.
    // ---------------------------------------------------------------------------

    private static PlccomMqttClient connectedClient(String clientId) throws Exception {
        PlccomMqttClient client = PlccomMqttClientFactory.builder(LICENSE_USER, LICENSE_SERIAL)
                .identifier(clientId)
                .endpoint(brokerEndpoint())
                .mqtt311()
                .keepAliveSeconds(30)
                .build();
        requireGoodConnect(client.connect());
        return client;
    }

    private static void requireGoodConnect(PlccomMqttConnectResult result) {
        if (!result.isGood()) {
            throw new IllegalStateException("CONNECT failed: "
                    + result.getQuality() + " - " + result.getMessage());
        }
    }

    private static void requireGood(String operation, MqttSubscribeResult result) {
        if (!result.isGood()) {
            throw new IllegalStateException(operation + " failed: "
                    + result.getQuality() + " - " + result.getMessage());
        }
    }

    private static void requireGood(String operation, MqttPublishResult result) {
        if (!result.isGood()) {
            throw new IllegalStateException(operation + " failed: "
                    + result.getQuality() + " - " + result.getMessage());
        }
    }

    // Resolves the broker endpoint: the visible constant, or an optional
    // system-property override used by the internal automation.
    private static String brokerEndpoint() {
        return System.getProperty("plccom.mqtt.workshop.endpoint", BROKER_ENDPOINT);
    }
}
