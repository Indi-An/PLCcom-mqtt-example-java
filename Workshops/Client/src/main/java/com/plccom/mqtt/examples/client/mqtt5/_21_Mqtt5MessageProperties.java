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
import com.plccom.mqtt.client.PlccomMqttUserProperty;

/**
 * Workshop 21 - MQTT 5 Message Properties (MQTT 5)
 *
 * WHAT THIS WORKSHOP SHOWS
 *   How to attach and read MQTT 5 message properties: metadata that travels next
 *   to the payload so a receiver can understand the bytes without a private
 *   agreement. It covers Content Type, Payload Format Indicator, User Properties
 *   and Message Expiry, and it explains Topic Alias.
 *
 * WHEN YOU NEED THIS IN REAL APPLICATIONS
 *   Whenever a payload needs meaning beyond raw bytes: telling a receiver that a
 *   body is JSON, tagging a message with an application key/value, or letting the
 *   broker drop stale messages for offline subscribers after a time limit.
 *
 * BROKER FOR THIS WORKSHOP
 *   This client workshop needs a running MQTT broker that speaks MQTT 5. Start
 *   Broker Workshop 11 (First Broker) first, or point BROKER_ENDPOINT below at
 *   any MQTT 5 broker of your choice (for example a local Mosquitto). No broker
 *   is embedded here, so a client-only PLCcom license is enough. Override with
 *   -Dplccom.mqtt.workshop.endpoint=mqtt://host:port .
 *
 * MQTT TERMS USED HERE
 *   - Content Type            : an IANA media type such as application/json that
 *                               describes the payload format.
 *   - Payload Format Indicator: marks the payload as UTF-8 text (true) or as
 *                               unspecified binary data (false).
 *   - User Properties         : application-defined key/value pairs. The broker
 *                               forwards them unchanged and never interprets them.
 *   - Message Expiry Interval : how long the broker may keep the message for an
 *                               offline subscriber before discarding it.
 *   - Topic Alias             : an MQTT 5 optimization that replaces a long topic
 *                               string with a small integer for repeated
 *                               publishes on the same connection, saving
 *                               bandwidth. The SDK negotiates and applies aliases
 *                               transparently; you only enable inbound aliases
 *                               with topicAliasMaximum(...).
 *
 * EXPECTED RESULT
 *   The received message shows the Content Type, Payload Format, Message Expiry
 *   and User Properties exactly as they were published.
 */
public final class _21_Mqtt5MessageProperties {

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

    private static final String TOPIC = "plccom/workshop/21/telemetry";

    private _21_Mqtt5MessageProperties() {
    }

