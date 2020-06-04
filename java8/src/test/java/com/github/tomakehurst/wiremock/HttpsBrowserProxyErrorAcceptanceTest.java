/*
 * Copyright (C) 2011 Thomas Akehurst
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.tomakehurst.wiremock;

import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.github.tomakehurst.wiremock.testsupport.TestFiles;
import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

public class HttpsBrowserProxyErrorAcceptanceTest {

    private static final String CERTIFICATE_NOT_TRUSTED_BY_TEST_CLIENT = TestFiles.KEY_STORE_PATH;

    @ClassRule
    public static WireMockClassRule target = new WireMockClassRule(wireMockConfig()
            .httpDisabled(true)
            .keystorePath(CERTIFICATE_NOT_TRUSTED_BY_TEST_CLIENT)
            .dynamicHttpsPort()
    );

    @Rule
    public WireMockClassRule instanceRule = target;

    private WireMockServer proxy;
    private WireMockTestClient testClient;

    @Before
    public void addAResourceToProxy() {
        testClient = new WireMockTestClient(target.httpsPort());

        proxy = new WireMockServer(wireMockConfig()
                .dynamicPort()
                .dynamicHttpsPort()
                .enableBrowserProxying(true)
        );
        proxy.start();
    }

    @After
    public void stopServer() {
        if (proxy.isRunning()) {
            proxy.stop();
        }
    }

    @Test(timeout=1000)
    public void canReturnHttpsErrorsQuicklyInBrowserProxyMode() throws Exception {
        WireMockResponse response = testClient.getViaProxy(target.url("/whatever"), proxy.port());
        assertThat(response.statusCode(), is(SC_INTERNAL_SERVER_ERROR));
        assertThat(response.content(), containsString("SSLException"));
    }

    @Test(timeout=1000)
    public void canReturnHttpsErrorsQuicklyInBrowserHttpsProxyMode() throws Exception {
        WireMockResponse response = testClient.getViaProxy(target.url("/whatever"), proxy.httpsPort(), "https");
        assertThat(response.statusCode(), is(SC_INTERNAL_SERVER_ERROR));
        assertThat(response.content(), containsString("SSLException"));
    }
}
/*
HTTP proxy, HTTP target - so on the HTTP connector
| route to proxy  | Supported? | Comment
| 1.1             | Y          |
| 2               | N          | WM does not support raw H2

HTTP proxy, HTTPS target - so on the HTTP connector. No CONNECT call.
| client GET offer | agreed GET protocol | Supported? | Comment
| 1.1              | 1.1                 | Y          |
| 1.1, 2           | 1.1                 | Y          | We don't know if the target supports HTTP/2 anyway, so we can't be realistic, so may as well keep it simple and use 1.1.
|                  |                     |            | Might be possible to make this use 2 but we'd need to:
|                  |                     |            | * Add an SSL connection factory to the http connector
|                  |                     |            | * Make the ManInTheMiddleSslConnectHandler's ssl factory use alpn as its next protocol
|                  |                     |            |   - might be easier for it to look it up by protocol (`connector.getConnectionFactory("alpn"`) rather than injecting it.


HTTPS proxy, HTTPS target - so on the HTTPS connector, alpn & h2 available
| client CONNECT offer | client GET offer | Supported? | agreed CONNECT protocol | agreed GET protocol | Comment
| 1.1                  | 1.1              | Y          | 1.1                     | 1.1                 |
| 1.1                  | 1.1, 2           | Y          | 1.1                     | 1.1                 | This is what curl does. We have to use 1.1 on the GET because if we use 2 and an error occurs (e.g. invalid target certificate)
|                      |                  |            |                         |                     | it fails to flush for 30 seconds. Should be fixable, but I don't know how!
| 1.1, 2               | N/A              | N          | 2                       | N/A                 | We can't yet support a CONNECT over HTTP/2 because Jetty 9.4 can't support it (10 does).
|                      |                  |            |                         |                     | But we can't negotiate down because the ALPN negotiation happens before we know the method,
|                      |                  |            |                         |                     | and we want HTTP/2 to be possible when not browser proxying

| route to connect | route to actual call | Supported? | Reason
| HTTP/1.1         | HTTPS->HTTP/1.1      | Y          |
| HTTP/1.1         | HTTPS->HTTP/2        | N          | I can't work out how to test this!
| HTTP/2           | HTTPS->HTTP/1.1      | N          | We don't support raw HTTP/2. We don't support CONNECT on HTTP/2.
| HTTP/2           | HTTPS->HTTP/2        | N          | We don't support raw HTTP/2. We don't support CONNECT on HTTP/2.
| HTTPS->HTTP/1.1  | HTTPS->HTTP/1.1      | Y          |
| HTTPS->HTTP/1.1  | HTTPS->HTTP/2        | N          | Curl does this if server offers HTTP/2. It doesn't work on errors, so we remove support. Hard to test; needs a client that can offer different protocols on CONNECT and
| HTTPS->HTTP/2    | HTTPS->HTTP/1.1      | ?          | We don't support CONNECT on HTTP/2 (Hard to test... why would client neg. 2 on CONNECT but 1.1 on call?)
| HTTPS->HTTP/2    | HTTPS->HTTP/2        | N          | We don't support CONNECT on HTTP/2
 */
