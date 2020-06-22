package com.github.tomakehurst.wiremock.http.ssl;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;
import static java.util.Objects.requireNonNull;

public class InMemoryX509KeyStore extends X509KeyStore {

    private final KeyStoreType type;

    public InMemoryX509KeyStore(
        KeyStoreType type,
        Secret password
    ) throws KeyStoreException {
        super(initialise(requireNonNull(type, "type"), password), password);
        this.type = type;
    }

    private static KeyStore initialise(KeyStoreType type, Secret password) {
        try {
            KeyStore keyStore = KeyStore.getInstance(type.type);
            keyStore.load(null, password.value());
            return keyStore;
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
            return throwUnchecked(e, null);
        }
    }

    public FileBackedX509KeyStore saveAs(Path path, FileAttribute<?>... attrs) throws IOException {
        if (!Files.exists(path)) {
            Files.createFile(path, attrs);
        }
        try (FileOutputStream fos = new FileOutputStream(path.toFile())) {
            try {
                keyStore.store(fos, password.value());
            } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
                throwUnchecked(e);
            }
        }
        return new FileBackedX509KeyStore(keyStore, password, path);
    }
}
