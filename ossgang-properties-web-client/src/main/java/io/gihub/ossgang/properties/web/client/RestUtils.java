package io.gihub.ossgang.properties.web.client;

import org.springframework.http.ResponseEntity;

import java.util.Optional;

public final class RestUtils {

    public static boolean isSuccessfulResponse(ResponseEntity<?> response) {
        return response.getStatusCode().is2xxSuccessful();
    }

    public static <T> boolean isSuccessfulResponse(Optional<ResponseEntity<T>> response) {
        return response.isPresent() && isSuccessfulResponse(response.get());
    }

    private RestUtils() {
        throw new UnsupportedOperationException("Only static!");
    }
}
