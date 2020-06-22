package com.github.tomakehurst.wiremock.http.ssl;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

class SimpleCertChainAndKey implements CertChainAndKey {
    final X509Certificate[] certificateChain;
    final PrivateKey key;

    SimpleCertChainAndKey(X509Certificate[] certificateChain, PrivateKey key) {
        this.certificateChain = certificateChain;
        this.key = key;
    }

    @Override
    public X509Certificate[] certificateChain() {
        return certificateChain;
    }

    @Override
    public PrivateKey key() {
        return key;
    }
}
