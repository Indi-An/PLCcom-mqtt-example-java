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

package com.plccom.mqtt.examples.client.mqtt5;

import com.plccom.console.PLCcomConsole;
import com.plccom.mqtt.client.MqttMessage;
import com.plccom.mqtt.client.MqttPublishResult;
import com.plccom.mqtt.client.MqttSubscribeResult;
import com.plccom.mqtt.client.PlccomMqttClient;
import com.plccom.mqtt.client.PlccomMqttClientFactory;
import com.plccom.mqtt.client.PlccomMqttConnectResult;
import com.plccom.mqtt.client.PlccomMqttPublishMessage;

/**
 * Workshop 22 - MQTT 5 Subscription Options (MQTT 5)
 *
 * WHAT THIS WORKSHOP SHOWS
 *   The MQTT 5 options that are chosen at SUBSCRIBE time and change what a
 *   subscriber receives: Subscription Identifier, No Local, Retain As Published
 *   and (explained in comments) Retain Handling. These options let applications
 *   build clean event flows without inventing custom fields inside the payload.
 *
 * WHEN YOU NEED THIS IN REAL APPLICATIONS
 *   No Local avoids feedback loops when one client both publishes and subscribes
 *   on related topics. Subscription Identifiers help a client with several
 *   overlapping subscriptions know which one caused a message. Retain As
 *   Published lets a receiver tell remembered state from live updates.
 *
 * BROKER FOR THIS WORKSHOP
 *   This client workshop needs a running MQTT 5 broker. Start Broker Workshop 11
 *   (First Broker) first, or point BROKER_ENDPOINT below at any MQTT 5 broker of
 *   your choice (for example a local Mosquitto). No broker is embedded here, so
 *   a client-only PLCcom license is enough. Override with
 *   -Dplccom.mqtt.workshop.endpoint=mqtt://host:port .
 *
 * MQTT TERMS USED HERE
 *   - Subscription Identifier : an integer the subscriber assigns to a
 *                               subscription. The broker echoes it on every
 *                               message delivered because of that subscription.
 *   - No Local                : the broker does not deliver a client its own
 *                               publications through that subscription.
 *   - Retain As Published     : the broker keeps the retain flag exactly as the
 *                               publisher set it, instead of clearing it on live
 *                               delivery.
 *   - Retain Handling         : controls whether retained messages are replayed
 *                               when the subscription is created
 *                               (retainHandlingSendAtSubscribe / ...IfNew /
 *                               retainHandlingDoNotSend).
 *
 * EXPECTED RESULT
 *   The console shows the Subscription Identifier echoed back, a self-message
 *   suppressed by No Local while another client's message still arrives, and the
 *   retain flag preserved only for the Retain-As-Published subscriber.
 */
public final class _22_Mqtt5SubscriptionOptions {

    // === YOUR PLCcom LICENSE =================================================
    // Replace both values with the user name and serial from your PLCcom license
    // e-mail. A client-only license is sufficient for the client workshops.
    private static final String LICENSE_USER   = "<Enter your UserName here>";
    private static final String LICENSE_SERIAL = "<Enter your Serial here>";
    // =========================================================================

    // === BROKER TO CONNECT TO ================================================
    // Start Broker Workshop 11, or change this to any MQTT 5 broker you like.
    // The scheme must be mqtt:// / mqtts:// / ws:// / wss:// - not tcp:// .
    private static final String BROKER_ENDPOINT = "mqtt://localhost:1883";
    // =========================================================================

    private _22_Mqtt5SubscriptionOptions() {
    }

    /**
     * Opens the console and runs the workshop.
     *
     * @param args command-line arguments; not used by this workshop
     */
    public static void main(String[] args) {
        PLCcomConsole.open("PLCcom MQTT Workshop 22 - MQTT 5 Subscription Options", 1000);
        try {
            start();
        } catch (Exception ex) {
            System.out.println();
            System.out.println("  This workshop needs a running MQTT 5 broker at " + brokerEndpoint() + ".");
            System.out.println("  Start Broker Workshop 11 (or a local Mosquitto) and run again.");
            System.out.println("  Technical detail: " + ex.getMessage());
        } finally {
            PLCcomConsole.waitForEnter();
            PLCcomConsole.close();
        }
    }

    /**
     * Executes the MQTT 5 subscription-option flow.
     *
     * @throws Exception if any option does not behave as expected
     */
    public static void start() throws Exception {
        System.out.println("╔════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║  What this workshop demonstrates                                           ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  MQTT 5 subscription options - flags chosen when you SUBSCRIBE.            ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  Subscription Identifier: a number the broker echoes on every message      ║");
        System.out.println("║                           that matched this subscription.                  ║");
        System.out.println("║  No Local               : do not deliver a client its own publications.    ║");
        System.out.println("║  Retain As Published    : keep the retain flag exactly as the publisher    ║");
        System.out.println("║                           set it, instead of clearing it.                  ║");
        System.out.println("║  Retain Handling        : control whether retained messages are replayed   ║");
        System.out.println("║                           at subscribe time (explained in comments).       ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  Expected result: each option changes what the subscriber receives, and    ║");
        System.out.println("║  the console shows the difference.                                         ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════════════╝");
        System.out.println();

        System.out.println("  Broker endpoint : " + brokerEndpoint());
        System.out.println();

        demonstrateSubscriptionIdentifier();
        System.out.println();
        demonstrateNoLocal();
        System.out.println();
        demonstrateRetainAsPublished();
    }

