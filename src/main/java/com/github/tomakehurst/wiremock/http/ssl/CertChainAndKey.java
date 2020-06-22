package com.github.tomakehurst.wiremock.http.ssl;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

public interface CertChainAndKey {
    X509Certificate[] certificateChain();

    PrivateKey key();
}
