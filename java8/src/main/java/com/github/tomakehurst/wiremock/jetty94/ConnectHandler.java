package com.github.tomakehurst.wiremock.jetty94;

import org.eclipse.jetty.io.Connection;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnection;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.eclipse.jetty.http.HttpMethod.CONNECT;

public class ConnectHandler extends AbstractHandler {

    private final SslConnectionFactory sslConnectionFactory;
    private final HttpConfiguration httpConfiguration;

    public ConnectHandler(
        SslConnectionFactory sslConnectionFactory,
        HttpConfiguration httpConfiguration
    ) {
        this.sslConnectionFactory = sslConnectionFactory;
        this.httpConfiguration = httpConfiguration;
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
        if (CONNECT.is(request.getMethod())) {
            baseRequest.setHandled(true);
            handleConnect(baseRequest, response);
        }
    }

    private void handleConnect(
        Request baseRequest,
        HttpServletResponse response
    ) throws IOException {
        sendConnectResponse(response);
        final String hostAndPort = baseRequest.getPathInfo();
        final HttpConnection transport = (HttpConnection) baseRequest.getHttpChannel().getHttpTransport();
        EndPoint endpoint = transport.getEndPoint();
        Connector connector = new ConnectorWithForwardProxyHostAndPort(transport.getConnector(), hostAndPort);
        Connection connection = sslConnectionFactory.newConnection(connector, endpoint);
        endpoint.setConnection(connection);
        connection.onOpen();
    }

    private void sendConnectResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.getOutputStream().close();
    }
}
