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

package com.plccom.mqtt.examples.broker.security;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import com.plccom.console.PLCcomConsole;
import com.plccom.mqtt.broker.PlccomMqttBroker;
import com.plccom.mqtt.broker.PlccomMqttBrokerAccessDecision;
import com.plccom.mqtt.broker.PlccomMqttBrokerAccessDeniedReason;
import com.plccom.mqtt.broker.PlccomMqttBrokerAccessValidator;
import com.plccom.mqtt.broker.PlccomMqttBrokerConnectContext;
import com.plccom.mqtt.broker.PlccomMqttBrokerFactory;
import com.plccom.mqtt.broker.PlccomMqttBrokerProtocolVersion;
import com.plccom.mqtt.broker.PlccomMqttBrokerPublishContext;
import com.plccom.mqtt.broker.PlccomMqttBrokerQualityOfService;
import com.plccom.mqtt.broker.PlccomMqttBrokerSubscriptionContext;
import com.plccom.mqtt.diagnostics.PlccomMqttDiagnosticLevel;

/**
 * Workshop 31 - Access Control (Security)
 *
 * WHAT THIS WORKSHOP SHOWS
 *   How the broker decides who may CONNECT and which topics may be published or
 *   subscribed. This is the job of an application-owned access validator. Without
 *   one the broker is fully open (allow-all); with one, your code authenticates
 *   clients and authorizes topic operations. The recommended posture is
 *   deny-by-default: allow only what you explicitly permit.
 *
 * WHEN YOU NEED THIS IN REAL APPLICATIONS
 *   Every broker exposed beyond a fully trusted network. The validator is where
 *   you integrate your user directory and topic-permission model without the MQTT
 *   library imposing one fixed policy.
 *
 * MATCHING CLIENT
 *   Pair with Client Workshop 11 (basic) or 41 (secure) to try it live: connect
 *   with the wrong password and watch the denial, then with the right one.
 *
 * MQTT TERMS USED HERE
 *   - Access validator     : the callback the broker asks for CONNECT / PUBLISH /
 *                            SUBSCRIBE / UNSUBSCRIBE decisions.
 *   - Deny-by-default      : reject anything not explicitly allowed.
 *   - Topic authorization  : allow or deny based on the topic (here a "plant/"
 *                            prefix and wildcards).
 *   - Denied reason        : a structured reason such as BadUserNameOrPassword or
 *                            NotAuthorized returned to the client.
 *   - Access-denied event  : a denied CONNECT is also reported as a WARNING
 *                            diagnostic (BROKER_ACCESS_CONNECT_DENIED) that never
 *                            contains the password.
 *
 * EXPECTED RESULT
 *   The validator allows the operator with the correct password on plant topics,
 *   and denies a wrong password and an unauthorized topic with a clear reason.
 */
public final class _31_AccessControl {

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

    private _31_AccessControl() {
    }

    /**
     * Opens the console and runs the workshop.
     *
     * @param args command-line arguments; not used by this workshop
     */
    public static void main(String[] args) {
        PLCcomConsole.open("PLCcom MQTT Broker Workshop 31 - Access Control", 1000);
        try {
            start();
        } catch (Exception ex) {
            System.out.println();
            System.out.println("  The access-control scenario failed.");
            System.out.println("  Technical detail: " + ex.getMessage());
        } finally {
            PLCcomConsole.waitForEnter();
            PLCcomConsole.close();
        }
    }

