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

package com.plccom.mqtt.tools.broker;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import com.plccom.mqtt.broker.PlccomMqttBrokerAccessDecision;
import com.plccom.mqtt.broker.PlccomMqttBrokerAccessDeniedReason;
import com.plccom.mqtt.broker.PlccomMqttBrokerAccessValidator;
import com.plccom.mqtt.broker.PlccomMqttBrokerConnectContext;
import com.plccom.mqtt.broker.PlccomMqttBrokerPublishContext;
import com.plccom.mqtt.broker.PlccomMqttBrokerSubscriptionContext;

/**
 * Sample access validator of the broker tool.
 *
 * <p>The validator is an immutable snapshot of the access rules visible in the
 * tool UI when the broker starts. The rules follow the documented product
 * behavior:</p>
 *
 * <ul>
 * <li>{@code requireUserName}: CONNECT without a user name is denied with MQTT
 * reason code 0x86 (Bad User Name or Password). When the configured user list
 * contains entries, user name and password must match exactly one
 * {@code user=password} entry; otherwise a present user name is sufficient.</li>
 * <li>{@code requireClientCertificate}: CONNECT without a TLS client
 * certificate is denied with MQTT reason code 0x87 (Not authorized). This is a
 * business-level access rule and does not change the TLS handshake itself.</li>
 * <li>{@code blockedTopicPrefix}: PUBLISH and SUBSCRIBE on topics starting
 * with the prefix are denied with MQTT reason code 0x87 while the connection
 * stays open. QoS 0 publishes are dropped by the broker without an
 * acknowledgement, exactly as the protocol allows.</li>
 * </ul>
 *
 * <p>All log lines produced by this validator are English by design so broker
 * diagnostics stay consistent across UI languages.</p>
 */
final class BrokerToolAccessValidator implements PlccomMqttBrokerAccessValidator {

    /** Receives the validator's English log lines for the tool log panel. */
    interface LogSink {
        void info(String message);

        void warning(String message);
    }

    /** No-op sink used when no log callback is supplied, so the validator never NPEs. */
    private static final LogSink SILENT = new LogSink() {
        @Override
        public void info(String message) {
        }

        @Override
        public void warning(String message) {
        }
    };

    private final boolean requireUserName;          // deny CONNECT without a user name
    private final Map<String, byte[]> accessUsers;  // optional user=password allow-list (frozen snapshot)
    private final boolean requireClientCertificate; // deny CONNECT without a TLS client certificate
    private final String blockedTopicPrefix;        // deny publish/subscribe on this prefix (null = off)
    private final LogSink log;                       // where the English access-decision lines go

    BrokerToolAccessValidator(boolean requireUserName, Map<String, byte[]> accessUsers,
            boolean requireClientCertificate, String blockedTopicPrefix, LogSink log) {
        this.requireUserName = requireUserName;
        // Defensive copy so later edits in the UI cannot change a running broker's
        // rules: the validator is a frozen snapshot taken when the broker started.
        this.accessUsers = accessUsers == null
                ? new LinkedHashMap<String, byte[]>()
                : new LinkedHashMap<String, byte[]>(accessUsers);
        this.requireClientCertificate = requireClientCertificate;
        this.blockedTopicPrefix = trimOrNull(blockedTopicPrefix);
        this.log = log == null ? SILENT : log; // fall back to the silent sink
    }

    /**
     * Access check for MQTT CONNECT. Logs the attempt, then applies the two connect
     * rules in order — user name/password and client certificate — denying with the
     * matching MQTT reason code, or allowing and returning the authenticated principal.
     */
    @Override
    public PlccomMqttBrokerAccessDecision validateConnect(PlccomMqttBrokerConnectContext context) {
        boolean certificatePresent = context.hasClientCertificate();
        // Always log the attempt (never the password itself), so the operator sees
        // every CONNECT and why it was allowed or denied.
        log.info("ACCESS CONNECT clientId=" + context.getClientId()
                + ", user=" + displayUser(context.getUserName())
                + ", passwordPresent=" + context.hasPassword()
                + ", certPresent=" + certificatePresent);
        // Rule 1: a user name (and, if a user list is configured, a matching password).
        if (requireUserName) {
            String userName = trimOrNull(context.getUserName());
            if (userName == null) {
                // No user name at all -> 0x86 Bad User Name or Password.
                log.warning("ACCESS DENY CONNECT clientId=" + context.getClientId()
                        + ", reason=user name required");
                return PlccomMqttBrokerAccessDecision.deny(
                        PlccomMqttBrokerAccessDeniedReason.BAD_USER_NAME_OR_PASSWORD,
                        "A user name is required");
            }
            // With a non-empty user list, the user/password must match one entry.
            // An empty list means "any user name is enough" (no password check).
            if (!accessUsers.isEmpty() && !isConfiguredUser(userName, context)) {
                log.warning("ACCESS DENY CONNECT clientId=" + context.getClientId()
                        + ", user=" + userName + ", reason=bad user name or password");
                return PlccomMqttBrokerAccessDecision.deny(
                        PlccomMqttBrokerAccessDeniedReason.BAD_USER_NAME_OR_PASSWORD,
                        "The supplied user name or password is not configured");
            }
        }
        // Rule 2: a TLS client certificate must be present. This is a business rule,
        // not TLS config -> 0x87 Not authorized (see the class Javadoc).
        if (requireClientCertificate && !certificatePresent) {
            log.warning("ACCESS DENY CONNECT clientId=" + context.getClientId()
                    + ", reason=client certificate required");
            return PlccomMqttBrokerAccessDecision.deny(
                    PlccomMqttBrokerAccessDeniedReason.NOT_AUTHORIZED,
                    "A TLS client certificate is required");
        }
        // All active rules passed: allow and record who connected.
        return PlccomMqttBrokerAccessDecision.allow(authenticatedPrincipal(context));
    }

