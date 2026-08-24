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

package com.plccom.mqtt.examples.broker.service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.plccom.console.PLCcomConsole;
import com.plccom.mqtt.broker.PlccomMqttBroker;
import com.plccom.mqtt.broker.PlccomMqttBrokerException;
import com.plccom.mqtt.broker.PlccomMqttBrokerFactory;
import com.plccom.mqtt.broker.PlccomMqttBrokerRetainedMessage;
import com.plccom.mqtt.broker.PlccomMqttBrokerSessionState;
import com.plccom.mqtt.broker.PlccomMqttBrokerStore;

/**
 * Workshop 41 - Custom State Stores (Operations)
 *
 * WHAT THIS WORKSHOP SHOWS
 *   How to move broker state - retained messages, sessions and offline queues -
 *   out of memory and into durable storage that YOU control. You implement the
 *   {@link PlccomMqttBrokerStore} contract and the broker calls it to save and
 *   load state. Restarting the broker with the same store recovers the retained
 *   messages, which proves durability.
 *
 * WHEN YOU NEED THIS IN REAL APPLICATIONS
 *   Production brokers that must survive a restart without losing retained state
 *   or persistent sessions, and deployments with specific storage, backup or
 *   encryption requirements the in-memory default cannot meet.
 *
 * MATCHING CLIENT
 *   None specific; this is a broker capability. Any client that uses retained
 *   messages or persistent sessions benefits from a durable store.
 *
 * MQTT TERMS USED HERE
 *   - Custom store   : the application-provided persistence for broker state.
 *   - Retained store : the part that persists retained messages.
 *   - Session store  : the part that persists persistent-session state.
 *
 * RESPONSIBILITY AND PERFORMANCE
 *   Store methods are called synchronously on broker protocol paths. Slow or
 *   failing storage directly slows down or disrupts connected clients. The
 *   application owns correctness, latency, thread-safety, backup and encryption.
 *
 * EXPECTED RESULT
 *   A retained message saved through the store is recovered after the broker is
 *   restarted with the same store.
 */
public final class _41_CustomStores {

    // === YOUR PLCcom LICENSE =================================================
    // Important !!!!!!!!!!!!!!!!!!
    // Enter your Username + Serial here! Please note: passing two empty strings runs the library
    // for 15 minutes during a debug session. Both values can also come
    // from configuration or an environment variable.
    // A broker (or broker+client) license is required for the broker workshops.
    // Free trial license (14 days, uninterrupted): https://www.indi-an.com/en/plccom/mqtt/mqtt-download/
    private static final String LICENSE_USER   = "";
    private static final String LICENSE_SERIAL = "";
    // =========================================================================

    private static final String RETAINED_TOPIC = "store/workshop/state";

    private _41_CustomStores() {
    }

    /**
     * Opens the console and runs the workshop.
     *
     * @param args command-line arguments; not used by this workshop
     */
    public static void main(String[] args) {
        PLCcomConsole.open("PLCcom MQTT Broker Workshop 41 - Custom State Stores", 1000);
        try {
            start();
        } catch (Exception ex) {
            System.out.println();
            System.out.println("  The custom-store scenario failed.");
            System.out.println("  Technical detail: " + ex.getMessage());
        } finally {
            PLCcomConsole.waitForEnter();
            PLCcomConsole.close();
        }
    }

