package com.bankofgeorgia.notification.service;

import com.bankofgeorgia.notification.model.Notification;
import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
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
    private boolean initialized;

    public TwilioSmsDispatcher(@Value("${app.twilio.account-sid:}") String accountSid,
                               @Value("${app.twilio.auth-token:}") String authToken,
                               @Value("${app.twilio.from-number:}") String fromNumber,
                               CustomerLookup customerLookup) {
        this.accountSid = accountSid;
        this.authToken = authToken;
        this.fromNumber = fromNumber;
        this.customerLookup = customerLookup;
    }

    public boolean send(Notification notification) {
        if (accountSid.isBlank() || authToken.isBlank() || fromNumber.isBlank()) {
            log.info("Twilio not configured; skipping SMS for {}", notification.getId());
            return false;
        }
        CustomerLookup.Contact contact = customerLookup.resolve(notification.getCustomerId());
        if (contact == null || contact.phone() == null || contact.phone().isBlank()) {
            log.warn("no phone on file for customer {}", notification.getCustomerId());
            return false;
        }

        try {
            if (!initialized) {
                Twilio.init(accountSid, authToken);
                initialized = true;
            }
            String body = (notification.getSubject() == null ? "" : notification.getSubject() + ": ")
                    + (notification.getBody() == null ? "" : notification.getBody());
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
}
