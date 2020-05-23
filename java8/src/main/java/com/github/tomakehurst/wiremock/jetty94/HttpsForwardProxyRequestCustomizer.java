package com.github.tomakehurst.wiremock.jetty94;

import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConfiguration.Customizer;
import org.eclipse.jetty.server.Request;

class HttpsForwardProxyRequestCustomizer implements Customizer {

    @Override
    public void customize(Connector connector, HttpConfiguration channelConfig, Request request) {
        if (connector instanceof HasHttpsForwardProxyHostAndPort) {
            HasHttpsForwardProxyHostAndPort hasForwardProxyHostAndPort = (HasHttpsForwardProxyHostAndPort) connector;
            request.setHttpURI(new HttpURI("https://" + hasForwardProxyHostAndPort.getProxyHostAndPort() + request.getPathInfo()));
            request.setSecure(true);
            request.setScheme("https");
        }
    }
}
