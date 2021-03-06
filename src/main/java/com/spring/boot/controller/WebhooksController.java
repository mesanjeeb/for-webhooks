package com.spring.boot.controller;

import com.braintreegateway.BraintreeGateway;
import com.braintreegateway.WebhookNotification;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping(value = "/", produces = "application/json")
public class WebhooksController {

    @Autowired
    public BraintreeGateway braintreeGateway;

    private static final Logger LOGGER = LoggerFactory.getLogger(WebhooksController.class);


    @RequestMapping(value = "/hello", method = GET)
    @ResponseBody
    public String hello() {
        return "hello there\n";
    }


    @RequestMapping(value = "/webhooks", method = POST)
    public void handleWebhooks(final HttpServletRequest request) throws  IOException {
        if ("POST".equalsIgnoreCase(request.getMethod())) {
            final String requestBody = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
            logObject(requestBody);
        }
        else {
            LOGGER.info("Received GET Request");
        }
    }

    @RequestMapping(value = "/braintree-webhooks", method = POST)
    public void handleBraintreeWebhooks(final HttpServletRequest httpServletRequest) {
        final String btSignature = extractFromServletRequest(httpServletRequest, "bt_signature");
        final String btPayload = extractFromServletRequest(httpServletRequest, "bt_payload");

        logBraintreeWebhookPayLoad(btSignature, btPayload);
    }

    @RequestMapping(value = "/webhooks/{merchant_account_id}", method = POST)
    public void handle(@PathVariable("merchant_account_id") @NotEmpty final String merchantAccountId, final HttpServletRequest httpServletRequest) {

        LOGGER.info("merchant_account_id={}", merchantAccountId);

        logObject(httpServletRequest.getParameterMap());
    }

    @RequestMapping(value = "/object", method = POST, consumes = APPLICATION_JSON_VALUE)
    public String handleAnyJson(final InputStream inputStream) throws IOException {
        final String jsonInRequest = IOUtils.toString(inputStream, Charset.defaultCharset());
        LOGGER.info("jsonInRequest={}", jsonInRequest);
        return jsonInRequest;
    }

    private static void logObject(final Object object) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        LOGGER.info(gson.toJson(object));
    }

    private String extractFromServletRequest(final ServletRequest servletRequest, final String requestParam) {
        return servletRequest.getParameter(requestParam);
    }

    private void logBraintreeWebhookPayLoad(final String btSignature, final String btPayload) {
        final WebhookNotification webhookNotification = braintreeGateway.webhookNotification().parse(btSignature, btPayload);
        logObject(webhookNotification);
    }
}
