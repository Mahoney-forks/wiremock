package com.github.tomakehurst.wiremock.http.ssl;

import javax.net.ssl.SNIHostName;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

public interface CertificateAuthority extends CertChainAndKey {
    X509Certificate[] certificateChain();

    X509Certificate issuer();

    PrivateKey key();

    CertChainAndKey generateCertificate(
            String keyType,
            SNIHostName hostName
    ) throws CertificateGenerationUnsupportedException;
}
