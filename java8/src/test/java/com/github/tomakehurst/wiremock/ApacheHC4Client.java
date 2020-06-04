package com.github.tomakehurst.wiremock;

import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import org.apache.hc.core5.http.URIScheme;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.net.URI;

import static com.google.common.base.Strings.isNullOrEmpty;

public class ApacheHC4Client {

    static HttpClient buildClient(WireMockClassRule proxyServer, URIScheme proxyScheme) throws Exception {
        return url -> {

            HttpClientBuilder httpClientBuilder = HttpClientBuilder.create()
                    .setSSLContext(WireMockTestClient.buildTrustWireMockDefaultCertificateSSLContext())
                    .setSSLHostnameVerifier(new NoopHostnameVerifier());

            int proxyPort = proxyScheme == URIScheme.HTTP ? proxyServer.port() : proxyServer.httpsPort();

            HttpHost proxyHost = new HttpHost("localhost", proxyPort, proxyScheme.name());

            org.apache.http.client.HttpClient httpClientUsingProxy = httpClientBuilder
                .setProxy(proxyHost)
                .build();

            URI targetUri = URI.create(url);
            HttpHost target = new HttpHost(targetUri.getHost(), targetUri.getPort(), targetUri.getScheme());
            HttpGet req = new HttpGet(targetUri.getPath() + (isNullOrEmpty(targetUri.getQuery()) ? "" : "?" + targetUri.getQuery()));

            HttpResponse httpResponse = httpClientUsingProxy.execute(target, req);
            return new Response(httpResponse.getStatusLine().getStatusCode(), EntityUtils.toString(httpResponse.getEntity()));
        };
    }
}