    /**
     * Installs an access validator and shows its allow/deny decisions.
     *
     * @throws Exception if the broker cannot be started with the validator
     */
    public static void start() throws Exception {
        System.out.println("╔════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║  What this workshop demonstrates                                           ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  Access control: your code decides who may CONNECT, PUBLISH, SUBSCRIBE and ║");
        System.out.println("║  UNSUBSCRIBE, via an application-owned access validator.                   ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  With no validator the broker is open (allow-all). A validator lets you    ║");
        System.out.println("║  authenticate users and authorize topics - prefer deny-by-default.         ║");
        System.out.println("║  Denied CONNECTs are also reported as a WARNING diagnostic                 ║");
        System.out.println("║  (BROKER_ACCESS_CONNECT_DENIED) - never containing the password.           ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  Pair with Client Workshop 11 / 41 to try it live.                         ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  Expected result: allowed operations pass; wrong password and unauthorized ║");
        System.out.println("║  topics are denied with a clear reason.                                    ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════════════╝");
        System.out.println();

        // -- Step 1: start the broker with the validator and diagnostics ------
        // accessValidator(...) installs the policy below. The diagnostic listener
        // prints denials the broker reports for real network clients; the level
        // WARNING keeps the log quiet but still shows access denials.
        WorkshopAccessValidator validator = new WorkshopAccessValidator();
        PlccomMqttBroker broker = PlccomMqttBrokerFactory.builder(LICENSE_USER, LICENSE_SERIAL)
                .tcpPort(1883) // 1883 is the standard MQTT-over-TCP port
                .accessValidator(validator)
                .diagnosticLevel(PlccomMqttDiagnosticLevel.WARNING)
                .onDiagnostic(event -> System.out.println("    [diagnostic] " + event.getSeverity()
                        + " " + event.getCode() + " : " + event.getMessage()))
                .start();
        try {
            System.out.println("  Broker running on mqtt://127.0.0.1:" + broker.getBoundPort()
                    + " with an application access validator.");
            System.out.println("  Connect a client with the wrong password to see a logged denial.");
            System.out.println();

            // -- Step 2: show the decisions the broker will make ---------------
            // The broker calls the validator with a context for every real client
            // operation. To make the policy visible and deterministic here, we ask
            // the validator the same questions with representative contexts - these
            // are exactly the contexts the broker builds for a real client.
            System.out.println("  Decisions the validator returns:");

            PlccomMqttBrokerAccessDecision connectOk = validator.validateConnect(
                    connectContext("operator-panel", "operator", "secret"));
            System.out.println("    CONNECT operator/secret     : allowed=" + connectOk.isAllowed()
                    + " principal=" + connectOk.getPrincipal());

            PlccomMqttBrokerAccessDecision connectBad = validator.validateConnect(
                    connectContext("operator-panel", "operator", "wrong-password"));
            System.out.println("    CONNECT operator/wrong      : allowed=" + connectBad.isAllowed()
                    + " reason=" + connectBad.getDeniedReason());

            PlccomMqttBrokerAccessDecision publishOk = validator.validatePublish(
                    publishContext("operator-panel", connectOk.getPrincipal(), "plant/line1/command"));
            System.out.println("    PUBLISH plant/line1/command : allowed=" + publishOk.isAllowed());

            PlccomMqttBrokerAccessDecision publishBad = validator.validatePublish(
                    publishContext("operator-panel", connectOk.getPrincipal(), "billing/invoices"));
            System.out.println("    PUBLISH billing/invoices    : allowed=" + publishBad.isAllowed()
                    + " reason=" + publishBad.getDeniedReason());

            PlccomMqttBrokerAccessDecision subscribeOk = validator.validateSubscribe(
                    subscribeContext("operator-panel", connectOk.getPrincipal(), "plant/line1/#"));
            System.out.println("    SUBSCRIBE plant/line1/#     : allowed=" + subscribeOk.isAllowed());

            System.out.println();
            System.out.println("  Success: the validator enforces authentication and topic authorization.");
        } finally {
            broker.close();
            System.out.println("  Broker stopped.");
        }
    }

    // ---------------------------------------------------------------------------
    // Helpers that build the same context objects the broker passes to the
    // validator for a real client operation. Kept in this class so the demo is
    // deterministic; a live client produces the identical decisions.
    // ---------------------------------------------------------------------------

