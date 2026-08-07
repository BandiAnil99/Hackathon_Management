package com.hackathon.util;

/**
 * Builds public frontend links from the single APP_FRONTEND_URL setting.
 * Keeping this in one place prevents QR, invitation and post-check-in links
 * from drifting to localhost or to an older deployment URL.
 */
public final class FrontendUrlBuilder {

    private FrontendUrlBuilder() {
    }

    public static String build(String frontendUrl, String path) {
        if (frontendUrl == null || frontendUrl.isBlank()) {
            throw new IllegalStateException("APP_FRONTEND_URL must be configured to build public links");
        }
        if (path == null || !path.startsWith("/")) {
            throw new IllegalArgumentException("Frontend path must begin with '/'");
        }

        String base = frontendUrl.trim();
        while (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base + path;
    }
}