    /**
     * Opens the console and runs the workshop.
     *
     * @param args command-line arguments; not used by this workshop
     */
    public static void main(String[] args) {
        PLCcomConsole.open("PLCcom MQTT Workshop 21 - MQTT 5 Message Properties", 1000);
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
     * Executes the MQTT 5 message-property flow.
     *
     * @throws Exception if the workshop cannot connect, subscribe, publish or
     *         receive its demonstration message
     */
    public static void start() throws Exception {
        System.out.println("╔════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║  What this workshop demonstrates                                           ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  MQTT 5 message properties: metadata that travels next to the payload      ║");
        System.out.println("║  so a receiver understands the bytes without a private convention.         ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  Content Type   : a media type such as application/json.                   ║");
        System.out.println("║  Payload Format : marks the payload as UTF-8 text or raw bytes.            ║");
        System.out.println("║  User Properties: free key/value pairs the broker forwards unchanged.      ║");
        System.out.println("║  Message Expiry : how long the broker may keep the message for offline     ║");
        System.out.println("║                   subscribers before discarding it.                        ║");
        System.out.println("║  Topic Alias    : replace a long topic with a short number to save         ║");
        System.out.println("║                   bandwidth on repeated publishes.                         ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  Expected result: the received message shows every property that was set.  ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════════════╝");
        System.out.println();

        System.out.println("  Broker endpoint : " + brokerEndpoint());
        System.out.println();

        // -- Step 1: connect an MQTT 5 client ---------------------------------
        // mqtt5() is required: all properties below exist only in MQTT 5.
        // topicAliasMaximum(8) tells the broker this client accepts up to 8
        // inbound Topic Aliases; outbound aliases are negotiated automatically.
        PlccomMqttClient client = PlccomMqttClientFactory.builder(LICENSE_USER, LICENSE_SERIAL)
                .identifier("plccom-workshop-21")
                .endpoint(brokerEndpoint())
                .mqtt5()
                .topicAliasMaximum(8)
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

            // -- Step 2: subscribe --------------------------------------------
            MqttSubscribeResult subscribeResult = client.subscribe(TOPIC).qos1().send();
            if (!subscribeResult.isGood()) {
                throw new IllegalStateException("SUBSCRIBE failed: "
                        + subscribeResult.getQuality() + " - " + subscribeResult.getMessage());
            }

            // -- Step 3: publish one message rich with MQTT 5 properties ------
            // The payload is ordinary JSON text. The properties describe it so a
            // receiver (or a generic MQTT tool) understands it without guessing.
            System.out.println("  Publishing a message with MQTT 5 properties ...");
            MqttPublishResult publishResult = client.publish(TOPIC, "{\"temperature\":22.4}")
                    .qos1()
                    .contentType("application/json")   // what the payload is
                    .payloadFormatUtf8()               // it is UTF-8 text, not binary
                    .messageExpirySeconds(30)          // broker may drop it after 30 s
                    .userProperty("unit", "celsius")   // application metadata ...
                    .userProperty("source", "sensor-A")// ... may be repeated
                    .send();
            if (!publishResult.isGood()) {
                throw new IllegalStateException("PUBLISH failed: "
                        + publishResult.getQuality() + " - " + publishResult.getMessage());
            }

            // -- Step 4: receive and read every property back -----------------
            System.out.println("  Receiving and reading the properties back:");
            PlccomMqttPublishMessage received = client.receive(3000);
            if (received == null) {
                throw new IllegalStateException("The MQTT 5 message was not received in time.");
            }
            MqttMessage message = new MqttMessage(received);
            System.out.println("    payload          : " + message.payloadAsString());
            System.out.println("    content type     : " + message.contentType());
            System.out.println("    payload is UTF-8 : " + message.payloadFormatUtf8());
            System.out.println("    message expiry s : " + message.messageExpirySeconds());
            System.out.println("    user properties  :");
            for (PlccomMqttUserProperty property : message.userProperties()) {
                System.out.println("        " + property.getName() + " = " + property.getValue());
            }

            // -- Step 5: Topic Alias note -------------------------------------
            // Topic Alias is a bandwidth optimization for repeated publishes on
            // the same connection: after the first full topic, the SDK may send a
            // short alias number instead of the long topic string. It is applied
            // transparently by the SDK when the broker advertises support, so
            // there is no extra code to write - only topicAliasMaximum(...) above
            // to accept inbound aliases. The two publishes below would benefit.
            System.out.println();
            System.out.println("  Publishing twice to a long topic (Topic Alias may compress the repeat):");
            String longTopic = "plccom/workshop/21/site/hall-3/line-7/station-42/sensor/temperature";
            client.subscribe(longTopic).qos1().send();
            client.publish(longTopic, "22.5").qos1().payloadFormatUtf8().send();
            client.publish(longTopic, "22.6").qos1().payloadFormatUtf8().send();
            for (int i = 0; i < 2; i++) {
                PlccomMqttPublishMessage repeat = client.receive(3000);
                if (repeat != null) {
                    System.out.println("    received on long topic: " + new MqttMessage(repeat).payloadAsString());
                }
            }

            System.out.println();
            System.out.println("  Success: all MQTT 5 properties were transported and read back.");
        } finally {
            client.close();
        }
    }

    // Resolves the broker endpoint: the visible constant, or an optional
    // system-property override used by the internal automation.
    private static String brokerEndpoint() {
        return System.getProperty("plccom.mqtt.workshop.endpoint", BROKER_ENDPOINT);
    }
}
