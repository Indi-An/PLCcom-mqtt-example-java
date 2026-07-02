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
 * Workshop 33 - Shared Subscriptions (Resilience)
 *
 * WHAT THIS WORKSHOP SHOWS
 *   How to load-balance consumers with MQTT 5 Shared Subscriptions. Several
 *   clients join the same group using a {@code $share/<group>/<filter>} Topic
 *   Filter, and the broker delivers each matching message to only ONE member of
 *   the group. A normal (non-shared) subscription would instead deliver every
 *   message to every subscriber.
 *
 * WHEN YOU NEED THIS IN REAL APPLICATIONS
 *   Scaling out worker-style processing: several equal workers share the load of
 *   a job or telemetry topic, and you can add or remove workers without changing
 *   the publishers. It is the MQTT-standard alternative to an extra message-queue
 *   product for simple fan-out-to-one distribution.
 *
 * BROKER FOR THIS WORKSHOP
 *   This client workshop needs a running MQTT 5 broker that supports Shared
 *   Subscriptions. Start Broker Workshop 11 (First Broker) first, or point
 *   BROKER_ENDPOINT below at any such broker (for example a local Mosquitto). No
 *   broker is embedded here, so a client-only PLCcom license is enough. Override
 *   with -Dplccom.mqtt.workshop.endpoint=mqtt://host:port .
 *
 * MQTT TERMS USED HERE
 *   - Shared Subscription : the wire Topic Filter {@code $share/<group>/<filter>}.
 *                           The part after the group name is the normal filter.
 *   - Consumer group      : all clients that subscribe with the same share name;
 *                           the broker distributes messages among them.
 *
 * EXPECTED RESULT
 *   A batch of jobs published to the shared topic is distributed across two
 *   workers, and the total number of jobs received equals the number published.
 */
public final class _33_SharedSubscriptions {

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

    private static final String JOB_TOPIC = "plccom/workshop/33/jobs";
    private static final String SHARE_NAME = "plccom-workers";
    private static final int JOB_COUNT = 6;

    private _33_SharedSubscriptions() {
    }

    /**
     * Opens the console and runs the workshop.
     *
     * @param args command-line arguments; not used by this workshop
     */
    public static void main(String[] args) {
        PLCcomConsole.open("PLCcom MQTT Workshop 33 - Shared Subscriptions", 1000);
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
     * Distributes a batch of jobs across two shared-subscription workers.
     *
     * @throws Exception if the shared subscription does not distribute the jobs
     */
    public static void start() throws Exception {
        System.out.println("╔════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║  What this workshop demonstrates                                           ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  Shared Subscriptions - the MQTT 5 way to load-balance consumers.          ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  Several clients join one group with $share/<group>/<filter>. The broker   ║");
        System.out.println("║  delivers each matching message to only ONE member of the group, instead   ║");
        System.out.println("║  of broadcasting it to all members (as a normal subscription would).       ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  This scales out worker-style processing without an extra queue product.   ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  Expected result: a batch of jobs is distributed across two workers; the   ║");
        System.out.println("║  total received equals the total published.                                ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════════════╝");
        System.out.println();

        System.out.println("  Broker endpoint : " + brokerEndpoint());
        System.out.println();

        PlccomMqttClient workerA = connectedClient("plccom-workshop-33-worker-a");
        PlccomMqttClient workerB = connectedClient("plccom-workshop-33-worker-b");
        PlccomMqttClient publisher = connectedClient("plccom-workshop-33-publisher");
        try {
            // -- Step 1: both workers join the SAME shared group --------------
            // sharedSubscription(SHARE_NAME) turns the plain filter into
            // "$share/plccom-workers/plccom/workshop/33/jobs". Both workers use
            // the same share name, so they form one consumer group.
            System.out.println("  Two workers join shared group '" + SHARE_NAME + "' on " + JOB_TOPIC);
            workerA.subscribe(JOB_TOPIC).sharedSubscription(SHARE_NAME).qos1().send();
            workerB.subscribe(JOB_TOPIC).sharedSubscription(SHARE_NAME).qos1().send();

            // -- Step 2: publish a batch of jobs ------------------------------
            // The publisher uses the PLAIN topic (never the $share prefix). The
            // broker spreads these jobs across the group members.
            System.out.println("  Publishing " + JOB_COUNT + " jobs ...");
            for (int i = 1; i <= JOB_COUNT; i++) {
                publisher.publish(JOB_TOPIC, "job-" + i).qos1().payloadFormatUtf8().send();
            }

            // -- Step 3: drain both workers and count the distribution --------
            // We alternate short receives on both workers until no more jobs
            // arrive. The exact split between workers is broker-defined, so we
            // only assert that the TOTAL matches what was published.
            int countA = 0;
            int countB = 0;
            boolean progress = true;
            while (progress) {
                progress = false;
                PlccomMqttPublishMessage a = workerA.receive(700);
                if (a != null) {
                    countA++;
                    progress = true;
                    System.out.println("    worker A processed: " + new MqttMessage(a).payloadAsString());
                }
                PlccomMqttPublishMessage b = workerB.receive(700);
                if (b != null) {
                    countB++;
                    progress = true;
                    System.out.println("    worker B processed: " + new MqttMessage(b).payloadAsString());
                }
            }

            System.out.println();
            System.out.println("  Distribution: worker A = " + countA + ", worker B = " + countB
                    + ", total = " + (countA + countB) + " of " + JOB_COUNT);
            if (countA + countB != JOB_COUNT) {
                throw new IllegalStateException("The shared group did not deliver every job exactly once.");
            }
            System.out.println();
            System.out.println("  Success: the shared subscription load-balanced all jobs across the workers.");
        } finally {
            publisher.close();
            workerB.close();
            workerA.close();
        }
    }

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

    private static String brokerEndpoint() {
        return System.getProperty("plccom.mqtt.workshop.endpoint", BROKER_ENDPOINT);
    }
}
