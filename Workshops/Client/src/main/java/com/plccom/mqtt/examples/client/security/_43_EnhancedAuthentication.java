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

package com.plccom.mqtt.examples.client.security;

import java.nio.charset.StandardCharsets;

import com.plccom.console.PLCcomConsole;
import com.plccom.mqtt.client.PlccomMqttClient;
import com.plccom.mqtt.client.PlccomMqttClientEnhancedAuthContext;
import com.plccom.mqtt.client.PlccomMqttClientEnhancedAuthResponse;
import com.plccom.mqtt.client.PlccomMqttClientEnhancedAuthenticator;
import com.plccom.mqtt.client.PlccomMqttClientFactory;
import com.plccom.mqtt.client.PlccomMqttConnectResult;

/**
 * Workshop 43 - Enhanced Authentication (Security)
 *
 * WHAT THIS WORKSHOP SHOWS
 *   The CLIENT side of MQTT 5 Enhanced Authentication - an application-owned
 *   challenge/response mechanism beyond username/password. The client sends an
 *   Authentication Method and initial Authentication Data in CONNECT, and a
 *   PlccomMqttClientEnhancedAuthenticator answers any AUTH challenges the broker
 *   sends back before the connection is accepted. Broker Workshop 32 shows the
 *   matching broker side.
 *
 * WHEN YOU NEED THIS IN REAL APPLICATIONS
 *   Custom identity schemes - signed tokens, one-time passwords, SASL-style
 *   mechanisms - where a simple username/password is not enough and you want the
 *   check to stay inside your own code, interoperable with any MQTT 5 broker.
 *
 * BROKER FOR THIS WORKSHOP
 *   This client workshop needs a running MQTT 5 broker whose enhanced
 *   authenticator accepts the method and token used below. Broker Workshop 32
 *   installs exactly such an authenticator (method "plccom.token", token
 *   "token-123"). No broker is embedded here, so a client-only PLCcom license is
 *   enough. Override the endpoint with
 *   -Dplccom.mqtt.workshop.endpoint=mqtt://host:port .
 *
 * MQTT TERMS USED HERE
 *   - Authentication Method : an MQTT 5 string naming the mechanism (here
 *                             "plccom.token"). Client and broker must agree on it.
 *   - Authentication Data   : opaque bytes for the method (here the token). Sent
 *                             in CONNECT and in each AUTH packet.
 *   - AUTH exchange         : after CONNECT the broker may send AUTH challenges;
 *                             the client's authenticator answers each one until the
 *                             broker accepts or rejects the connection.
 *
 * HOW THIS WORKSHOP IS WRITTEN
 *   The whole flow is inline in start(); the client-side authenticator you
 *   implement (the actual extension point) is the small WorkshopTokenAuthenticator
 *   class at the bottom, mirroring the WorkshopTokenAuthenticator of Broker 32.
 *
 * EXPECTED RESULT
 *   The client connects using the enhanced-authentication method and token; the
 *   CONNECT result is Good when the broker's authenticator accepts them.
 */
public final class _43_EnhancedAuthentication {

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
    // A broker whose enhanced authenticator accepts the method/token below.
    // Start Broker Workshop 32 (Authentication and PKI) for a matching authenticator.
    private static final String BROKER_ENDPOINT = "mqtt://localhost:1883";
    // =========================================================================

    // The method and token this client presents. They must match what the broker's
    // enhanced authenticator expects - here the exact values from Broker Workshop 32.
    private static final String AUTH_METHOD = "plccom.token";
    private static final String AUTH_TOKEN  = "token-123";

    private _43_EnhancedAuthentication() {
    }

    /**
     * Opens the console and runs the workshop.
     *
     * @param args command-line arguments; not used by this workshop
     */
    public static void main(String[] args) {
        PLCcomConsole.open("PLCcom MQTT Workshop 43 - Enhanced Authentication", 1000);
        try {
            start();
        } catch (Exception ex) {
            System.out.println();
            System.out.println("  This workshop needs a broker at " + brokerEndpoint()
                    + " whose enhanced authenticator accepts method '" + AUTH_METHOD + "'.");
            System.out.println("  Start Broker Workshop 32 (Authentication and PKI) and run again.");
            System.out.println("  Technical detail: " + ex.getMessage());
        } finally {
            PLCcomConsole.waitForEnter();
            PLCcomConsole.close();
        }
    }

