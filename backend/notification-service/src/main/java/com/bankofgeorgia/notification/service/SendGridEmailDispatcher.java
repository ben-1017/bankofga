package com.bankofgeorgia.notification.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.bankofgeorgia.notification.model.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SendGridEmailDispatcher {

    private static final Logger log = LoggerFactory.getLogger(SendGridEmailDispatcher.class);

    private final String apiKey;
    private final String fromAddress;
    private final CustomerLookup customerLookup;

    public SendGridEmailDispatcher(@Value("${app.sendgrid.api-key:}") String apiKey,
                                   @Value("${app.sendgrid.from:no-reply@bankofgeorgia.dev}") String fromAddress,
                                   CustomerLookup customerLookup) {
        this.apiKey = apiKey;
        this.fromAddress = fromAddress;
        this.customerLookup = customerLookup;
    }

    public boolean send(Notification notification) {
        if (apiKey == null || apiKey.isBlank()) {
            log.info("SendGrid api-key not configured; skipping email for {}", notification.getId());
            return false;
        }
        CustomerLookup.Contact contact = customerLookup.resolve(notification.getCustomerId());
        if (contact == null || contact.email() == null || contact.email().isBlank()) {
            log.warn("no email on file for customer {}", notification.getCustomerId());
            return false;
        }

        Mail mail = new Mail(
                new Email(fromAddress),
                notification.getSubject() == null ? "Bank of Georgia notification" : notification.getSubject(),
                new Email(contact.email()),
                new Content("text/plain", notification.getBody() == null ? "" : notification.getBody()));

        try {
            SendGrid sg = new SendGrid(apiKey);
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            int status = response.getStatusCode();
            if (status >= 200 && status < 300) {
                log.info("SendGrid delivered email to {} (status {})", contact.email(), status);
                return true;
            }
            log.warn("SendGrid rejected email (status {}): {}", status, response.getBody());
            return false;
        } catch (IOException ex) {
            log.warn("SendGrid send failed: {}", ex.getMessage());
            return false;
        }
    }
}
