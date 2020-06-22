package com.github.tomakehurst.wiremock.http.ssl;

public enum KeyStoreType {
    JKS("jks");

    final String type;

    KeyStoreType(String type) {
        this.type = type;
    }

    public static KeyStoreType of(String type) {
        return KeyStoreType.valueOf(type.toUpperCase());
    }
}
