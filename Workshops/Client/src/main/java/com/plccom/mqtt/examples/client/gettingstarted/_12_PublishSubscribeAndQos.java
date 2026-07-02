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
 * Workshop 12 - Publish, Subscribe and Quality of Service (Getting Started)
 *
 * WHAT THIS WORKSHOP SHOWS
 *   How to choose the right delivery guarantee (QoS 0, 1 or 2) for a publish,
 *   and how a single subscription with a wildcard Topic Filter can receive
 *   messages from many different Topic Names at once.
 *
 * WHEN YOU NEED THIS IN REAL APPLICATIONS
 *   Almost always. Telemetry that may be re-sent often uses QoS 0, normal
 *   application data uses QoS 1, and commands that must not be duplicated use
 *   QoS 2. Wildcards let one consumer subscribe to a whole branch of topics
 *   (for example every sensor of a machine) with a single subscription.
 *
 * BROKER FOR THIS WORKSHOP
 *   This client workshop needs a running MQTT broker. Start Broker Workshop 11
 *   (First Broker) first, or point BROKER_ENDPOINT below at any MQTT broker of
 *   your choice (for example a local Mosquitto). No broker is embedded here, so
 *   a client-only PLCcom license is enough to run it. Override without editing
 *   the file with -Dplccom.mqtt.workshop.endpoint=mqtt://host:port .
 *
 * MQTT TERMS USED HERE
 *   - QoS 0 (at most once)  : "fire and forget". Fast and cheap, but the
 *                             message can be lost and is never re-sent.
 *   - QoS 1 (at least once) : the broker acknowledges the message, so it is not
 *                             lost, but a duplicate is possible after a retry.
 *   - QoS 2 (exactly once)  : a four-part handshake guarantees the message is
 *                             delivered once and only once. Highest overhead.
 *   - Topic wildcards       : in a Topic Filter, '+' matches exactly one level
 *                             and '#' matches the rest of the topic tree. A
 *                             Topic Name used for publishing never has wildcards.
 *   - Delivered QoS         : the QoS a subscriber actually receives is the
 *                             minimum of the publish QoS and the subscription
 *                             QoS. Here we subscribe with QoS 2 so each message
 *                             is delivered at exactly the QoS it was published.
 *
 * EXPECTED RESULT
 *   Three messages published to different topics with QoS 0, 1 and 2 all arrive
 *   through one "#" subscription, each printed with its own delivered QoS.
 */
public final class _12_PublishSubscribeAndQos {

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

    private _12_PublishSubscribeAndQos() {
    }

