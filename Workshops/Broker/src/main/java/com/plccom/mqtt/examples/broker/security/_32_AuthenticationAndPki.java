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
import com.plccom.mqtt.broker.PlccomMqttBrokerEnhancedAuthContext;
import com.plccom.mqtt.broker.PlccomMqttBrokerEnhancedAuthResult;
import com.plccom.mqtt.broker.PlccomMqttBrokerEnhancedAuthenticator;
import com.plccom.mqtt.broker.PlccomMqttBrokerFactory;
import com.plccom.mqtt.broker.PlccomMqttBrokerProtocolVersion;
import com.plccom.mqtt.broker.PlccomMqttBrokerPublishContext;
import com.plccom.mqtt.broker.PlccomMqttBrokerSubscriptionContext;

/**
 * Workshop 32 - Authentication and PKI (Security)
 *
 * WHAT THIS WORKSHOP SHOWS
 *   The ways a client proves who it is to the broker, and where PKI fits in.
 *   Username/password authentication is decided in the access validator's CONNECT
 *   check. MQTT 5 Enhanced Authentication is an optional challenge-style method
 *   your code owns. Certificate authentication (mTLS) is configured on the TLS
 *   listener and validated against a PKI store - the transport side is shown in
 *   Broker Workshop 13.
 *
 * WHEN YOU NEED THIS IN REAL APPLICATIONS
 *   Any broker that must know which client is connecting: simple deployments use
 *   username/password, higher-assurance ones use client certificates (mTLS), and
 *   custom identity systems use enhanced authentication tokens.
 *
 * MATCHING CLIENT
 *   Client Workshop 41 (Secure Connection) shows the client side of TLS and mTLS;
 *   Client Workshop 43 (Enhanced Authentication) shows the client side of the
 *   enhanced-authentication method installed here.
 *
 * MQTT TERMS USED HERE
 *   - Username/password      : credentials in CONNECT, checked by the validator.
 *   - Enhanced Authentication: MQTT 5 AUTH exchange with an application method.
 *   - Certificate / mTLS     : the TLS listener requires a client certificate.
 *   - PKI store              : folders the broker uses to validate certificates -
 *                              trusted, issuers, rejected and its own identity.
 *
 * EXPECTED RESULT
 *   Valid credentials and a valid token are accepted; invalid ones are rejected
 *   with a clear reason. No password or secret is ever printed.
 */
public final class _32_AuthenticationAndPki {

    // === YOUR PLCcom LICENSE =================================================
    // Replace both values with the user name and serial from your PLCcom license
    // e-mail. A broker (or broker+client) license is required.
    private static final String LICENSE_USER   = "<Enter your UserName here>";
    private static final String LICENSE_SERIAL = "<Enter your Serial here>";
    // =========================================================================

    private static final String AUTH_METHOD = "plccom.token";

    private _32_AuthenticationAndPki() {
    }

    /**
     * Opens the console and runs the workshop.
     *
     * @param args command-line arguments; not used by this workshop
     */
    public static void main(String[] args) {
        PLCcomConsole.open("PLCcom MQTT Broker Workshop 32 - Authentication and PKI", 1000);
        try {
            start();
        } catch (Exception ex) {
            System.out.println();
            System.out.println("  The authentication scenario failed.");
            System.out.println("  Technical detail: " + ex.getMessage());
        } finally {
            PLCcomConsole.waitForEnter();
            PLCcomConsole.close();
        }
    }

