package com.github.tomakehurst.wiremock.jetty94;

import com.github.tomakehurst.wiremock.common.HttpsSettings;
import com.github.tomakehurst.wiremock.common.JettySettings;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.http.AdminRequestHandler;
import com.github.tomakehurst.wiremock.http.StubRequestHandler;
import com.github.tomakehurst.wiremock.jetty9.DefaultMultipartRequestConfigurer;
import com.github.tomakehurst.wiremock.jetty9.JettyHttpServer;
import com.github.tomakehurst.wiremock.servlet.MultipartRequestConfigurer;
import org.eclipse.jetty.alpn.server.ALPNServerConnectionFactory;
import org.eclipse.jetty.http2.HTTP2Cipher;
import org.eclipse.jetty.http2.server.HTTP2ServerConnectionFactory;
import org.eclipse.jetty.io.NetworkTrafficListener;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.util.ssl.SslContextFactory;

public class Jetty94HttpServer extends JettyHttpServer {

    public Jetty94HttpServer(Options options, AdminRequestHandler adminRequestHandler, StubRequestHandler stubRequestHandler) {
        super(options, adminRequestHandler, stubRequestHandler);
    }

    @Override
    protected MultipartRequestConfigurer buildMultipartRequestConfigurer() {
        return new DefaultMultipartRequestConfigurer();
    }

    @Override
    protected ServerConnector createHttpConnector(Options options, NetworkTrafficListener listener) {

        ConnectionFactories connectionFactories = buildConnectionFactories(options.jettySettings(), options.httpsSettings(), 0);
        return createServerConnector(
                options.bindAddress(),
                options.jettySettings(),
                options.portNumber(),
                listener,
                // http needs to be the first (the default)
                connectionFactories.http,
                // ssl, alpn & h2 are included so that HTTPS forward proxying can find them
                connectionFactories.ssl,
                connectionFactories.alpn,
                connectionFactories.h2
        );
    }

    @Override
    protected ServerConnector createHttpsConnector(Server server, String bindAddress, HttpsSettings httpsSettings, JettySettings jettySettings, NetworkTrafficListener listener) {

        ConnectionFactories connectionFactories = buildConnectionFactories(jettySettings, httpsSettings, httpsSettings.port());

        return createServerConnector(
                bindAddress,
                jettySettings,
                httpsSettings.port(),
                listener,
                connectionFactories.ssl,
                connectionFactories.alpn,
                connectionFactories.h2,
                connectionFactories.http
        );
    }

    private SslContextFactory.Server buildHttp2SslContextFactory(HttpsSettings httpsSettings) {
        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();

        sslContextFactory.setKeyStorePath(httpsSettings.keyStorePath());
        sslContextFactory.setKeyManagerPassword(httpsSettings.keyStorePassword());
        sslContextFactory.setKeyStoreType(httpsSettings.keyStoreType());
        if (httpsSettings.hasTrustStore()) {
            sslContextFactory.setTrustStorePath(httpsSettings.trustStorePath());
            sslContextFactory.setTrustStorePassword(httpsSettings.trustStorePassword());
        }
        sslContextFactory.setNeedClientAuth(httpsSettings.needClientAuth());
        sslContextFactory.setCipherComparator(HTTP2Cipher.COMPARATOR);
        sslContextFactory.setProvider("Conscrypt");
        return sslContextFactory;
    }

    @Override
    protected HttpConfiguration createHttpConfig(JettySettings jettySettings) {
        HttpConfiguration httpConfig = super.createHttpConfig(jettySettings);
        httpConfig.setSendXPoweredBy(false);
        httpConfig.setSendServerVersion(false);
        httpConfig.addCustomizer(new SecureRequestCustomizer());
        return httpConfig;
    }

    @Override
    protected HandlerCollection createHandler(
        Options options,
        AdminRequestHandler adminRequestHandler,
        StubRequestHandler stubRequestHandler
    ) {
        HandlerCollection handler = super.createHandler(options, adminRequestHandler, stubRequestHandler);

        ManInTheMiddleSslConnectHandler manInTheMiddleSslConnectHandler = new ManInTheMiddleSslConnectHandler();

        handler.addHandler(manInTheMiddleSslConnectHandler);

        return handler;
    }

    private ConnectionFactories buildConnectionFactories(
        JettySettings jettySettings,
        HttpsSettings httpsSettings,
        int securePort
    ) {
        HttpConfiguration httpConfig = createHttpConfig(jettySettings);
        httpConfig.setSecurePort(securePort);

        HttpConnectionFactory http = new HttpConnectionFactory(httpConfig);
        HTTP2ServerConnectionFactory h2 = new HTTP2ServerConnectionFactory(httpConfig);
        ALPNServerConnectionFactory alpn = new ALPNServerConnectionFactory(h2.getProtocol().toLowerCase(), http.getProtocol().toLowerCase());
        SslContextFactory.Server http2SslContextFactory = buildHttp2SslContextFactory(httpsSettings);
        SslConnectionFactory ssl = new SslConnectionFactory(http2SslContextFactory, alpn.getProtocol());

        return new ConnectionFactories(http, h2, alpn, ssl);
    }

    private static class ConnectionFactories {
        private final HttpConnectionFactory http;
        private final HTTP2ServerConnectionFactory h2;
        private final ALPNServerConnectionFactory alpn;
        private final SslConnectionFactory ssl;

        private ConnectionFactories(HttpConnectionFactory http, HTTP2ServerConnectionFactory h2, ALPNServerConnectionFactory alpn, SslConnectionFactory ssl) {
            this.http = http;
            this.h2 = h2;
            this.alpn = alpn;
            this.ssl = ssl;
        }
    }
}
