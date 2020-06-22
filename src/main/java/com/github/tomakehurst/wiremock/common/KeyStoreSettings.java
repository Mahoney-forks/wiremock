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
package com.github.tomakehurst.wiremock.common;

import com.github.tomakehurst.wiremock.http.ssl.FileBackedX509KeyStore;
import com.github.tomakehurst.wiremock.http.ssl.KeyStoreType;
import com.github.tomakehurst.wiremock.http.ssl.ResourceBackedX509KeyStore;
import com.github.tomakehurst.wiremock.http.ssl.Secret;
import com.github.tomakehurst.wiremock.http.ssl.X509KeyStore;
import com.google.common.io.Resources;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;

public class KeyStoreSettings {

    public static final KeyStoreSettings NO_STORE = new KeyStoreSettings(null, null, null);

    private final String path;
    private final String password;
    private final String type;

    public KeyStoreSettings(String path, String password, String type) {
        this.path = path;
        this.password = password;
        this.type = type;
    }

    public String path() {
        return path;
    }

    public String password() {
        return password;
    }

    public String type() {
        return type;
    }

    public X509KeyStore loadStore() {
        try {
            if (exists()) {
                return new FileBackedX509KeyStore(Paths.get(path), KeyStoreType.of(type), new Secret(password));
            } else {
                return new ResourceBackedX509KeyStore(Resources.getResource(path), KeyStoreType.of(type), new Secret(password));
            }
        } catch (IOException | CertificateException | NoSuchAlgorithmException | KeyStoreException e) {
            return throwUnchecked(e, null);
        }
    }

    public boolean exists() {
        return new File(path).isFile();
    }
}