    /**
     * Installs username/password and enhanced authentication and shows decisions.
     *
     * @throws Exception if the broker cannot be started with the authenticators
     */
    public static void start() throws Exception {
        System.out.println("╔════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║  What this workshop demonstrates                                           ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  How clients authenticate to the broker, and where PKI fits in.            ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  Username/password: checked in the access validator's CONNECT decision.    ║");
        System.out.println("║  Enhanced Auth: an optional MQTT 5 challenge method your own code owns.    ║");
        System.out.println("║  Certificate / mTLS: a TLS listener can require a client certificate,      ║");
        System.out.println("║  validated against a PKI store (trusted / issuers / rejected / own) - see  ║");
        System.out.println("║  Broker Workshop 13 for the TLS server side.                               ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  Passwords and secrets are never logged.                                   ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  Expected result: valid credentials and token are accepted, invalid ones   ║");
        System.out.println("║  are rejected with a clear reason.                                         ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════════════╝");
        System.out.println();

        // -- Step 1: start the broker with both authentication hooks ----------
        // accessValidator(...) handles username/password (and topic policy);
        // enhancedAuthenticator(...) adds the optional MQTT 5 token method. A real
        // deployment would usually pick one primary method; both are shown here so
        // the API is visible in one place.
        WorkshopPasswordValidator passwords = new WorkshopPasswordValidator();
        WorkshopTokenAuthenticator tokens = new WorkshopTokenAuthenticator();
        PlccomMqttBroker broker = PlccomMqttBrokerFactory.builder(LICENSE_USER, LICENSE_SERIAL)
                .tcpPort(1883) // 1883 is the standard MQTT-over-TCP port
                .accessValidator(passwords)
                .enhancedAuthenticator(tokens)
                .start();
        try {
            System.out.println("  Broker running on mqtt://127.0.0.1:" + broker.getBoundPort());
            System.out.println();

            // -- Step 2: username/password decisions --------------------------
            System.out.println("  Username/password authentication:");
            PlccomMqttBrokerAccessDecision good = passwords.validateConnect(
                    connectContext("panel-1", "operator", "secret"));
            System.out.println("    operator/secret : allowed=" + good.isAllowed()
                    + " principal=" + good.getPrincipal());
            PlccomMqttBrokerAccessDecision bad = passwords.validateConnect(
                    connectContext("panel-2", "operator", "wrong"));
            System.out.println("    operator/wrong  : allowed=" + bad.isAllowed()
                    + " reason=" + bad.getDeniedReason());
            PlccomMqttBrokerAccessDecision unknown = passwords.validateConnect(
                    connectContext("panel-3", "intruder", "x"));
            System.out.println("    intruder/x      : allowed=" + unknown.isAllowed()
                    + " reason=" + unknown.getDeniedReason());

            // -- Step 3: enhanced authentication decisions --------------------
            System.out.println();
            System.out.println("  MQTT 5 enhanced authentication (method '" + AUTH_METHOD + "'):");
            PlccomMqttBrokerEnhancedAuthResult tokenOk = tokens.onConnect(
                    authContext(AUTH_METHOD, "token-123"));
            System.out.println("    correct token   : success=" + tokenOk.isSuccess());
            PlccomMqttBrokerEnhancedAuthResult tokenBad = tokens.onConnect(
                    authContext(AUTH_METHOD, "nope"));
            System.out.println("    wrong token     : success=" + tokenBad.isSuccess()
                    + " reason=" + tokenBad.getDeniedReason());
            PlccomMqttBrokerEnhancedAuthResult wrongMethod = tokens.onConnect(
                    authContext("some.other.method", "token-123"));
            System.out.println("    wrong method    : badMethod=" + wrongMethod.isBadAuthenticationMethod());

            // -- Step 4: certificate / mTLS note ------------------------------
            // Certificate authentication is configured on the TLS listener, not
            // here: the listener requests a client certificate and validates it
            // against the PKI store (trusted / issuers / rejected / own folders).
            // See Broker Workshop 13 for starting TLS listeners and providing the
            // server certificate; the same TLS options carry the client-certificate
            // mode and PKI validation for mTLS.
            System.out.println();
            System.out.println("  Certificate / mTLS: configured on the TLS listener (see Broker Workshop 13).");
            System.out.println();
            System.out.println("  Success: username/password and enhanced authentication decisions are shown.");
        } finally {
            broker.close();
            System.out.println("  Broker stopped.");
        }
    }

