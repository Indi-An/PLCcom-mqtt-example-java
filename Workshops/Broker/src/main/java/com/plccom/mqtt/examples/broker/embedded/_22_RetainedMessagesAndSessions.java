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

package com.plccom.mqtt.examples.broker.embedded;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.plccom.console.PLCcomConsole;
import com.plccom.mqtt.broker.MqttBrokerListenerHandle;
import com.plccom.mqtt.broker.PlccomMqttBroker;
import com.plccom.mqtt.broker.PlccomMqttBrokerFactory;

/**
 * Workshop 22 - Retained Messages and Sessions (Embedded)
 *
 * WHAT THIS WORKSHOP SHOWS
 *   How the broker remembers state. A retained message is the broker's stored
 *   "latest value" for a topic: it is replayed to the next subscriber - even an
 *   in-process listener registered afterwards - and it is cleared by publishing
 *   an empty retained payload. The workshop also explains session state and
 *   offline queues, which the broker keeps for persistent clients.
 *
 * WHEN YOU NEED THIS IN REAL APPLICATIONS
 *   Status/configuration topics that new subscribers must see immediately
 *   (retained), and reliable delivery to clients that reconnect after an outage
 *   (persistent sessions with queued QoS 1/2 messages).
 *
 * MATCHING CLIENT
 *   Client Workshop 13 shows retained messages and Last Will from the client
 *   side; Client Workshop 31 shows persistent sessions and the offline queue.
 *   This workshop stays broker-only.
 *
 * MQTT TERMS USED HERE
 *   - Retained message : the broker's stored latest value for a Topic Name.
 *   - Retained deletion: an empty retained payload clears the stored value.
 *   - Retained replay  : the stored value is delivered to a new subscriber.
 *   - Session state    : per-client subscriptions the broker keeps for a
 *                        persistent (non-clean) session.
 *   - Offline queue    : QoS 1/2 messages the broker stores for a persistent
 *                        session while its client is disconnected.
 *
 * EXPECTED RESULT
 *   A listener registered AFTER a retained publish still receives the value with
 *   retain=true; after the value is deleted, a further listener receives nothing.
 */
public final class _22_RetainedMessagesAndSessions {

    // === YOUR PLCcom LICENSE =================================================
    // Replace both values with the user name and serial from your PLCcom license
    // e-mail. A broker (or broker+client) license is required.
    private static final String LICENSE_USER   = "<Enter your UserName here>";
    private static final String LICENSE_SERIAL = "<Enter your Serial here>";
    // =========================================================================

    private static final String STATE_TOPIC = "machine/line3/state";

    private _22_RetainedMessagesAndSessions() {
    }

    /**
     * Opens the console and runs the workshop.
     *
     * @param args command-line arguments; not used by this workshop
     */
    public static void main(String[] args) {
        PLCcomConsole.open("PLCcom MQTT Broker Workshop 22 - Retained Messages and Sessions", 1000);
        try {
            start();
        } catch (Exception ex) {
            System.out.println();
            System.out.println("  The retained-message scenario failed.");
            System.out.println("  Technical detail: " + ex.getMessage());
        } finally {
            PLCcomConsole.waitForEnter();
            PLCcomConsole.close();
        }
    }

    /**
     * Demonstrates retained replay and deletion through the embedded broker.
     *
     * @throws Exception if the broker cannot be started or replay does not occur
     */
    public static void start() throws Exception {
        System.out.println("╔════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║  What this workshop demonstrates                                           ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  Broker-side state: retained messages and (in explanation) sessions.       ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  Retained: the broker stores the last value of a topic and replays it to   ║");
        System.out.println("║  the next subscriber - even an in-process listener registered afterwards.  ║");
        System.out.println("║  Publish an empty retained payload to clear it.                            ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  Sessions/queued: for persistent clients the broker also keeps session     ║");
        System.out.println("║  state and queues QoS 1/2 messages while they are offline (see Broker      ║");
        System.out.println("║  Workshop 42 for counters, Client Workshop 31 for the client side).        ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  Expected result: a listener registered AFTER the publish still receives   ║");
        System.out.println("║  the retained value with retain=true; after deletion it receives nothing.  ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════════════╝");
        System.out.println();

        PlccomMqttBroker broker = PlccomMqttBrokerFactory.builder(LICENSE_USER, LICENSE_SERIAL)
                .tcpPort(1883) // 1883 is the standard MQTT-over-TCP port
                .start();
        try {
            // -- Step 1: publish a retained value BEFORE anyone listens -------
            // Retained storage does not need a subscriber. This is how MQTT keeps
            // "last known state" available for whoever connects next.
            System.out.println("  Publishing retained state before any listener exists ...");
            broker.publish(STATE_TOPIC, "waiting-for-operator")
                    .qos1()
                    .retain()
                    .payloadFormatUtf8()
                    .send();

            // -- Step 2: a listener registered LATER still gets the value -----
            System.out.println("  Registering a listener afterwards; it should receive the retained value ...");
            CountDownLatch replay = new CountDownLatch(1);
            MqttBrokerListenerHandle first = broker.listen(STATE_TOPIC)
                    .qos1()
                    .onMessage(message -> {
                        System.out.println("    replayed value : '" + message.payloadAsString()
                                + "'  retain=" + message.retain());
                        replay.countDown();
                    })
                    .start();
            try {
                if (!replay.await(3, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("The retained value was not replayed to the new listener.");
                }
            } finally {
                first.close();
            }

            // -- Step 3: delete the retained value (empty retained payload) ---
            System.out.println();
            System.out.println("  Deleting the retained value (empty retained payload) ...");
            broker.publish(STATE_TOPIC, new byte[0]).qos1().retain().send();

            // -- Step 4: a further listener now receives nothing --------------
            System.out.println("  Registering another listener; it should receive no retained value ...");
            CountDownLatch afterDelete = new CountDownLatch(1);
            MqttBrokerListenerHandle second = broker.listen(STATE_TOPIC)
                    .qos1()
                    .onMessage(message -> afterDelete.countDown())
                    .start();
            try {
                if (afterDelete.await(1500, TimeUnit.MILLISECONDS)) {
                    throw new IllegalStateException("A deleted retained value must not be replayed.");
                }
                System.out.println("    received nothing - retained state was cleared as expected.");
            } finally {
                second.close();
            }

            // -- Note on sessions ---------------------------------------------
            // Sessions and offline queues are per-client broker state and are best
            // observed with real clients: see Client Workshop 31 for a persistent
            // session and Broker Workshop 42 for the broker's session/queue
            // counters in the admin snapshot.
            System.out.println();
            System.out.println("  Success: retained replay and deletion behaved as MQTT defines.");
            System.out.println("  (Session state and offline queues: see Broker Workshop 42 / Client Workshop 31.)");
        } finally {
            broker.close();
            System.out.println("  Broker stopped.");
        }
    }
}
