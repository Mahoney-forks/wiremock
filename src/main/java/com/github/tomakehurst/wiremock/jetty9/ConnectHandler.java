package com.github.tomakehurst.wiremock.jetty9;

import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.io.Connection;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConnection;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ConnectHandler extends AbstractHandler {

    private final SslConnectionFactory sslConnectionFactory;

    public ConnectHandler(SslConnectionFactory sslConnectionFactory) {
        this.sslConnectionFactory = sslConnectionFactory;
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        sslConnectionFactory.start();
    }

    @Override
    protected void doStop() throws Exception {
        super.doStop();
        sslConnectionFactory.stop();
    }

    @Override
    public void handle(
        String target,
        Request baseRequest,
        HttpServletRequest request,
        HttpServletResponse response
    ) throws IOException {
        if (HttpMethod.CONNECT.is(request.getMethod())) {
            baseRequest.setHandled(true);
            handleConnect(baseRequest, response);
        }
    }

    private void handleConnect(
        Request baseRequest,
        HttpServletResponse response
    ) throws IOException {
        sendConnectResponse(response);
        final HttpConnection transport = (HttpConnection) baseRequest.getHttpChannel().getHttpTransport();
        EndPoint endpoint = transport.getEndPoint();
        Connection connection = sslConnectionFactory.newConnection(transport.getConnector(), endpoint);
        endpoint.setConnection(connection);
        connection.onOpen();
    }

    private ServerConnector findServerConnector() {
        Connector[] connectors = getServer().getConnectors();
        for (Connector connector : connectors) {
            if (connector instanceof ServerConnector && ((ServerConnector) connector).getDefaultProtocol().equals("SSL-http/1.1")) {
                return (ServerConnector) connector;
            }
        }
        throw new IllegalStateException("No ServerConnector");
    }

    private void sendConnectResponse(
            HttpServletResponse response
    ) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.getOutputStream().close();
    }
}