    private static PlccomMqttBrokerConnectContext connectContext(String clientId, String userName,
            String password) {
        byte[] passwordBytes = password == null ? null : password.getBytes(StandardCharsets.UTF_8);
        return new PlccomMqttBrokerConnectContext(PlccomMqttBrokerProtocolVersion.MQTT_5_0,
                new InetSocketAddress("127.0.0.1", 1883), clientId, clientId, true, 30,
                userName, passwordBytes, passwordBytes != null, null, 0, false);
    }

    private static PlccomMqttBrokerEnhancedAuthContext authContext(String method, String data) {
        return new PlccomMqttBrokerEnhancedAuthContext(PlccomMqttBrokerProtocolVersion.MQTT_5_0,
                new InetSocketAddress("127.0.0.1", 1883), "auth-client", "auth-client",
                "operator", null, method, data.getBytes(StandardCharsets.UTF_8), 0);
    }

    /**
     * A tiny username/password policy. It authenticates known accounts and denies
     * everything else. Passwords are compared and then cleared, never logged.
     */
    private static final class WorkshopPasswordValidator implements PlccomMqttBrokerAccessValidator {

        @Override
        public PlccomMqttBrokerAccessDecision validateConnect(PlccomMqttBrokerConnectContext context) {
            byte[] password = context.getPassword();
            boolean ok = "operator".equals(context.getUserName())
                    && Arrays.equals("secret".getBytes(StandardCharsets.UTF_8), password);
            if (password != null) {
                Arrays.fill(password, (byte) 0);
            }
            if (ok) {
                return PlccomMqttBrokerAccessDecision.allow("role=operator");
            }
            return PlccomMqttBrokerAccessDecision.deny(
                    PlccomMqttBrokerAccessDeniedReason.BAD_USER_NAME_OR_PASSWORD,
                    "Unknown user or wrong password.");
        }

        @Override
        public PlccomMqttBrokerAccessDecision validatePublish(PlccomMqttBrokerPublishContext context) {
            return PlccomMqttBrokerAccessDecision.allow(context.getPrincipal());
        }

        @Override
        public PlccomMqttBrokerAccessDecision validateSubscribe(PlccomMqttBrokerSubscriptionContext context) {
            return PlccomMqttBrokerAccessDecision.allow(context.getPrincipal());
        }

        @Override
        public PlccomMqttBrokerAccessDecision validateUnsubscribe(PlccomMqttBrokerSubscriptionContext context) {
            return PlccomMqttBrokerAccessDecision.allow(context.getPrincipal());
        }
    }

    /**
     * A tiny MQTT 5 enhanced authenticator that accepts one shared token. Real
     * implementations would verify a signed token or call an identity service.
     */
    private static final class WorkshopTokenAuthenticator implements PlccomMqttBrokerEnhancedAuthenticator {
        private static final byte[] EXPECTED_TOKEN = "token-123".getBytes(StandardCharsets.UTF_8);

        @Override
        public PlccomMqttBrokerEnhancedAuthResult onConnect(PlccomMqttBrokerEnhancedAuthContext context) {
            if (!AUTH_METHOD.equals(context.getAuthenticationMethod())) {
                return PlccomMqttBrokerEnhancedAuthResult.badAuthenticationMethod();
            }
            byte[] supplied = context.getAuthenticationData();
            boolean ok = Arrays.equals(EXPECTED_TOKEN, supplied);
            if (supplied != null) {
                Arrays.fill(supplied, (byte) 0);
            }
            if (ok) {
                return PlccomMqttBrokerEnhancedAuthResult.success();
            }
            return PlccomMqttBrokerEnhancedAuthResult.deny(
                    PlccomMqttBrokerAccessDeniedReason.BAD_USER_NAME_OR_PASSWORD);
        }

        @Override
        public PlccomMqttBrokerEnhancedAuthResult onAuth(PlccomMqttBrokerEnhancedAuthContext context) {
            // Multi-step methods continue here; this single-step method reuses onConnect.
            return onConnect(context);
        }
    }
}
