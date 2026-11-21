package com.rutaoax.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rutaoax.common.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
public class ApiErrorWriter {

    private final ObjectMapper mapper;

    public ApiErrorWriter(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public void escribir(HttpServletRequest req, HttpServletResponse res, HttpStatus estado, String mensaje)
            throws IOException {
        ApiError error = new ApiError(LocalDateTime.now(), estado.value(), estado.getReasonPhrase(), mensaje,
                req.getRequestURI(), null);
        res.setStatus(estado.value());
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding(StandardCharsets.UTF_8.name());
        mapper.writeValue(res.getOutputStream(), error);
    }
}