    /** Denies PUBLISH on a blocked topic prefix (0x87); otherwise allows it. */
    @Override
    public PlccomMqttBrokerAccessDecision validatePublish(PlccomMqttBrokerPublishContext context) {
        if (isBlocked(context.getTopicName())) {
            log.warning("ACCESS DENY PUBLISH clientId=" + context.getClientId()
                    + ", topic=" + context.getTopicName());
            return PlccomMqttBrokerAccessDecision.deny(
                    PlccomMqttBrokerAccessDeniedReason.NOT_AUTHORIZED,
                    "The topic is blocked by the configured topic prefix");
        }
        return PlccomMqttBrokerAccessDecision.allow(context.getPrincipal());
    }

    /** Denies SUBSCRIBE on a blocked topic prefix (0x87); otherwise allows it. */
    @Override
    public PlccomMqttBrokerAccessDecision validateSubscribe(PlccomMqttBrokerSubscriptionContext context) {
        if (isBlocked(context.getTopicFilter())) {
            log.warning("ACCESS DENY SUBSCRIBE clientId=" + context.getClientId()
                    + ", topicFilter=" + context.getTopicFilter());
            return PlccomMqttBrokerAccessDecision.deny(
                    PlccomMqttBrokerAccessDeniedReason.NOT_AUTHORIZED,
                    "The topic filter is blocked by the configured topic prefix");
        }
        return PlccomMqttBrokerAccessDecision.allow(context.getPrincipal());
    }

    /** Unsubscribe is always allowed; the tool never restricts leaving a topic. */
    @Override
    public PlccomMqttBrokerAccessDecision validateUnsubscribe(PlccomMqttBrokerSubscriptionContext context) {
        return PlccomMqttBrokerAccessDecision.allow(context.getPrincipal());
    }

    /**
     * Parses the tool's multi-line user list.
     *
     * <p>Each non-empty line must use {@code user=password}. Lines starting
     * with {@code #} are comments. Duplicate user names are rejected.</p>
     *
     * @param text raw text of the users text area, may be {@code null}
     * @return parsed users in declaration order, never {@code null}
     * @throws IllegalArgumentException if a line is malformed
     */
    static Map<String, byte[]> parseAccessUsers(String text) {
        Map<String, byte[]> users = new LinkedHashMap<String, byte[]>();
        if (text == null || text.trim().length() == 0) {
            return users;
        }
        String normalized = text.replace("\r\n", "\n").replace('\r', '\n');
        String[] lines = normalized.split("\n");
        for (int index = 0; index < lines.length; index++) {
            String line = lines[index].trim();
            if (line.length() == 0 || line.startsWith("#")) {
                continue;
            }
            int separator = line.indexOf('=');
            if (separator <= 0) {
                throw new IllegalArgumentException("Access user line " + (index + 1)
                        + " must use user=password.");
            }
            String userName = line.substring(0, separator).trim();
            String password = line.substring(separator + 1);
            if (userName.length() == 0 || password.length() == 0) {
                throw new IllegalArgumentException("Access user line " + (index + 1)
                        + " must contain a user name and a password.");
            }
            if (users.containsKey(userName)) {
                throw new IllegalArgumentException("Access user '" + userName
                        + "' is configured more than once.");
            }
            users.put(userName, password.getBytes(StandardCharsets.UTF_8));
        }
        return users;
    }

    /**
     * True when the supplied password matches the configured one for the user. The
     * comparison is constant-time to avoid leaking the password through timing, and
     * the supplied password bytes are wiped afterwards.
     */
    private boolean isConfiguredUser(String userName, PlccomMqttBrokerConnectContext context) {
        byte[] expectedPassword = accessUsers.get(userName);
        if (expectedPassword == null) {
            return false; // unknown user name
        }
        byte[] suppliedPassword = context.getPassword();
        if (suppliedPassword == null) {
            return false; // known user but no password provided
        }
        try {
            // Constant-time comparison so the match does not leak the password
            // through timing differences. Copy this pattern into real access
            // validators instead of Arrays.equals, which returns early.
            return MessageDigest.isEqual(expectedPassword, suppliedPassword);
        } finally {
            Arrays.fill(suppliedPassword, (byte) 0);
        }
    }

    /** True when a topic starts with the configured blocked prefix (and blocking is on). */
    private boolean isBlocked(String topic) {
        return blockedTopicPrefix != null && topic != null && topic.startsWith(blockedTopicPrefix);
    }

    /**
     * Builds a readable principal string from the user name and/or the certificate
     * subject, so the allow decision records who was authenticated.
     */
    private static String authenticatedPrincipal(PlccomMqttBrokerConnectContext context) {
        String userName = trimOrNull(context.getUserName());
        X509Certificate certificate = context.getClientCertificate();
        String certificateSubject = certificate == null
                ? null
                : certificate.getSubjectX500Principal().getName();
        if (userName != null && certificateSubject != null) {
            return userName + " [" + certificateSubject + "]";
        }
        if (userName != null) {
            return userName;
        }
        return certificateSubject;
    }

    /** The user name for logging, or "none" when it is absent. */
    private static String displayUser(String userName) {
        String trimmed = trimOrNull(userName);
        return trimmed == null ? "none" : trimmed;
    }

    /** The trimmed value, or {@code null} when it is empty. */
    private static String trimOrNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.length() == 0 ? null : trimmed;
    }
}