    /**
     * Connects with MQTT 5 enhanced authentication and reports the result.
     *
     * @throws Exception if the enhanced-authentication CONNECT is not accepted
     */
    public static void start() throws Exception {
        System.out.println("╔════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║  What this workshop demonstrates                                           ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  MQTT 5 Enhanced Authentication - the CLIENT side of a custom login.       ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  The client sends an Authentication Method and token in CONNECT; your      ║");
        System.out.println("║  authenticator answers any AUTH challenges until the broker accepts.       ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  Method and token here match Broker Workshop 32 (method 'plccom.token').   ║");
        System.out.println("║                                                                            ║");
        System.out.println("║  Expected result: CONNECT is Good when the broker accepts the token.       ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════════════╝");
        System.out.println();

        System.out.println("  Broker endpoint : " + brokerEndpoint());
        System.out.println();

        // -- Step 1: the client-side authenticator (the extension point) ------
        // This object is what YOU implement. The broker may send AUTH challenges
        // during CONNECT; this authenticator answers each challenge. Its class is
        // WorkshopTokenAuthenticator at the bottom of this file.
        PlccomMqttClientEnhancedAuthenticator authenticator = new WorkshopTokenAuthenticator();

        // -- Step 2: build the client with enhanced authentication ------------
        // enhancedAuthentication(method, initialData, authenticator) sends the
        // method and the initial token in CONNECT and wires the authenticator to
        // answer any broker challenges. This is the whole client-side setup.
        PlccomMqttClient client = PlccomMqttClientFactory.builder(LICENSE_USER, LICENSE_SERIAL)
                .identifier("plccom-workshop-43")
                .endpoint(brokerEndpoint())
                .mqtt5()
                .enhancedAuthentication(AUTH_METHOD, AUTH_TOKEN.getBytes(StandardCharsets.UTF_8), authenticator)
                .keepAliveSeconds(30)
                .build();

        try {
            // -- Step 3: connect - the enhanced-auth handshake runs inside connect()
            // The method + token go out in CONNECT; if the broker challenges, the
            // authenticator above answers, and only then is the result returned.
            System.out.println("  Connecting with enhanced authentication (method '" + AUTH_METHOD + "') ...");
            PlccomMqttConnectResult connectResult = client.connect();
            if (!connectResult.isGood()) {
                throw new IllegalStateException("Enhanced-auth CONNECT failed: "
                        + connectResult.getQuality() + " - " + connectResult.getMessage()
                        + "  (the broker must accept method '" + AUTH_METHOD + "' with this token; see Broker Workshop 32)");
            }
            System.out.println("  Connected. Negotiated protocol: "
                    + connectResult.getNegotiatedProtocolVersion());
            System.out.println();
            System.out.println("  Success: the broker accepted the enhanced-authentication token.");
            System.out.println("  From here the client publishes and subscribes exactly like Workshop 11.");
        } finally {
            client.close();
        }
    }

    /**
     * The client-side authenticator you implement. It answers broker AUTH
     * challenges. This single-step token method simply resends the same method and
     * token; a real mechanism would compute a response from the broker's challenge
     * data (context.getAuthenticationData()).
     */
    private static final class WorkshopTokenAuthenticator implements PlccomMqttClientEnhancedAuthenticator {
        @Override
        public PlccomMqttClientEnhancedAuthResponse authenticate(PlccomMqttClientEnhancedAuthContext context) {
            System.out.println("    [enhanced-auth] answering broker challenge, step " + context.getStep());
            return PlccomMqttClientEnhancedAuthResponse.continueWith(
                    context.getAuthenticationMethod(),
                    AUTH_TOKEN.getBytes(StandardCharsets.UTF_8));
        }
    }

    // Small private helper, kept inside this same workshop class on purpose: it
    // only resolves the broker endpoint (constant, or an optional system-property
    // override for automation). It carries no MQTT learning logic.
    private static String brokerEndpoint() {
        return System.getProperty("plccom.mqtt.workshop.endpoint", BROKER_ENDPOINT);
    }
}
