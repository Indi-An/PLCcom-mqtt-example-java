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

import java.nio.charset.StandardCharsets;

import com.plccom.console.PLCcomConsole;
import com.plccom.mqtt.client.MqttRequestResponseResult;
import com.plccom.mqtt.client.PlccomMqttClient;
import com.plccom.mqtt.client.PlccomMqttClientFactory;
import com.plccom.mqtt.client.PlccomMqttConnectResult;

/**
 * Workshop 23 - Request / Response (MQTT 5)
 *
 * WHAT THIS WORKSHOP SHOWS
 *   How to do a request/response exchange over MQTT. MQTT is publish/subscribe,
 *   so request/response is a convention built from two MQTT 5 properties: a
 *   Response Topic (where the answer should go) and Correlation Data (an id that
 *   lets the requester match an answer to its question). The high-level
 *   request(...) call wraps this into a single blocking operation.
 *
 * WHEN YOU NEED THIS IN REAL APPLICATIONS
 *   Command/acknowledge and query/answer patterns: ask a device for its status,
 *   send a command and wait for the result, all while staying interoperable with
 *   any standard MQTT 5 client or broker.
 *
 * BROKER FOR THIS WORKSHOP
 *   This client workshop needs a running MQTT 5 broker. Start Broker Workshop 11
 *   (First Broker) first, or point BROKER_ENDPOINT below at any MQTT 5 broker of
 *   your choice (for example a local Mosquitto). No broker is embedded here, so
 *   a client-only PLCcom license is enough. Override with
 *   -Dplccom.mqtt.workshop.endpoint=mqtt://host:port .
 *
 *   The responder in this workshop is a SECOND, fully visible MQTT client. This
 *   is deliberate: the other side of a request/response is itself just an
 *   ordinary MQTT client, and nothing important is hidden in a helper.
 *
 * MQTT TERMS USED HERE
 *   - Response Topic   : an MQTT 5 property naming the topic where the reply
 *                        should be published.
 *   - Correlation Data : opaque bytes the requester chooses. The responder must
 *                        copy them into the reply so the requester can match the
 *                        answer to the original request.
 *   - request(...)     : the high-level convenience that publishes the request,
 *                        subscribes to the response topic and returns the
 *                        matching reply as one operation.
 *
 * EXPECTED RESULT
 *   The requester receives exactly the reply that carries its Correlation Data.
 */
public final class _23_RequestResponse {

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
    // Start Broker Workshop 11, or change this to any MQTT 5 broker you like.
    // The scheme must be mqtt:// / mqtts:// / ws:// / wss:// - not tcp:// .
    private static final String BROKER_ENDPOINT = "mqtt://localhost:1883";
    // =========================================================================

    private static final String REQUEST_TOPIC  = "plccom/workshop/23/status/request";
    private static final String RESPONSE_TOPIC = "plccom/workshop/23/status/response";

    private _23_RequestResponse() {
    }

    /**
     * Opens the console and runs the workshop.
     *
     * @param args command-line arguments; not used by this workshop
     */
    public static void main(String[] args) {
        PLCcomConsole.open("PLCcom MQTT Workshop 23 - Request / Response", 1000);
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
     * Runs one complete request/response round trip between two clients.
     *
     * @throws Exception if the exchange cannot be completed
     */
    public static void start() throws Exception {
        System.out.println("╔════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║  What this workshop demonstrates                                           ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  Request/response over MQTT - a convention, not a new packet type.         ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  The requester publishes with a Response Topic (where to reply) and        ║");
        System.out.println("║  Correlation Data (an id so the answer can be matched to the question).    ║");
        System.out.println("║  A responder copies the Correlation Data into its reply on the Response    ║");
        System.out.println("║  Topic. The high-level request(...) call wraps this into one operation.    ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  Here the responder is a second, fully visible MQTT client - no hidden     ║");
        System.out.println("║  helper - so you can see both sides of the exchange.                       ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  Expected result: the requester receives exactly the reply for its id.     ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════════════╝");
        System.out.println();

        System.out.println("  Broker endpoint : " + brokerEndpoint());
        System.out.println();

        // Two ordinary MQTT 5 clients: one asks, one answers. Both are built and
        // connected right here, and each CONNECT result is checked in place.
        PlccomMqttClient requester = PlccomMqttClientFactory.builder(LICENSE_USER, LICENSE_SERIAL)
                .identifier("plccom-workshop-23-requester")
                .endpoint(brokerEndpoint())
                .mqtt5()
                .keepAliveSeconds(30)
                .build();
        PlccomMqttConnectResult requesterConnect = requester.connect();
        if (!requesterConnect.isGood()) {
            throw new IllegalStateException("CONNECT (requester) failed: "
                    + requesterConnect.getQuality() + " - " + requesterConnect.getMessage());
        }

        PlccomMqttClient responder = PlccomMqttClientFactory.builder(LICENSE_USER, LICENSE_SERIAL)
                .identifier("plccom-workshop-23-responder")
                .endpoint(brokerEndpoint())
                .mqtt5()
                .keepAliveSeconds(30)
                .build();
        PlccomMqttConnectResult responderConnect = responder.connect();
        if (!responderConnect.isGood()) {
            throw new IllegalStateException("CONNECT (responder) failed: "
                    + responderConnect.getQuality() + " - " + responderConnect.getMessage());
        }
        try {
            // -- Step 1: the responder listens for requests -------------------
            // It subscribes to the request topic and, for each request, replies
            // on the message's Response Topic, copying back the Correlation Data.
            // This is exactly what any MQTT 5 request/response service must do.
            System.out.println("  Responder subscribes to " + REQUEST_TOPIC + " and will answer each request.");
            responder.subscribe(REQUEST_TOPIC)
                    .qos1()
                    .onMessage(message -> {
                        String replyTo = message.responseTopic();
                        if (replyTo == null) {
                            // A request without a Response Topic cannot be answered.
                            return;
                        }
                        // Publish the answer on the requested Response Topic and
                        // copy the Correlation Data so the requester can match it.
                        responder.publish(replyTo, "status = RUNNING")
                                .qos1()
                                .payloadFormatUtf8()
                                .correlationData(message.correlationData())
                                .send();
                    })
                    .send();

            // -- Step 2: the requester sends a request and waits for the reply -
            // request(...) publishes the request (with Response Topic and
            // Correlation Data), subscribes to the response topic for us, waits
            // up to the timeout and returns the matching reply as one result.
            System.out.println("  Requester sends a request and waits for the correlated reply ...");
            MqttRequestResponseResult result = requester.request(REQUEST_TOPIC, "what is your status?")
                    .responseTopic(RESPONSE_TOPIC)
                    .correlationData("request-0001".getBytes(StandardCharsets.UTF_8))
                    .qos1()
                    .responseQos1()
                    .timeoutMillis(3000)
                    .send();
            if (!result.isGood()) {
                throw new IllegalStateException("Request failed: "
                        + result.getQuality() + " - " + result.getMessage());
            }

            System.out.println();
            System.out.println("    request topic  : " + result.getRequestTopic());
            System.out.println("    response topic : " + result.getResponseTopic());
            System.out.println("    reply payload  : " + result.getResponse().payloadAsString());
            System.out.println();
            System.out.println("  Success: the correlated response was received via one request(...) call.");
        } finally {
            responder.close();
            requester.close();
        }
    }

    private static String brokerEndpoint() {
        return System.getProperty("plccom.mqtt.workshop.endpoint", BROKER_ENDPOINT);
    }
}
