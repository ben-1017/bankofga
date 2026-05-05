package com.bankofgeorgia.notification.service;

import com.bankofgeorgia.notification.model.Notification;
import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TwilioSmsDispatcher {

    private static final Logger log = LoggerFactory.getLogger(TwilioSmsDispatcher.class);

    private final String accountSid;
    private final String authToken;
    private final String fromNumber;
    private final CustomerLookup customerLookup;

    public TwilioSmsDispatcher(@Value("${app.twilio.account-sid:}") String accountSid,
                               @Value("${app.twilio.auth-token:}") String authToken,
                               @Value("${app.twilio.from-number:}") String fromNumber,
                               CustomerLookup customerLookup) {
        this.accountSid = accountSid;
        this.authToken = authToken;
        this.fromNumber = fromNumber;
        this.customerLookup = customerLookup;
    }

    @PostConstruct
    void init() {
        if (isConfigured()) {
            Twilio.init(accountSid, authToken);
        }
    }

    public boolean send(Notification notification) {
        if (!isConfigured()) {
            log.info("Twilio not configured; skipping SMS for {}", notification.getId());
            return false;
        }
        CustomerLookup.Contact contact = customerLookup.resolve(notification.getCustomerId());
        if (contact == null || contact.phone() == null || contact.phone().isBlank()) {
            log.warn("no phone on file for customer {}", notification.getCustomerId());
            return false;
        }
        String body = buildBody(notification);
        if (body.isBlank()) {
            log.warn("empty SMS body for {}; skipping", notification.getId());
            return false;
        }

        try {
            Message message = Message.creator(
                    new PhoneNumber(contact.phone()),
                    new PhoneNumber(fromNumber),
                    body).create();
            log.info("Twilio sent SMS to {} (sid {})", contact.phone(), message.getSid());
            return true;
        } catch (ApiException ex) {
            log.warn("Twilio send failed: {}", ex.getMessage());
            return false;
        }
    }

    private boolean isConfigured() {
        return accountSid != null && !accountSid.isBlank()
                && authToken != null && !authToken.isBlank()
                && fromNumber != null && !fromNumber.isBlank();
    }

    private static String buildBody(Notification notification) {
        String subject = notification.getSubject();
        String body = notification.getBody();
        if (subject != null && !subject.isBlank() && body != null && !body.isBlank()) {
            return subject + ": " + body;
        }
        if (subject != null && !subject.isBlank()) return subject;
        if (body != null && !body.isBlank()) return body;
        return "";
    }
}
