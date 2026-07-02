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
import com.plccom.mqtt.client.PlccomMqttClient;
import com.plccom.mqtt.client.PlccomMqttClientFactory;
import com.plccom.mqtt.client.PlccomMqttConnectResult;
import com.plccom.mqtt.client.PlccomMqttPublishMessage;

/**
 * Workshop 24 - JSON Mapping (MQTT 5)
 *
 * WHAT THIS WORKSHOP SHOWS
 *   How to send a Java object as a JSON payload and read it back as the same
 *   type. MQTT itself only transports bytes; publishJson(...) serializes an
 *   object to UTF-8 JSON and marks it as application/json, and payloadAsJson(...)
 *   turns the received payload back into a Java object. Topic, QoS and MQTT 5
 *   metadata stay fully visible - only the payload conversion is done for you.
 *
 * WHEN YOU NEED THIS IN REAL APPLICATIONS
 *   Almost every real application sends structured data (a measurement record, a
 *   command with parameters, a status object). JSON keeps that data readable and
 *   interoperable with any MQTT peer, while this convenience keeps the Java side
 *   free of manual serialization boilerplate.
 *
 * BROKER FOR THIS WORKSHOP
 *   This client workshop needs a running MQTT 5 broker. Start Broker Workshop 11
 *   (First Broker) first, or point BROKER_ENDPOINT below at any MQTT 5 broker of
 *   your choice (for example a local Mosquitto). No broker is embedded here, so
 *   a client-only PLCcom license is enough. Override with
 *   -Dplccom.mqtt.workshop.endpoint=mqtt://host:port .
 *
 * MQTT TERMS USED HERE
 *   - JSON payload  : the message body is JSON text; MQTT does not care about the
 *                     format, it only carries the bytes.
 *   - Content Type  : publishJson(...) sets application/json so any receiver can
 *                     recognize the format without parsing the topic name.
 *   - Payload Format: the payload is marked as UTF-8 text.
 *
 * EXPECTED RESULT
 *   The MotorState object that is published arrives as an equal MotorState object
 *   on the receiving side, and the Content Type reads application/json.
 */
public final class _24_JsonMapping {

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

    private static final String TOPIC = "plccom/workshop/24/motor-state";

    private _24_JsonMapping() {
    }

    /**
     * Opens the console and runs the workshop.
     *
     * @param args command-line arguments; not used by this workshop
     */
    public static void main(String[] args) {
        PLCcomConsole.open("PLCcom MQTT Workshop 24 - JSON Mapping", 1000);
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
     * Publishes a Java object as JSON and reads it back as the same type.
     *
     * @throws Exception if the JSON round trip cannot be completed
     */
    public static void start() throws Exception {
        System.out.println("╔════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║  What this workshop demonstrates                                           ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  JSON mapping - a convenience for sending Java objects as JSON payloads.   ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  MQTT itself only transports bytes. publishJson(...) serializes a Java     ║");
        System.out.println("║  object to UTF-8 JSON and marks it as application/json; the receiver turns ║");
        System.out.println("║  the payload back into the same type with payloadAsJson(...).              ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  The topic, QoS and MQTT 5 metadata stay fully visible - only the payload  ║");
        System.out.println("║  conversion is done for you.                                               ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  Expected result: the object that is published arrives as an equal object. ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════════════╝");
        System.out.println();

        System.out.println("  Broker endpoint : " + brokerEndpoint());
        System.out.println();

        PlccomMqttClient client = PlccomMqttClientFactory.builder(LICENSE_USER, LICENSE_SERIAL)
                .identifier("plccom-workshop-24")
                .endpoint(brokerEndpoint())
                .mqtt5()
                .keepAliveSeconds(30)
                .build();

        try {
            // -- Step 1: connect and subscribe --------------------------------
            PlccomMqttConnectResult connectResult = client.connect();
            if (!connectResult.isGood()) {
                throw new IllegalStateException("CONNECT failed: "
                        + connectResult.getQuality() + " - " + connectResult.getMessage());
            }
            client.subscribe(TOPIC).qos1().send();

            // -- Step 2: publish a Java object as JSON ------------------------
            // publishJson(...) serializes the object with the built-in JSON mapper
            // and sets Content Type application/json for us. No manual toString or
            // byte handling is needed.
            MotorState sent = new MotorState("Motor A", true, 1480);
            System.out.println("  Publishing object as JSON: " + sent);
            MqttPublishResult publishResult = client.publishJson(TOPIC, sent).qos1().send();
            if (!publishResult.isGood()) {
                throw new IllegalStateException("PUBLISH failed: "
                        + publishResult.getQuality() + " - " + publishResult.getMessage());
            }

            // -- Step 3: receive and deserialize back into the same type ------
            PlccomMqttPublishMessage received = client.receive(3000);
            if (received == null) {
                throw new IllegalStateException("The JSON message was not received in time.");
            }
            MqttMessage message = new MqttMessage(received);
            MotorState back = message.payloadAsJson(MotorState.class);

            System.out.println();
            System.out.println("    content type  : " + message.contentType());
            System.out.println("    raw JSON bytes: " + message.payloadAsString());
            System.out.println("    as object     : " + back);
            System.out.println();
            System.out.println("  Success: the published object was received as an equal Java object.");
        } finally {
            client.close();
        }
    }

    private static String brokerEndpoint() {
        return System.getProperty("plccom.mqtt.workshop.endpoint", BROKER_ENDPOINT);
    }

    /**
     * A tiny data-transfer object used only to demonstrate JSON mapping. It is a
     * plain Java object with fields; the JSON mapper serializes and restores it.
     */
    private static final class MotorState {
        private String name;
        private boolean running;
        private int rpm;

        private MotorState(String name, boolean running, int rpm) {
            this.name = name;
            this.running = running;
            this.rpm = rpm;
        }

        @Override
        public String toString() {
            return "MotorState{name=" + name + ", running=" + running + ", rpm=" + rpm + "}";
        }
    }
}