    // ---------------------------------------------------------------------------
    // Part A: Subscription Identifier
    // ---------------------------------------------------------------------------
    private static void demonstrateSubscriptionIdentifier() throws Exception {
        System.out.println("  === Part A: Subscription Identifier ===");
        final String topic = "plccom/workshop/22/subid";
        PlccomMqttClient client = connectedClient("plccom-workshop-22-subid");
        try {
            // The identifier 77 is our own label for this subscription. The broker
            // will attach it to every message delivered because of this subscribe.
            requireGood("SUBSCRIBE", client.subscribe(topic).qos1().subscriptionIdentifier(77).send());
            requireGood("PUBLISH", client.publish(topic, "hello").qos1().send());

            PlccomMqttPublishMessage received = client.receive(3000);
            if (received == null) {
                throw new IllegalStateException("No message received for the Subscription Identifier demo.");
            }
            System.out.println("    delivered subscription identifiers: "
                    + new MqttMessage(received).subscriptionIdentifiers() + " (expected [77])");
        } finally {
            client.close();
        }
    }

    // ---------------------------------------------------------------------------
    // Part B: No Local
    // ---------------------------------------------------------------------------
    private static void demonstrateNoLocal() throws Exception {
        System.out.println("  === Part B: No Local ===");
        final String topic = "plccom/workshop/22/nolocal";
        PlccomMqttClient self = connectedClient("plccom-workshop-22-self");
        PlccomMqttClient other = connectedClient("plccom-workshop-22-other");
        try {
            // With No Local, the broker will not send this client its OWN messages
            // on this subscription - a simple way to avoid echo/feedback loops.
            requireGood("SUBSCRIBE", self.subscribe(topic).qos1().noLocal().send());

            // The client publishes to the same topic it subscribed to. Because of
            // No Local, it must NOT receive this message back.
            requireGood("PUBLISH self", self.publish(topic, "my own message").qos1().send());
            PlccomMqttPublishMessage ownEcho = self.receive(600);
            if (ownEcho != null) {
                throw new IllegalStateException("No Local should have suppressed the client's own message.");
            }
            System.out.println("    own message: suppressed by No Local (as expected)");

            // A different client publishes the same topic. No Local only filters
            // the SAME client, so this message must still arrive.
            requireGood("PUBLISH other", other.publish(topic, "message from another client").qos1().send());
            PlccomMqttPublishMessage external = self.receive(3000);
            if (external == null) {
                throw new IllegalStateException("No Local must not suppress another client's message.");
            }
            System.out.println("    other client's message: received '"
                    + new MqttMessage(external).payloadAsString() + "'");
        } finally {
            other.close();
            self.close();
        }
    }

    // ---------------------------------------------------------------------------
    // Part C: Retain As Published
    // ---------------------------------------------------------------------------
    private static void demonstrateRetainAsPublished() throws Exception {
        System.out.println("  === Part C: Retain As Published ===");
        final String topic = "plccom/workshop/22/retain-as-published";
        PlccomMqttClient normalSub = connectedClient("plccom-workshop-22-normal");
        PlccomMqttClient rapSub = connectedClient("plccom-workshop-22-rap");
        PlccomMqttClient publisher = connectedClient("plccom-workshop-22-publisher");
        try {
            // Normally, when a live (already subscribed) client receives a message,
            // the broker clears the retain flag. Retain As Published keeps it set,
            // so this subscriber can still tell the message was published retained.
            // (Retain Handling, by contrast, would control whether OLD retained
            //  messages are replayed at subscribe time - not shown separately here.)
            requireGood("SUBSCRIBE normal", normalSub.subscribe(topic).qos1().send());
            requireGood("SUBSCRIBE rap", rapSub.subscribe(topic).qos1().retainAsPublished().send());

            requireGood("PUBLISH retained live",
                    publisher.publish(topic, "live retained value").qos1().retain().send());

            PlccomMqttPublishMessage normal = normalSub.receive(3000);
            PlccomMqttPublishMessage rap = rapSub.receive(3000);
            if (normal == null || rap == null) {
                throw new IllegalStateException("Both subscribers should receive the live retained publish.");
            }
            System.out.println("    normal subscriber retain flag           : " + normal.isRetain()
                    + " (expected false)");
            System.out.println("    retain-as-published subscriber retain fl: " + rap.isRetain()
                    + " (expected true)");

            System.out.println();
            System.out.println("  Success: subscription options changed delivery exactly as MQTT 5 defines.");
        } finally {
            publisher.close();
            rapSub.close();
            normalSub.close();
        }
    }

    // ---------------------------------------------------------------------------
    // Small private helpers kept inside this workshop class (only remove
    // repetition; the MQTT actions themselves stay visible above).
    // ---------------------------------------------------------------------------

    private static PlccomMqttClient connectedClient(String clientId) throws Exception {
        PlccomMqttClient client = PlccomMqttClientFactory.builder(LICENSE_USER, LICENSE_SERIAL)
                .identifier(clientId)
                .endpoint(brokerEndpoint())
                .mqtt5()
                .keepAliveSeconds(30)
                .build();
        PlccomMqttConnectResult result = client.connect();
        if (!result.isGood()) {
            throw new IllegalStateException("CONNECT failed: "
                    + result.getQuality() + " - " + result.getMessage());
        }
        return client;
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

    private static String brokerEndpoint() {
        return System.getProperty("plccom.mqtt.workshop.endpoint", BROKER_ENDPOINT);
    }
}
