package com.bankofgeorgia.notification.service;

import com.bankofgeorgia.notification.model.Notification;
import com.sendgrid.Request;
import com.sendgrid.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SendGridEmailDispatcherTest {

    @Mock private CustomerLookup customerLookup;
    @Mock private SendGridClient sendGridClient;

    @Test
    void send_returnsFalseAndDoesNotResolveCustomer_whenApiKeyMissing() throws IOException {
        SendGridEmailDispatcher dispatcher = new SendGridEmailDispatcher(
                "", "no-reply@bankofgeorgia.dev", customerLookup, sendGridClient);

        boolean sent = dispatcher.send(notification());

        assertThat(sent).isFalse();
        verify(customerLookup, never()).resolve("cust-1");
        verify(sendGridClient, never()).send(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void send_buildsMailRequestAndReturnsTrue_whenSendGridAccepts() throws IOException {
        when(customerLookup.resolve("cust-1"))
                .thenReturn(new CustomerLookup.Contact("ada@example.com", "555-0100"));
        Response accepted = new Response();
        accepted.setStatusCode(202);
        when(sendGridClient.send(org.mockito.ArgumentMatchers.any(Request.class))).thenReturn(accepted);

        SendGridEmailDispatcher dispatcher = new SendGridEmailDispatcher(
                "SG.fake-key", "no-reply@bankofgeorgia.dev", customerLookup, sendGridClient);

        boolean sent = dispatcher.send(notification());

        assertThat(sent).isTrue();
        verify(sendGridClient).send(org.mockito.ArgumentMatchers.argThat(req ->
                req.getMethod() == com.sendgrid.Method.POST
                        && "mail/send".equals(req.getEndpoint())
                        && req.getBody().contains("ada@example.com")
                        && req.getBody().contains("Low balance alert")));
    }

    @Test
    void send_returnsFalse_whenCustomerHasNoEmail() throws IOException {
        when(customerLookup.resolve("cust-1"))
                .thenReturn(new CustomerLookup.Contact("", "555-0100"));
        SendGridEmailDispatcher dispatcher = new SendGridEmailDispatcher(
                "SG.fake-key", "no-reply@bankofgeorgia.dev", customerLookup, sendGridClient);

        boolean sent = dispatcher.send(notification());

        assertThat(sent).isFalse();
        verify(sendGridClient, never()).send(org.mockito.ArgumentMatchers.any());
    }

    private static Notification notification() {
        Notification n = new Notification();
        n.setId("n-1");
        n.setCustomerId("cust-1");
        n.setSubject("Low balance alert");
        n.setBody("Your balance is below the threshold.");
        return n;
    }
}