    /**
     * Opens the console and runs the workshop.
     *
     * @param args command-line arguments; not used by this workshop
     */
    public static void main(String[] args) {
        PLCcomConsole.open("PLCcom MQTT Workshop 12 - Publish, Subscribe and QoS", 1000);
        try {
            start();
        } catch (Exception ex) {
            // The most common reason a customer sees a failure here is simply
            // that no broker is running yet at BROKER_ENDPOINT. Give a friendly
            // hint instead of only a stack trace.
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
     * @throws Exception if the workshop cannot connect, subscribe, publish or
     *         receive its demonstration messages
     */
    public static void start() throws Exception {

        // The explanation is the very first thing in the flow, written out in
        // full so anyone reading this source understands the workshop at a glance.
        System.out.println("╔════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║  What this workshop demonstrates                                           ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  How to choose a Quality of Service (QoS) level and how topic              ║");
        System.out.println("║  wildcards let one subscription receive many topics.                       ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  QoS 0 = at most once  (fire and forget, may be lost).                     ║");
        System.out.println("║  QoS 1 = at least once (guaranteed, but duplicates possible).              ║");
        System.out.println("║  QoS 2 = exactly once  (guaranteed, no duplicates, most overhead).         ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  A Topic Filter may use wildcards: + (one level), # (all levels).          ║");
        System.out.println("║  Delivered QoS = min(publish QoS, subscription QoS).                       ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  Expected result: three messages on different topics arrive through        ║");
        System.out.println("║  one '#' subscription, each shown with its own QoS level.                  ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════════════╝");
        System.out.println();

        // One wildcard Topic Filter that covers the whole workshop 12 topic tree.
        // '#' at the end matches every remaining topic level, so this single
        // subscription receives all three publications below regardless of their
        // exact Topic Name.
        final String topicFilter = "plccom/workshop/12/#";

        System.out.println("  Broker endpoint : " + brokerEndpoint());
        System.out.println();

        // -- Step 1: build and connect the client -----------------------------
        // We use MQTT 3.1.1 here; it supports all three QoS levels just like
        // MQTT 5. The license and endpoint are visible at the top of this file.
        PlccomMqttClient client = PlccomMqttClientFactory.builder(LICENSE_USER, LICENSE_SERIAL)
                .identifier("plccom-workshop-12")
                .endpoint(brokerEndpoint())
                .mqtt311()
                .keepAliveSeconds(30)
                .build();

        try {
            System.out.println("  Connecting ...");
            PlccomMqttConnectResult connectResult = client.connect();
            if (!connectResult.isGood()) {
                throw new IllegalStateException("CONNECT failed: "
                        + connectResult.getQuality() + " - " + connectResult.getMessage());
            }
            System.out.println("  Connected. Negotiated protocol: "
                    + connectResult.getNegotiatedProtocolVersion());

            // -- Step 2: subscribe to the wildcard filter with QoS 2 ----------
            // We grant QoS 2 for the subscription so the broker may deliver each
            // message at the QoS it was published with. If we subscribed with
            // QoS 0 instead, even a QoS 2 publish would be delivered as QoS 0,
            // because the delivered QoS is the minimum of the two.
            System.out.println("  Subscribing to Topic Filter: " + topicFilter + " (max QoS 2)");
            MqttSubscribeResult subscribeResult = client.subscribe(topicFilter)
                    .qos2()
                    .send();
            if (!subscribeResult.isGood()) {
                throw new IllegalStateException("SUBSCRIBE failed: "
                        + subscribeResult.getQuality() + " - " + subscribeResult.getMessage());
            }

            // -- Step 3: publish three messages at QoS 0, 1 and 2 -------------
            // Each message goes to a different Topic Name, but all of them match
            // the single wildcard filter above. Note how the QoS is a property of
            // the publish, chosen per message to fit its reliability needs.
            System.out.println();
            System.out.println("  Publishing three messages with different QoS levels:");
            publishAtQos(client, "plccom/workshop/12/sensor/temperature", "21.4 degC", 0);
            publishAtQos(client, "plccom/workshop/12/sensor/humidity", "48 %", 1);
            publishAtQos(client, "plccom/workshop/12/command/restart", "restart-now", 2);

            // -- Step 4: receive all three through the one subscription --------
            // Because all three Topic Names match "plccom/workshop/12/#", the same
            // subscription delivers every message. The order is not guaranteed, so
            // we simply collect up to three and print each with its delivered QoS.
            System.out.println();
            System.out.println("  Receiving through the single wildcard subscription:");
            int receivedCount = 0;
            for (int i = 0; i < 3; i++) {
                PlccomMqttPublishMessage received = client.receive(3000);
                if (received == null) {
                    break;
                }
                MqttMessage message = new MqttMessage(received);
                System.out.println("    topic=" + message.topic()
                        + "  QoS=" + message.qos()
                        + "  payload=" + message.payloadAsString());
                receivedCount++;
            }

            System.out.println();
            if (receivedCount == 3) {
                System.out.println("  Success: all three messages arrived through one '#' subscription.");
            } else {
                // A QoS 0 message may legitimately be dropped by a broker under
                // load; anything else usually points to a configuration problem.
                System.out.println("  Note: received " + receivedCount
                        + " of 3 messages. QoS 0 may be dropped; QoS 1/2 should always arrive.");
            }

        } finally {
            // -- Step 5: clean shutdown ---------------------------------------
            client.close();
        }
    }

    // Small private helper, kept inside this same workshop class on purpose: it
    // only removes the repetition of the three publish calls and prints what it
    // sends. The actual MQTT action (publish at a chosen QoS) stays fully visible.
    private static void publishAtQos(PlccomMqttClient client, String topicName, String payload, int qos)
            throws Exception {
        MqttPublishResult result;
        switch (qos) {
            case 0:
                result = client.publish(topicName, payload).qos0().send();
                break;
            case 1:
                result = client.publish(topicName, payload).qos1().send();
                break;
            case 2:
                result = client.publish(topicName, payload).qos2().send();
                break;
            default:
                throw new IllegalArgumentException("QoS must be 0, 1 or 2 but was " + qos);
        }
        if (!result.isGood()) {
            throw new IllegalStateException("PUBLISH (QoS " + qos + ") failed: "
                    + result.getQuality() + " - " + result.getMessage());
        }
        System.out.println("    published QoS " + qos + " to Topic Name " + topicName);
    }

    // Resolves the broker endpoint: the visible constant, or an optional
    // system-property override used by the internal automation.
    private static String brokerEndpoint() {
        return System.getProperty("plccom.mqtt.workshop.endpoint", BROKER_ENDPOINT);
    }
}
