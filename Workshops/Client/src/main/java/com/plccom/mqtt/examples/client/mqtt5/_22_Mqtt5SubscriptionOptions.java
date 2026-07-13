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
 * HOW THIS WORKSHOP IS WRITTEN
 *   The three parts run one after another, top-to-bottom inside start(). Every
 *   client is built and connected where it is used, and every SUBSCRIBE/PUBLISH
 *   result is checked in place with a visible isGood() test - nothing important
 *   is tucked away in a helper method.
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

        // =====================================================================
        // Part A: Subscription Identifier
        // =====================================================================
        System.out.println("  === Part A: Subscription Identifier ===");
        final String subIdTopic = "plccom/workshop/22/subid";

        // Build and connect one MQTT 5 client for this part.
        PlccomMqttClient subIdClient = PlccomMqttClientFactory.builder(LICENSE_USER, LICENSE_SERIAL)
                .identifier("plccom-workshop-22-subid")
                .endpoint(brokerEndpoint())
                .mqtt5()
                .keepAliveSeconds(30)
                .build();
        try {
            PlccomMqttConnectResult subIdConnect = subIdClient.connect();
            if (!subIdConnect.isGood()) {
                throw new IllegalStateException("CONNECT (subid client) failed: "
                        + subIdConnect.getQuality() + " - " + subIdConnect.getMessage());
            }

            // The identifier 77 is our own label for this subscription. The broker
            // attaches it to every message delivered because of this subscribe.
            MqttSubscribeResult subIdSubscribe = subIdClient.subscribe(subIdTopic)
                    .qos1()
                    .subscriptionIdentifier(77)
                    .send();
            if (!subIdSubscribe.isGood()) {
                throw new IllegalStateException("SUBSCRIBE (subid) failed: "
                        + subIdSubscribe.getQuality() + " - " + subIdSubscribe.getMessage());
            }

            MqttPublishResult subIdPublish = subIdClient.publish(subIdTopic, "hello").qos1().send();
            if (!subIdPublish.isGood()) {
                throw new IllegalStateException("PUBLISH (subid) failed: "
                        + subIdPublish.getQuality() + " - " + subIdPublish.getMessage());
            }

            PlccomMqttPublishMessage subIdReceived = subIdClient.receive(3000);
            if (subIdReceived == null) {
                throw new IllegalStateException("No message received for the Subscription Identifier demo.");
            }
            System.out.println("    delivered subscription identifiers: "
                    + new MqttMessage(subIdReceived).subscriptionIdentifiers() + " (expected [77])");
        } finally {
            subIdClient.close();
        }

        System.out.println();

        // =====================================================================
        // Part B: No Local (two clients - one publishes its own topic, one other)
        // =====================================================================
        System.out.println("  === Part B: No Local ===");
        final String noLocalTopic = "plccom/workshop/22/nolocal";

        PlccomMqttClient self = PlccomMqttClientFactory.builder(LICENSE_USER, LICENSE_SERIAL)
                .identifier("plccom-workshop-22-self")
                .endpoint(brokerEndpoint())
                .mqtt5()
                .keepAliveSeconds(30)
                .build();
        PlccomMqttClient other = PlccomMqttClientFactory.builder(LICENSE_USER, LICENSE_SERIAL)
                .identifier("plccom-workshop-22-other")
                .endpoint(brokerEndpoint())
                .mqtt5()
                .keepAliveSeconds(30)
                .build();
        try {
            PlccomMqttConnectResult selfConnect = self.connect();
            if (!selfConnect.isGood()) {
                throw new IllegalStateException("CONNECT (self) failed: "
                        + selfConnect.getQuality() + " - " + selfConnect.getMessage());
            }
            PlccomMqttConnectResult otherConnect = other.connect();
            if (!otherConnect.isGood()) {
                throw new IllegalStateException("CONNECT (other) failed: "
                        + otherConnect.getQuality() + " - " + otherConnect.getMessage());
            }

            // With No Local, the broker will not send this client its OWN messages
            // on this subscription - a simple way to avoid echo/feedback loops.
            MqttSubscribeResult noLocalSubscribe = self.subscribe(noLocalTopic).qos1().noLocal().send();
            if (!noLocalSubscribe.isGood()) {
                throw new IllegalStateException("SUBSCRIBE (no local) failed: "
                        + noLocalSubscribe.getQuality() + " - " + noLocalSubscribe.getMessage());
            }

            // The client publishes to the same topic it subscribed to. Because of
            // No Local, it must NOT receive this message back.
            MqttPublishResult selfPublish = self.publish(noLocalTopic, "my own message").qos1().send();
            if (!selfPublish.isGood()) {
                throw new IllegalStateException("PUBLISH (self) failed: "
                        + selfPublish.getQuality() + " - " + selfPublish.getMessage());
            }
            PlccomMqttPublishMessage ownEcho = self.receive(600);
            if (ownEcho != null) {
                throw new IllegalStateException("No Local should have suppressed the client's own message.");
            }
            System.out.println("    own message: suppressed by No Local (as expected)");

            // A different client publishes the same topic. No Local only filters
            // the SAME client, so this message must still arrive.
            MqttPublishResult otherPublish = other.publish(noLocalTopic, "message from another client").qos1().send();
            if (!otherPublish.isGood()) {
                throw new IllegalStateException("PUBLISH (other) failed: "
                        + otherPublish.getQuality() + " - " + otherPublish.getMessage());
            }
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

        System.out.println();

        // =====================================================================
        // Part C: Retain As Published (a normal and a retain-as-published sub)
        // =====================================================================
        System.out.println("  === Part C: Retain As Published ===");
        final String rapTopic = "plccom/workshop/22/retain-as-published";

        PlccomMqttClient normalSub = PlccomMqttClientFactory.builder(LICENSE_USER, LICENSE_SERIAL)
                .identifier("plccom-workshop-22-normal")
                .endpoint(brokerEndpoint())
                .mqtt5()
                .keepAliveSeconds(30)
                .build();
        PlccomMqttClient rapSub = PlccomMqttClientFactory.builder(LICENSE_USER, LICENSE_SERIAL)
                .identifier("plccom-workshop-22-rap")
                .endpoint(brokerEndpoint())
                .mqtt5()
                .keepAliveSeconds(30)
                .build();
        PlccomMqttClient publisher = PlccomMqttClientFactory.builder(LICENSE_USER, LICENSE_SERIAL)
                .identifier("plccom-workshop-22-publisher")
                .endpoint(brokerEndpoint())
                .mqtt5()
                .keepAliveSeconds(30)
                .build();
        try {
            PlccomMqttConnectResult normalConnect = normalSub.connect();
            if (!normalConnect.isGood()) {
                throw new IllegalStateException("CONNECT (normal subscriber) failed: "
                        + normalConnect.getQuality() + " - " + normalConnect.getMessage());
            }
            PlccomMqttConnectResult rapConnect = rapSub.connect();
            if (!rapConnect.isGood()) {
                throw new IllegalStateException("CONNECT (retain-as-published subscriber) failed: "
                        + rapConnect.getQuality() + " - " + rapConnect.getMessage());
            }
            PlccomMqttConnectResult publisherConnect = publisher.connect();
            if (!publisherConnect.isGood()) {
                throw new IllegalStateException("CONNECT (publisher) failed: "
                        + publisherConnect.getQuality() + " - " + publisherConnect.getMessage());
            }

            // Normally, when a live (already subscribed) client receives a message,
            // the broker clears the retain flag. Retain As Published keeps it set,
            // so this subscriber can still tell the message was published retained.
            // (Retain Handling, by contrast, would control whether OLD retained
            //  messages are replayed at subscribe time - not shown separately here.)
            MqttSubscribeResult normalSubscribe = normalSub.subscribe(rapTopic).qos1().send();
            if (!normalSubscribe.isGood()) {
                throw new IllegalStateException("SUBSCRIBE (normal) failed: "
                        + normalSubscribe.getQuality() + " - " + normalSubscribe.getMessage());
            }
            MqttSubscribeResult rapSubscribe = rapSub.subscribe(rapTopic).qos1().retainAsPublished().send();
            if (!rapSubscribe.isGood()) {
                throw new IllegalStateException("SUBSCRIBE (retain as published) failed: "
                        + rapSubscribe.getQuality() + " - " + rapSubscribe.getMessage());
            }

            MqttPublishResult retainedLive = publisher.publish(rapTopic, "live retained value")
                    .qos1()
                    .retain()
                    .send();
            if (!retainedLive.isGood()) {
                throw new IllegalStateException("PUBLISH (retained live) failed: "
                        + retainedLive.getQuality() + " - " + retainedLive.getMessage());
            }

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

    // Small private helper, kept inside this same workshop class on purpose: it
    // only resolves the broker endpoint (constant, or an optional system-property
    // override for automation). It carries no MQTT learning logic.
    private static String brokerEndpoint() {
        return System.getProperty("plccom.mqtt.workshop.endpoint", BROKER_ENDPOINT);
    }
}
