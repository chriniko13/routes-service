package com.adidas.chriniko.routesservice.core;

import org.javatuples.Pair;

import java.util.Base64;


public class SecuritySupport {

    public static Pair<String, String> getBasicAuthHeader(String username, String password) {

        String plainClientCredentials = username + ":" + password;
        String base64ClientCredentials = Base64.getEncoder().encodeToString(plainClientCredentials.getBytes());

        return Pair.with("Authorization", "Basic " + base64ClientCredentials);
    }

}