    /**
     * Persists a retained message through a custom store and recovers it on
     * restart.
     *
     * @throws Exception if the broker cannot use the store
     */
    public static void start() throws Exception {
        System.out.println("╔════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║  What this workshop demonstrates                                           ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  Custom stores: move broker state (retained messages, sessions, offline    ║");
        System.out.println("║  queues) from memory into your own durable storage.                        ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  Implement PlccomMqttBrokerStore and the broker calls it to save and load  ║");
        System.out.println("║  state. A restart with the same store recovers the retained messages.      ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  Responsibility & performance: store methods run on broker protocol paths, ║");
        System.out.println("║  so slow or failing storage directly slows down or disrupts clients. You   ║");
        System.out.println("║  own correctness, latency, backup and encryption.                          ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  Expected result: the retained message survives a broker restart.          ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════════════╝");
        System.out.println();

        // One store instance stands in for durable storage (a file or database in
        // production). We reuse it across two broker lifetimes to show recovery.
        WorkshopStateStore store = new WorkshopStateStore();

        // -- Step 1: first broker saves a retained message via the store ------
        System.out.println("  Starting broker #1 with the custom store ...");
        // store(store) is the key call: it tells the broker to persist and load
        // its state through your PlccomMqttBrokerStore instead of memory.
        PlccomMqttBroker firstBroker = PlccomMqttBrokerFactory.builder(LICENSE_USER, LICENSE_SERIAL)
                .tcpPort(1883) // standard MQTT-over-TCP port (both brokers run sequentially, so it is reused)
                .store(store)
                .start();
        try {
            System.out.println("  Publishing a retained message (this calls the store) ...");
            firstBroker.publish(RETAINED_TOPIC, "state saved by the custom store")
                    .qos1()
                    .retain()
                    .payloadFormatUtf8()
                    .send();
        } finally {
            firstBroker.close();
        }
        System.out.println("  Retained entries held by the store: " + store.retainedCount());

        // -- Step 2: second broker recovers state from the same store ---------
        // On startup the broker calls loadRetainedMessages() before accepting
        // clients, so the retained value is available again immediately.
        System.out.println();
        System.out.println("  Starting broker #2 with the SAME store (simulating a restart) ...");
        // Same builder, same store instance - on startup the broker calls
        // loadRetainedMessages() on it before accepting clients.
        PlccomMqttBroker secondBroker = PlccomMqttBrokerFactory.builder(LICENSE_USER, LICENSE_SERIAL)
                .tcpPort(1883) // standard MQTT-over-TCP port (both brokers run sequentially, so it is reused)
                .store(store)
                .start();
        try {
            int recovered = secondBroker.admin().snapshot().getRetainedMessages().size();
            System.out.println("  Retained messages recovered after restart: " + recovered);
            if (recovered < 1) {
                throw new IllegalStateException("The retained message was not recovered from the store.");
            }
            System.out.println();
            System.out.println("  Success: broker state persisted and recovered through your own store.");
        } finally {
            secondBroker.close();
        }
    }

    /**
     * A minimal in-memory implementation of the broker store contract, used only
     * to demonstrate the callbacks. A real store would write to a file or
     * database. Note the deliberate small latency: it stands in for real storage
     * I/O and is a reminder that these calls sit on broker protocol paths.
     */
    private static final class WorkshopStateStore implements PlccomMqttBrokerStore {
        private final Map<String, PlccomMqttBrokerRetainedMessage> retained =
                new LinkedHashMap<String, PlccomMqttBrokerRetainedMessage>();
        private final Map<String, PlccomMqttBrokerSessionState> sessions =
                new LinkedHashMap<String, PlccomMqttBrokerSessionState>();

        @Override
        public synchronized List<PlccomMqttBrokerRetainedMessage> loadRetainedMessages()
                throws PlccomMqttBrokerException {
            return new ArrayList<PlccomMqttBrokerRetainedMessage>(retained.values());
        }

        @Override
        public synchronized void saveRetainedMessage(PlccomMqttBrokerRetainedMessage message)
                throws PlccomMqttBrokerException {
            simulateStorageLatency();
            retained.put(message.getTopicName(), message);
            System.out.println("    [store] saved retained '" + message.getTopicName() + "' = '"
                    + new String(message.getPayload(), StandardCharsets.UTF_8) + "'");
        }

        @Override
        public synchronized void removeRetainedMessage(String topicName)
                throws PlccomMqttBrokerException {
            simulateStorageLatency();
            retained.remove(topicName);
        }

        @Override
        public synchronized List<PlccomMqttBrokerSessionState> loadSessions()
                throws PlccomMqttBrokerException {
            return new ArrayList<PlccomMqttBrokerSessionState>(sessions.values());
        }

        @Override
        public synchronized void saveSession(PlccomMqttBrokerSessionState session)
                throws PlccomMqttBrokerException {
            simulateStorageLatency();
            sessions.put(session.getClientIdentifier(), session);
        }

        @Override
        public synchronized void removeSession(String clientIdentifier)
                throws PlccomMqttBrokerException {
            simulateStorageLatency();
            sessions.remove(clientIdentifier);
        }

        synchronized int retainedCount() {
            return retained.size();
        }

        // Stands in for real storage I/O. Because this runs on a broker protocol
        // path, keeping it fast is a production requirement, not an optimization.
        private static void simulateStorageLatency() {
            try {
                Thread.sleep(2);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
