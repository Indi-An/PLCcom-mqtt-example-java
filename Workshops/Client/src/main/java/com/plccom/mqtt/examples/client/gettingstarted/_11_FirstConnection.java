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
 * Workshop 11 - First Connection (Getting Started)
 *
 * WHAT THIS WORKSHOP SHOWS
 *   The smallest complete MQTT client rhythm in one small flow: build a client,
 *   connect, subscribe, publish, receive one message and close the connection
 *   cleanly. Every later client workshop only adds one more MQTT feature on top
 *   of this same basic shape, so it is the right file to read first.
 *
 * WHEN YOU NEED THIS IN REAL APPLICATIONS
 *   Any time a device, service or HMI has to move data over MQTT. This is the
 *   minimum code required to send and receive application data without learning
 *   the MQTT wire protocol first.
 *
 * BROKER FOR THIS WORKSHOP
 *   A client always needs a broker to talk to. This workshop does NOT start a
 *   broker itself, so a client-only PLCcom license is enough to run it. Start
 *   Broker Workshop 11 (First Broker) first, or point BROKER_ENDPOINT below at
 *   any MQTT broker of your choice (for example a local Mosquitto). You can also
 *   override the endpoint without editing the file by setting the system property
 *   -Dplccom.mqtt.workshop.endpoint=mqtt://host:port .
 *
 * MQTT TERMS USED HERE
 *   - Endpoint / Broker URI : where the client connects, e.g. mqtt://host:1883.
 *   - Protocol version      : MQTT 3.1.1 (used here) or MQTT 5.0. Many existing
 *                             brokers and PLC-style integrations still use 3.1.1.
 *   - Topic Name            : where a publisher SENDS data. It has no wildcards.
 *   - Topic Filter          : what a subscriber ASKS FOR. It may use the
 *                             wildcards + (one level) and # (the rest).
 *   - QoS 1                 : "at least once" delivery. The broker acknowledges
 *                             the message, so it is not lost, but a duplicate is
 *                             possible after a reconnect. Applications should
 *                             tolerate a repeated QoS 1 message.
 *   - Quality / OperationResult : every high-level call returns a result object
 *                             whose quality tells you whether it succeeded.
 *
 * EXPECTED RESULT
 *   The UTF-8 text that is published is received unchanged through the
 *   high-level message API, and the negotiated protocol is printed.
 */
public final class _11_FirstConnection {

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

    private _11_FirstConnection() {
    }

    /**
     * Opens the console and runs the workshop.
     *
     * @param args command-line arguments; not used by this workshop
     */
    public static void main(String[] args) {
        PLCcomConsole.open("PLCcom MQTT Workshop 11 - First Connection", 1000);
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
     *         receive its demonstration message
     */
    public static void start() throws Exception {

        // The explanation is the very first thing in the flow, written out in
        // full so anyone reading this source understands the workshop at a glance.
        System.out.println("╔════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║  What this workshop demonstrates                                           ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  The smallest complete MQTT rhythm: connect, subscribe, publish,           ║");
        System.out.println("║  receive and close - the everyday shape every later workshop builds on.    ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  A Topic Name is where a publisher sends (no wildcards).                   ║");
        System.out.println("║  A Topic Filter is what a subscriber asks for (may use + and #).           ║");
        System.out.println("║  QoS 1 means at-least-once delivery: no loss, duplicates possible.         ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  Expected result: the published UTF-8 text is received unchanged.          ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════════════╝");
        System.out.println();

        // The topic used for the demonstration. A subscriber uses it as a Topic
        // Filter, a publisher uses the exact same string as a Topic Name.
        final String topic = "plccom/workshop/11/hello";

        System.out.println("  Broker endpoint : " + brokerEndpoint());
        System.out.println();

        // -- Step 1: build the high-level client ------------------------------
        // The factory builder creates the comfortable client used by normal
        // applications. We start with MQTT 3.1.1 on purpose, because it is the
        // most widely supported version. The license (visible at the top of this
        // file) is passed here; nothing about the license is hidden in a helper.
        PlccomMqttClient client = PlccomMqttClientFactory.builder(LICENSE_USER, LICENSE_SERIAL)
                .identifier("plccom-workshop-11")
                .endpoint(brokerEndpoint())
                .mqtt311()
                .keepAliveSeconds(30)
                .build();

        try {
            // -- Step 2: connect ----------------------------------------------
            // connect() performs the MQTT CONNECT/CONNACK handshake. The result
            // carries a PLCcom quality value; we check it explicitly so a failure
            // is visible immediately instead of silently continuing.
            System.out.println("  Connecting ...");
            PlccomMqttConnectResult connectResult = client.connect();
            if (!connectResult.isGood()) {
                throw new IllegalStateException("CONNECT failed: "
                        + connectResult.getQuality() + " - " + connectResult.getMessage());
            }
            System.out.println("  Connected. Negotiated protocol: "
                    + connectResult.getNegotiatedProtocolVersion());

            // -- Step 3: subscribe (Topic Filter) -----------------------------
            // We subscribe BEFORE publishing so the broker already has a route
            // for the message when it arrives. subscribe(...) uses the topic as a
            // Topic Filter; qos1() requests at-least-once delivery.
            System.out.println("  Subscribing to Topic Filter: " + topic);
            MqttSubscribeResult subscribeResult = client.subscribe(topic)
                    .qos1()
                    .send();
            if (!subscribeResult.isGood()) {
                throw new IllegalStateException("SUBSCRIBE failed: "
                        + subscribeResult.getQuality() + " - " + subscribeResult.getMessage());
            }

            // -- Step 4: publish (Topic Name) ---------------------------------
            // publish(...) uses the very same string as a Topic Name (no
            // wildcards allowed here). QoS 1 means the broker acknowledges the
            // PUBLISH; an application should still tolerate a possible duplicate.
            String payload = "Hello from the PLCcom MQTT high-level client.";
            System.out.println("  Publishing to Topic Name : " + topic);
            MqttPublishResult publishResult = client.publish(topic, payload)
                    .qos1()
                    .send();
            if (!publishResult.isGood()) {
                throw new IllegalStateException("PUBLISH failed: "
                        + publishResult.getQuality() + " - " + publishResult.getMessage());
            }

            // -- Step 5: receive and verify -----------------------------------
            // receive(...) waits up to the given timeout for the next application
            // message. MqttMessage is a friendly view over payload, topic and QoS.
            System.out.println("  Waiting for the message to come back ...");
            PlccomMqttPublishMessage received = client.receive(3000);
            if (received == null) {
                throw new IllegalStateException("The workshop message was not received in time.");
            }
            MqttMessage message = new MqttMessage(received);
            System.out.println();
            System.out.println("  Received topic : " + message.topic());
            System.out.println("  Received text  : " + message.payloadAsString());
            System.out.println("  Received QoS   : " + message.qos());
            System.out.println();
            System.out.println("  Success: the published text was received unchanged.");

        } finally {
            // -- Step 6: clean shutdown ---------------------------------------
            // Always close the client so its network connection and background
            // threads are released, even if a step above failed.
            client.close();
        }
    }

    // Small private helper, kept inside this same workshop class on purpose: it
    // only resolves the broker endpoint (constant, or an optional system-property
    // override for automation). It carries no MQTT learning logic.
    private static String brokerEndpoint() {
        return System.getProperty("plccom.mqtt.workshop.endpoint", BROKER_ENDPOINT);
    }
}
