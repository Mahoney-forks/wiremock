package com.github.tomakehurst.wiremock;

import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import org.apache.hc.core5.http.HttpVersion;
import org.apache.hc.core5.http.URIScheme;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.apache.hc.core5.http.HttpStatus.SC_INTERNAL_SERVER_ERROR;
import static org.apache.hc.core5.http.HttpStatus.SC_OK;
import static org.apache.hc.core5.http.HttpVersion.HTTP_1_1;
import static org.apache.hc.core5.http.HttpVersion.HTTP_2;
import static org.apache.hc.core5.http.URIScheme.HTTP;
import static org.apache.hc.core5.http.URIScheme.HTTPS;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class NewHttpsBrowserProxyAcceptanceTest {

    private final URIScheme proxyScheme;
    private final List<HttpVersion> clientConnectOfferedProtocols;
    private final List<HttpVersion> clientGetOfferedProtocols;

    @Test//(timeout = 2000L)
    public void returnsSuccessfulResponseFromProxy() throws Exception {
        target.stubFor(get(urlEqualTo("/whatever")).willReturn(aResponse().withBody("Got it")));
        HttpClient httpClient = clientProxiedVia(trustingProxy, proxyScheme, clientConnectOfferedProtocols, clientGetOfferedProtocols);

        Response response = httpClient.get(target.url("/whatever"));

        assertEquals(SC_OK, response.status);
        assertEquals("Got it", response.body);
    }

    @Test(timeout = 2000L)
    public void returnsErrorResponseFromProxy() throws Exception {
        HttpClient httpClient = clientProxiedVia(scepticalProxy, proxyScheme, clientConnectOfferedProtocols, clientGetOfferedProtocols);

        Response response = httpClient.get(target.url("/whatever"));

        assertEquals(SC_INTERNAL_SERVER_ERROR, response.status);
    }

    @Parameterized.Parameters(name = "{index}: proxyScheme={0}, clientConnectOfferedProtocols={1}, clientGetOfferedProtocols={2}")
    public static Collection<Object[]> data() {
        return asList(new Object[][] {
               // proxyScheme clientConnectOfferedProtocols clientGetOfferedProtocols
                { HTTP,       emptyList(),                  asList(HTTP_1_1)         },
                { HTTP,       emptyList(),                  asList(HTTP_1_1, HTTP_2) },
                { HTTPS,      asList(HTTP_1_1),             asList(HTTP_1_1)         },
                { HTTPS,      asList(HTTP_1_1),             asList(HTTP_1_1, HTTP_2) },
                { HTTPS,      asList(HTTP_1_1, HTTP_2),     asList(HTTP_1_1)         },
                { HTTPS,      asList(HTTP_1_1, HTTP_2),     asList(HTTP_1_1, HTTP_2) }
        });
    }

    private static HttpClient clientProxiedVia(
        WireMockClassRule proxy,
        URIScheme proxyScheme,
        List<HttpVersion> clientConnectOfferedProtocols,
        List<HttpVersion> clientGetOfferedProtocols
    ) throws Exception {
        HttpProxiedClientBuilder clientBuilder = new ApacheHC4ClientBuilder();
        return clientBuilder.buildClient(proxyScheme.name(), proxyScheme == HTTP ? proxy.port() : proxy.httpsPort());
    }

    @ClassRule
    public static WireMockClassRule target = new WireMockClassRule(wireMockConfig()
            .httpDisabled(true)
            .dynamicHttpsPort()
    );

    @ClassRule
    public static WireMockClassRule trustingProxy = new WireMockClassRule(wireMockConfig()
            .dynamicPort()
            .dynamicHttpsPort()
            .enableBrowserProxying(true)
            .trustAllProxyTargets(true)
    );

    @ClassRule
    public static WireMockClassRule scepticalProxy = new WireMockClassRule(wireMockConfig()
            .dynamicPort()
            .dynamicHttpsPort()
            .enableBrowserProxying(true)
            .trustAllProxyTargets(false)
    );

    public NewHttpsBrowserProxyAcceptanceTest(
            URIScheme proxyScheme,
            List<HttpVersion> clientConnectOfferedProtocols,
            List<HttpVersion> clientGetOfferedProtocols
    ) {
        this.proxyScheme = proxyScheme;
        this.clientConnectOfferedProtocols = clientConnectOfferedProtocols;
        this.clientGetOfferedProtocols = clientGetOfferedProtocols;
    }
}
