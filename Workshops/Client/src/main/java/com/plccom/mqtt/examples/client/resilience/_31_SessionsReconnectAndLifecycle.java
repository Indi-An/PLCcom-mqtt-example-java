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

import com.plccom.console.PLCcomConsole;
import com.plccom.mqtt.client.MqttMessage;
import com.plccom.mqtt.client.PlccomMqttClient;
import com.plccom.mqtt.client.PlccomMqttClientFactory;
import com.plccom.mqtt.client.PlccomMqttConnectResult;
import com.plccom.mqtt.client.PlccomMqttPublishMessage;

/**
 * Workshop 31 - Sessions, Reconnect and Lifecycle (Resilience)
 *
 * WHAT THIS WORKSHOP SHOWS
 *   How a client keeps working across connection loss. A persistent session
 *   (Clean Start = false plus a Session Expiry) lets the broker remember the
 *   client's subscriptions and queue QoS 1/2 messages while it is offline. When
 *   the client reconnects with the SAME Client Identifier it resumes the session
 *   and receives the queued messages. Lifecycle callbacks report connection
 *   state changes, and automatic reconnect can retry a lost connection for you.
 *
 * WHEN YOU NEED THIS IN REAL APPLICATIONS
 *   Field devices and services on unreliable networks (mobile, plant Wi-Fi) that
 *   must not miss important messages during a short outage, and applications that
 *   need to react to connect/disconnect events (update a status LED, pause work).
 *
 * BROKER FOR THIS WORKSHOP
 *   This client workshop needs a running MQTT 5 broker with a stable address (a
 *   persistent session must survive a reconnect to the same endpoint). Start
 *   Broker Workshop 11 (First Broker) first, or point BROKER_ENDPOINT below at
 *   any MQTT 5 broker of your choice (for example a local Mosquitto). No broker
 *   is embedded here, so a client-only PLCcom license is enough. Override with
 *   -Dplccom.mqtt.workshop.endpoint=mqtt://host:port .
 *
 * MQTT TERMS USED HERE
 *   - Clean Start          : when false, the broker keeps (or resumes) the
 *                            session for this Client Identifier.
 *   - Session Expiry       : how long the broker keeps the session after the
 *                            connection closes.
 *   - Offline queue        : QoS 1/2 messages the broker stores for a persistent
 *                            session while the client is disconnected.
 *   - Lifecycle callbacks  : onConnected / onDisconnected notifications, plus
 *                            onReconnectFailed when automatic reconnect gives up.
 *   - Automatic reconnect  : the client re-establishes a lost connection using a
 *                            bounded backoff (automaticReconnect()).
 *
 * EXPECTED RESULT
 *   Lifecycle callbacks print the connect and disconnect events, and a message
 *   published while the subscriber is offline is delivered after it reconnects
 *   with the same Client Identifier.
 */
public final class _31_SessionsReconnectAndLifecycle {

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

    // The Client Identifier is the key to the stored session. Reconnecting with
    // the same id is what resumes the persistent session.
    private static final String SESSION_CLIENT_ID = "plccom-workshop-31-device";
    private static final String TOPIC = "plccom/workshop/31/offline";

    private _31_SessionsReconnectAndLifecycle() {
    }

