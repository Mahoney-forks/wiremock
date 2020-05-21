package com.github.tomakehurst.wiremock.jetty94;

import org.eclipse.jetty.server.Connector;

public class ConnectorWithForwardProxyHostAndPort extends ConnectorFacade implements HasForwardProxyHostAndPort {

    private final String proxyHostAndPort;

    ConnectorWithForwardProxyHostAndPort(Connector delegate, String proxyHostAndPort) {
        super(delegate);
        this.proxyHostAndPort = proxyHostAndPort;
    }

    @Override
    public String getProxyHostAndPort() {
        return proxyHostAndPort;
    }
}
