package com.rutaoax.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> badRequest(BadRequestException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.BAD_REQUEST, ex.getMessage(), req, null);
    }

    @ExceptionHandler({UnauthorizedException.class, AuthenticationException.class})
    public ResponseEntity<ApiError> noAutenticado(RuntimeException ex, HttpServletRequest req) {
        String mensaje = ex instanceof UnauthorizedException ? ex.getMessage() : "Debes iniciar sesión";
        return respuesta(HttpStatus.UNAUTHORIZED, mensaje, req, null);
    }

    @ExceptionHandler({ForbiddenException.class, AccessDeniedException.class})
    public ResponseEntity<ApiError> prohibido(RuntimeException ex, HttpServletRequest req) {
        String mensaje = ex instanceof ForbiddenException ? ex.getMessage() : "No tienes permiso para esta acción";
        return respuesta(HttpStatus.FORBIDDEN, mensaje, req, null);
    }

    @ExceptionHandler({NotFoundException.class, NoResourceFoundException.class, NoHandlerFoundException.class})
    public ResponseEntity<ApiError> noEncontrado(Exception ex, HttpServletRequest req) {
        String mensaje = ex instanceof NotFoundException ? ex.getMessage() : "El recurso solicitado no existe";
        return respuesta(HttpStatus.NOT_FOUND, mensaje, req, null);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> conflicto(ConflictException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.CONFLICT, ex.getMessage(), req, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validacion(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(fe -> errores.putIfAbsent(campo(fe.getField()), fe.getDefaultMessage()));
        ex.getBindingResult().getGlobalErrors()
                .forEach(ge -> errores.putIfAbsent(ge.getObjectName(), ge.getDefaultMessage()));
        return respuesta(HttpStatus.UNPROCESSABLE_ENTITY, "Hay campos inválidos", req, errores);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> restricciones(ConstraintViolationException ex, HttpServletRequest req) {
        Map<String, String> errores = new LinkedHashMap<>();
        for (ConstraintViolation<?> v : ex.getConstraintViolations()) {
            String ruta = v.getPropertyPath().toString();
            String campo = ruta.contains(".") ? ruta.substring(ruta.lastIndexOf('.') + 1) : ruta;
            errores.putIfAbsent(campo(campo), v.getMessage());
        }
        return respuesta(HttpStatus.UNPROCESSABLE_ENTITY, "Hay campos inválidos", req, errores);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class})
    public ResponseEntity<ApiError> malaPeticion(Exception ex, HttpServletRequest req) {
        return respuesta(HttpStatus.BAD_REQUEST, "La petición no tiene el formato esperado", req, null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> metodoNoPermitido(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.METHOD_NOT_ALLOWED, "Método no permitido para esta ruta", req, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> inesperado(Exception ex, HttpServletRequest req) {
        log.error("Error inesperado en {}", req.getRequestURI(), ex);
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error inesperado. Intenta de nuevo", req, null);
    }

    // Los métodos @AssertTrue llamados isXxxValido reportan su error en el campo "xxx".
    private static String campo(String nombre) {
        return nombre.endsWith("Valido") ? nombre.substring(0, nombre.length() - "Valido".length()) : nombre;
    }

    private ResponseEntity<ApiError> respuesta(HttpStatus estado, String mensaje, HttpServletRequest req,
                                               Map<String, String> errores) {
        ApiError cuerpo = new ApiError(LocalDateTime.now(), estado.value(), estado.getReasonPhrase(), mensaje,
                req.getRequestURI(), errores);
        return ResponseEntity.status(estado).body(cuerpo);
    }
}