    /**
     * Opens the console and runs the workshop.
     *
     * @param args command-line arguments; not used by this workshop
     */
    public static void main(String[] args) {
        PLCcomConsole.open("PLCcom MQTT Workshop 31 - Sessions, Reconnect and Lifecycle", 1000);
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
     * Demonstrates lifecycle callbacks and a persistent-session offline queue.
     *
     * @throws Exception if the session does not queue and replay as expected
     */
    public static void start() throws Exception {
        System.out.println("╔════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║  What this workshop demonstrates                                           ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  How a client survives connection loss: sessions, reconnect, lifecycle.    ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  Clean Start = false plus a Session Expiry keeps the broker's memory of    ║");
        System.out.println("║  your subscriptions, so QoS 1/2 messages are queued while you are offline. ║");
        System.out.println("║  Reconnect with the SAME Client Identifier to resume the session and       ║");
        System.out.println("║  receive the queued messages.                                              ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  Lifecycle callbacks (onConnected / onDisconnected) tell the application   ║");
        System.out.println("║  when the connection state changes. automaticReconnect() retries for you.  ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  Expected result: a message published while offline is delivered after     ║");
        System.out.println("║  reconnecting with the same client id.                                     ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════════════╝");
        System.out.println();

        System.out.println("  Broker endpoint : " + brokerEndpoint());
        System.out.println();

        // -- Step 1: open a persistent session and subscribe ------------------
        // cleanStart(false) + sessionExpirySeconds(...) create a session the
        // broker remembers. The lifecycle callbacks print connection changes.
        // automaticReconnect() is enabled to show how it is configured (it would
        // silently re-establish the connection if the network dropped).
        System.out.println("  Opening a persistent session (cleanStart=false, sessionExpiry=60s) ...");
        // The session settings that matter are written out in full here:
        // cleanStart(false) plus a Session Expiry keep the session, automaticReconnect()
        // would retry a lost connection, and the lifecycle callbacks print state changes.
        PlccomMqttClient first = PlccomMqttClientFactory.builder(LICENSE_USER, LICENSE_SERIAL)
                .identifier(SESSION_CLIENT_ID)
                .endpoint(brokerEndpoint())
                .mqtt5()
                .cleanStart(false)                       // keep/resume the session
                .sessionExpirySeconds(Long.valueOf(60))  // survive up to 60 s offline
                .automaticReconnect()                    // retry a lost connection
                .keepAliveSeconds(30)
                .onConnected(context -> System.out.println("    [lifecycle] connected"))
                .onDisconnected(context -> System.out.println("    [lifecycle] disconnected"))
                .onReconnectFailed(context -> System.out.println("    [lifecycle] automatic reconnect finally gave up"))
                .build();
        PlccomMqttConnectResult firstConnect = first.connect();
        if (!firstConnect.isGood()) {
            throw new IllegalStateException("CONNECT (first session) failed: "
                    + firstConnect.getQuality() + " - " + firstConnect.getMessage());
        }
        first.subscribe(TOPIC).qos1().send();
        System.out.println("  Subscribed. Now disconnecting to simulate going offline ...");
        // A clean disconnect keeps the session (it does not delete it); the broker
        // will queue matching QoS 1/2 messages until we return.
        first.disconnect();
        first.close();

        // -- Step 2: publish while the subscriber is offline ------------------
        System.out.println();
        System.out.println("  A publisher sends a QoS 1 message while the subscriber is offline ...");
        PlccomMqttClient publisher = PlccomMqttClientFactory.builder(LICENSE_USER, LICENSE_SERIAL)
                .identifier("plccom-workshop-31-publisher")
                .endpoint(brokerEndpoint())
                .mqtt5()
                .keepAliveSeconds(30)
                .build();
        PlccomMqttConnectResult publisherConnect = publisher.connect();
        if (!publisherConnect.isGood()) {
            throw new IllegalStateException("CONNECT (publisher) failed: "
                    + publisherConnect.getQuality() + " - " + publisherConnect.getMessage());
        }
        publisher.publish(TOPIC, "queued while the subscriber was offline")
                .qos1()
                .payloadFormatUtf8()
                .send();
        publisher.close();

        // -- Step 3: reconnect with the SAME client id and drain the queue ----
        System.out.println();
        System.out.println("  Reconnecting with the same Client Identifier to resume the session ...");
        // Same builder as the first connection - reconnecting with the SAME
        // Client Identifier and cleanStart(false) is what resumes the session.
        PlccomMqttClient second = PlccomMqttClientFactory.builder(LICENSE_USER, LICENSE_SERIAL)
                .identifier(SESSION_CLIENT_ID)
                .endpoint(brokerEndpoint())
                .mqtt5()
                .cleanStart(false)
                .sessionExpirySeconds(Long.valueOf(60))
                .automaticReconnect()
                .keepAliveSeconds(30)
                .onConnected(context -> System.out.println("    [lifecycle] connected"))
                .onDisconnected(context -> System.out.println("    [lifecycle] disconnected"))
                .onReconnectFailed(context -> System.out.println("    [lifecycle] automatic reconnect finally gave up"))
                .build();
        PlccomMqttConnectResult secondConnect = second.connect();
        if (!secondConnect.isGood()) {
            throw new IllegalStateException("CONNECT (resumed session) failed: "
                    + secondConnect.getQuality() + " - " + secondConnect.getMessage());
        }
        try {
            PlccomMqttPublishMessage queued = second.receive(3000);
            if (queued == null) {
                throw new IllegalStateException("No queued message was replayed after reconnect.");
            }
            System.out.println("    received queued payload: '" + new MqttMessage(queued).payloadAsString() + "'");
            System.out.println();
            System.out.println("  Success: the persistent session queued the message and replayed it.");
        } finally {
            second.close();
        }
    }

    // Small private helper, kept inside this same workshop class on purpose: it
    // only resolves the broker endpoint (constant, or an optional system-property
    // override for automation). It carries no MQTT learning logic.
    private static String brokerEndpoint() {
        return System.getProperty("plccom.mqtt.workshop.endpoint", BROKER_ENDPOINT);
    }
}
