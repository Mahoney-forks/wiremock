package com.github.tomakehurst.wiremock;

import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.URIScheme;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.net.URI;

import static com.google.common.base.Strings.isNullOrEmpty;

public class ApacheHC5Client {

    static HttpClient buildClient(WireMockClassRule proxyServer, URIScheme proxyScheme) throws Exception {
        return url -> {

            SSLConnectionSocketFactory sslSocketFactory = SSLConnectionSocketFactoryBuilder.create()
                    .setSslContext(WireMockTestClient.buildTrustWireMockDefaultCertificateSSLContext())
                    .setHostnameVerifier(new NoopHostnameVerifier())
                    .build();

            HttpClientConnectionManager cm = PoolingHttpClientConnectionManagerBuilder.create()
                    .setSSLSocketFactory(sslSocketFactory)
                    .build();

            HttpClientBuilder httpClientBuilder = HttpClientBuilder.create()
                    .setConnectionManager(cm);

            int proxyPort = proxyScheme == URIScheme.HTTP ? proxyServer.port() : proxyServer.httpsPort();

            HttpHost proxyHost = new HttpHost(proxyScheme.name(), "localhost", proxyPort);

            org.apache.hc.client5.http.classic.HttpClient httpClientUsingProxy = httpClientBuilder
                .setProxy(proxyHost)
                .build();

            URI targetUri = URI.create(url);
            HttpHost target = new HttpHost(targetUri.getScheme(), targetUri.getHost(), targetUri.getPort());
            HttpGet req = new HttpGet(targetUri.getPath() + (isNullOrEmpty(targetUri.getQuery()) ? "" : "?" + targetUri.getQuery()));

            ClassicHttpResponse httpResponse = httpClientUsingProxy.execute(target, req);
            return new Response(httpResponse.getCode(), EntityUtils.toString(httpResponse.getEntity()));
        };
    }
}
