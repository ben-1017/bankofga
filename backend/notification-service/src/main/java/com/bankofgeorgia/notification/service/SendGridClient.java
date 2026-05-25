package com.bankofgeorgia.notification.service;

import com.sendgrid.Request;
import com.sendgrid.Response;

import java.io.IOException;

public interface SendGridClient {
    Response send(Request request) throws IOException;
}