    private static PlccomMqttBrokerConnectContext connectContext(String clientId, String userName,
            String password) {
        byte[] passwordBytes = password == null ? null : password.getBytes(StandardCharsets.UTF_8);
        return new PlccomMqttBrokerConnectContext(PlccomMqttBrokerProtocolVersion.MQTT_5_0,
                new InetSocketAddress("127.0.0.1", 1883), clientId, clientId, true, 30,
                userName, passwordBytes, passwordBytes != null, null, 0, false);
    }

    private static PlccomMqttBrokerPublishContext publishContext(String clientId, Object principal,
            String topicName) {
        return new PlccomMqttBrokerPublishContext(PlccomMqttBrokerProtocolVersion.MQTT_5_0,
                new InetSocketAddress("127.0.0.1", 1883), clientId, principal, topicName,
                PlccomMqttBrokerQualityOfService.AT_LEAST_ONCE, false,
                "value".getBytes(StandardCharsets.UTF_8));
    }

    private static PlccomMqttBrokerSubscriptionContext subscribeContext(String clientId, Object principal,
            String topicFilter) {
        return new PlccomMqttBrokerSubscriptionContext(PlccomMqttBrokerProtocolVersion.MQTT_5_0,
                new InetSocketAddress("127.0.0.1", 1883), clientId, principal, 1, topicFilter,
                PlccomMqttBrokerQualityOfService.AT_LEAST_ONCE);
    }

    /**
     * A small deny-by-default access policy. It authenticates one operator and
     * authorizes only the "plant/" topic branch. Everything else is denied.
     */
    private static final class WorkshopAccessValidator implements PlccomMqttBrokerAccessValidator {

        @Override
        public PlccomMqttBrokerAccessDecision validateConnect(PlccomMqttBrokerConnectContext context) {
            byte[] password = context.getPassword();
            boolean validOperator = "operator".equals(context.getUserName())
                    && Arrays.equals("secret".getBytes(StandardCharsets.UTF_8), password);
            // Clear the password bytes as soon as the check is done.
            if (password != null) {
                Arrays.fill(password, (byte) 0);
            }
            if (validOperator) {
                return PlccomMqttBrokerAccessDecision.allow("role=operator");
            }
            return PlccomMqttBrokerAccessDecision.deny(
                    PlccomMqttBrokerAccessDeniedReason.BAD_USER_NAME_OR_PASSWORD,
                    "Only the operator account is accepted in this workshop.");
        }

        @Override
        public PlccomMqttBrokerAccessDecision validatePublish(PlccomMqttBrokerPublishContext context) {
            if ("role=operator".equals(context.getPrincipal())
                    && context.getTopicName().startsWith("plant/")) {
                return PlccomMqttBrokerAccessDecision.allow(context.getPrincipal());
            }
            return PlccomMqttBrokerAccessDecision.deny(
                    PlccomMqttBrokerAccessDeniedReason.NOT_AUTHORIZED,
                    "Only operators may publish to plant/ topics.");
        }

        @Override
        public PlccomMqttBrokerAccessDecision validateSubscribe(PlccomMqttBrokerSubscriptionContext context) {
            // Topic filters may use wildcards; we allow the whole plant/ branch.
            if (context.getTopicFilter().startsWith("plant/")) {
                return PlccomMqttBrokerAccessDecision.allow(context.getPrincipal());
            }
            return PlccomMqttBrokerAccessDecision.deny(
                    PlccomMqttBrokerAccessDeniedReason.NOT_AUTHORIZED,
                    "This workshop only allows plant/ subscriptions.");
        }

        @Override
        public PlccomMqttBrokerAccessDecision validateUnsubscribe(PlccomMqttBrokerSubscriptionContext context) {
            // Unsubscribing is harmless here, so it is always allowed.
            return PlccomMqttBrokerAccessDecision.allow(context.getPrincipal());
        }
    }
}
