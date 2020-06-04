package com.github.tomakehurst.wiremock.jetty94;

import org.eclipse.jetty.io.Connection;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpChannel;
import org.eclipse.jetty.server.HttpConnection;
import org.eclipse.jetty.server.HttpTransport;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.SC_NOT_IMPLEMENTED;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.eclipse.jetty.http.HttpMethod.CONNECT;

/**
 * A Handler for the HTTP CONNECT method that, instead of opening up a
 * TCP tunnel between the downstream and upstream sockets, turns the connection
 * into an SSL connection allowing this server to handle it.
 */
class ManInTheMiddleSslConnectHandler extends AbstractHandler {

    @Override
    protected void doStart() throws Exception {
        super.doStart();
    }

    @Override
    protected void doStop() throws Exception {
        super.doStop();
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
        if (!(baseRequest.getHttpChannel().getHttpTransport() instanceof HttpConnection)) {
            response.sendError(SC_NOT_IMPLEMENTED, "CONNECT only supported over HTTP/1.1");
            return;
        }
        sendConnectResponse(response);

        HttpChannel httpChannel = baseRequest.getHttpChannel();
        Connector connector = httpChannel.getConnector();
        EndPoint endpoint = httpChannel.getEndPoint();
        endpoint.setConnection(null);

        Connection connection = connector.getConnectionFactory("ssl").newConnection(connector, endpoint);
        endpoint.setConnection(connection);

        endpoint.onOpen();
        connection.onOpen();
    }

    private void sendConnectResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.getOutputStream().close();
    }
}
