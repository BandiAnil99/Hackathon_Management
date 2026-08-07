package com.hackathon.controller;

import java.net.URI;
import com.hackathon.util.FrontendUrlBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Keeps registration QR codes generated before the frontend URL fix usable.
 */
@RestController
public class RegistrationRedirectController {

    private final String frontendUrl;

    public RegistrationRedirectController(@Value("${app.frontend-url}") String frontendUrl) {
        this.frontendUrl = frontendUrl;
    }

    @GetMapping("/participants/register")
    public ResponseEntity<Void> redirectToRegistration(@RequestParam Long eventId) {
        URI destination = URI.create(FrontendUrlBuilder.build(
                frontendUrl, "/participants/register?eventId=" + eventId));
        return redirect(destination);
    }

    /** Keeps older backend-hosted check-in QR codes working after the frontend move. */
    @GetMapping("/check-in")
    public ResponseEntity<Void> redirectToCheckIn(@RequestParam Long eventId) {
        URI destination = URI.create(FrontendUrlBuilder.build(
                frontendUrl, "/check-in?eventId=" + eventId));
        return redirect(destination);
    }

    private ResponseEntity<Void> redirect(URI destination) {
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, destination.toString())
                .build();
    }
}
