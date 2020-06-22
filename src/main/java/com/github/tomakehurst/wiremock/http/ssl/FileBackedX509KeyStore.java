package com.github.tomakehurst.wiremock.http.ssl;

import java.io.FileInputStream;
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

public class FileBackedX509KeyStore extends X509KeyStore {

    private final Path path;

    public FileBackedX509KeyStore(
        Path path,
        KeyStoreType type,
        Secret password,
        FileAttribute<?>... attrs
    ) throws IOException {
        this(initialise(requireNonNull(path), requireNonNull(type, "type"), password, attrs), password, path);
    }

    FileBackedX509KeyStore(KeyStore keyStore, Secret password, Path path) {
        super(keyStore, password);
        this.path = path;
    }

    private static KeyStore initialise(Path path, KeyStoreType type, Secret password, FileAttribute<?>... attrs) throws IOException {
        if (!Files.exists(path)) {
            Files.createFile(path, attrs);
            try (FileOutputStream fos = new FileOutputStream(path.toFile())) {
                KeyStore keyStore = KeyStore.getInstance(type.type);
                keyStore.load(null, password.value());
                keyStore.store(fos, password.value());
                return keyStore;
            } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
                return throwUnchecked(e, null);
            }
        } else {
            try (FileInputStream fis = new FileInputStream(path.toFile())) {
                KeyStore keyStore = KeyStore.getInstance(type.type);
                keyStore.load(fis, password.value());
                return keyStore;
            } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
                return throwUnchecked(e, null);
            }
        }
    }

    public FileBackedX509KeyStore save() throws IOException {
        try (FileOutputStream fos = new FileOutputStream(path.toFile())) {
            try {
                keyStore.store(fos, password.value());
            } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
                throwUnchecked(e);
            }
            return this;
        }
    }
}
