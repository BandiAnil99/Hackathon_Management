package com.hackathon.util;

import java.util.List;
import java.util.Set;

public final class LocationConstants {

    public static final List<String> ALLOWED_LOCATIONS = List.of(
            "KRC, Raidurg, Hyderabad, Telangana",
            "Sector 126, Noida, Uttar Pradesh",
            "Sholinganallur, Chennai, Tamil Nadu",
            "Jigani, Bengaluru, Karnataka",
            "Karle Town Centre, Nagawara, Bengaluru, Karnataka"
    );

    public static final Set<String> ALLOWED_LOCATION_SET = Set.copyOf(ALLOWED_LOCATIONS);

    private LocationConstants() {
    }
}
