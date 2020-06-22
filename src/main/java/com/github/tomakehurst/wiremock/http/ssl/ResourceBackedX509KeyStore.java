package com.github.tomakehurst.wiremock.http.ssl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import static java.util.Objects.requireNonNull;

public class ResourceBackedX509KeyStore extends X509KeyStore {
    public ResourceBackedX509KeyStore(URL resource, KeyStoreType type, Secret password) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
        super(initialise(requireNonNull(resource), requireNonNull(type, "type"), password), password);
    }

    private static KeyStore initialise(URL url, KeyStoreType type, Secret password) throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException {
        try (InputStream is = url.openStream()) {
            KeyStore keyStore = KeyStore.getInstance(type.type);
            keyStore.load(is, password.value());
            return keyStore;
        }
    }
}
