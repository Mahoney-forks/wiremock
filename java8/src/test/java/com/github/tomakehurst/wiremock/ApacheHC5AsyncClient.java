package com.github.tomakehurst.wiremock;

import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequests;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.URIScheme;
import org.apache.hc.core5.http.nio.ssl.TlsStrategy;
import org.apache.hc.core5.http2.HttpVersionPolicy;
import org.apache.hc.core5.reactor.ssl.TlsDetails;

import java.net.URI;

import static com.google.common.base.Strings.isNullOrEmpty;

public class ApacheHC5AsyncClient {

    static HttpClient buildClient(WireMockClassRule proxyServer, URIScheme proxyScheme) throws Exception {
        return url -> {

            TlsStrategy tlsStrategy = ClientTlsStrategyBuilder.create()
                    .setSslContext(WireMockTestClient.buildTrustWireMockDefaultCertificateSSLContext())
                    .setHostnameVerifier(new NoopHostnameVerifier())
                    .setTlsDetailsFactory(sslEngine -> new TlsDetails(sslEngine.getSession(), sslEngine.getApplicationProtocol()))
                    .build();

            PoolingAsyncClientConnectionManager cm = PoolingAsyncClientConnectionManagerBuilder.create()
                    .setTlsStrategy(tlsStrategy)
                    .build();

            HttpAsyncClientBuilder clientBuilder = HttpAsyncClients.custom()
                    .setVersionPolicy(HttpVersionPolicy.NEGOTIATE)
                    .setConnectionManager(cm);

            int proxyPort = proxyScheme == URIScheme.HTTP ? proxyServer.port() : proxyServer.httpsPort();

            HttpHost proxyHost = new HttpHost(proxyScheme.name(), "localhost", proxyPort);

            CloseableHttpAsyncClient httpClientUsingProxy = clientBuilder
                .setProxy(proxyHost)
                .build();

            httpClientUsingProxy.start();

            URI targetUri = URI.create(url);
            HttpHost target = new HttpHost(targetUri.getScheme(), targetUri.getHost(), targetUri.getPort());
            SimpleHttpRequest request = SimpleHttpRequests.get(target, targetUri.getPath() + (isNullOrEmpty(targetUri.getQuery()) ? "" : "?" + targetUri.getQuery()));
            SimpleHttpResponse httpResponse = httpClientUsingProxy.execute(
                    request,
                    new FutureCallback<SimpleHttpResponse>() {
                        @Override
                        public void completed(final SimpleHttpResponse response) {
                        }

                        @Override
                        public void failed(final Exception ex) {
                        }

                        @Override
                        public void cancelled() {
                        }
                    }
            ).get();

            httpClientUsingProxy.close();

            return new Response(httpResponse.getCode(), httpResponse.getBodyText());
        };
    }
}
