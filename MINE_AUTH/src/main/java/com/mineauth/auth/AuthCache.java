package com.mineauth.auth;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AuthCache {
    private static final Map<UUID, Boolean> authenticated = new HashMap<>();

    public static boolean isAuthenticated(UUID uuid) {
        return authenticated.getOrDefault(uuid, false);
    }

    public static void authenticate(UUID uuid) {
        authenticated.put(uuid, true);
    }

    public static void logout(UUID uuid) {
        authenticated.remove(uuid);
    }
}
