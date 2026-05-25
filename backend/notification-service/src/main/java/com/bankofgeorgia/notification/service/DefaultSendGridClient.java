package com.bankofgeorgia.notification.service;

import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class DefaultSendGridClient implements SendGridClient {

    private final String apiKey;

    public DefaultSendGridClient(@Value("${app.sendgrid.api-key:}") String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public Response send(Request request) throws IOException {
        return new SendGrid(apiKey).api(request);
    }
}
