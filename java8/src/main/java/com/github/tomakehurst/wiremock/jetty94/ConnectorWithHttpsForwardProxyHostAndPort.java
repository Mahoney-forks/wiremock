package com.github.tomakehurst.wiremock.jetty94;

import org.eclipse.jetty.server.Connector;

class ConnectorWithHttpsForwardProxyHostAndPort extends ConnectorFacade implements HasHttpsForwardProxyHostAndPort {

    private final String proxyHostAndPort;

    ConnectorWithHttpsForwardProxyHostAndPort(Connector delegate, String proxyHostAndPort) {
        super(delegate);
        this.proxyHostAndPort = proxyHostAndPort;
    }

    @Override
    public String getProxyHostAndPort() {
        return proxyHostAndPort;
    }
}
