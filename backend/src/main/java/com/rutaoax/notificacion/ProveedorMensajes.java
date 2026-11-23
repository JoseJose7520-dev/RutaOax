package com.rutaoax.notificacion;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base para los proveedores externos (SMS y WhatsApp): arma el teléfono, hace el POST a la API,
 * nunca lanza excepciones hacia afuera y guarda cada intento en notificaciones_log.
 */
public abstract class ProveedorMensajes {

    private static final Logger log = LoggerFactory.getLogger(ProveedorMensajes.class);
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    private final NotificacionLogRepository registros;

    protected ProveedorMensajes(NotificacionLogRepository registros) {
        this.registros = registros;
    }

    protected abstract Canal canal();

    /** true si el servidor tiene las credenciales de este proveedor. */
    protected abstract boolean configurado();

    /** Convierte "9511234567" al formato que pide el proveedor; null si no parece teléfono de 10 dígitos. */
    protected abstract String formatoDestino(String digitos);

    /** Hace la llamada al proveedor. */
    protected abstract Resultado llamar(String destino, String texto, Long pedidoId);

    public Resultado enviar(String telefono, String texto, String evento, Long usuarioId, Long pedidoId) {
        NotificacionLog registro = new NotificacionLog();
        registro.setCanal(canal());
        registro.setEvento(evento);
        registro.setUsuarioId(usuarioId);
        registro.setPedidoId(pedidoId);

        String digitos = diezDigitos(telefono);
        String destino = digitos == null ? null : formatoDestino(digitos);
        registro.setDestino(recortar(destino != null ? destino
                : (telefono == null || telefono.isBlank() ? "sin teléfono" : telefono), 150));

        Resultado resultado;
        if (!configurado()) {
            resultado = new Resultado(EstadoEnvio.FALLIDO, "El proveedor de " + canal() + " no está configurado en el servidor");
        } else if (destino == null) {
            resultado = new Resultado(EstadoEnvio.FALLIDO, "El teléfono no es válido");
        } else {
            try {
                resultado = llamar(destino, texto, pedidoId);
            } catch (Exception e) {
                resultado = new Resultado(EstadoEnvio.FALLIDO, "Error al enviar: " + e.getClass().getSimpleName());
            }
        }
        if (resultado.estado() == EstadoEnvio.FALLIDO) {
            log.warn("No se pudo enviar {} '{}' a {}: {}", canal(), evento, registro.getDestino(), resultado.detalle());
        }
        registro.setEstado(resultado.estado());
        registro.setDetalle(recortar(resultado.detalle(), 250));
        try {
            registros.save(registro);
        } catch (Exception e) {
            log.error("No se pudo guardar el registro de la notificación", e);
        }
        return resultado;
    }

    /** POST con cuerpo JSON. Regresa [código HTTP, cuerpo]; si no hay conexión, código 0. */
    protected static Object[] postJson(String url, Map<String, String> encabezados, String json) {
        HttpRequest.Builder peticion = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json));
        encabezados.forEach(peticion::header);
        try {
            HttpResponse<String> r = HTTP.send(peticion.build(), HttpResponse.BodyHandlers.ofString());
            return new Object[]{r.statusCode(), r.body()};
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new Object[]{0, "envío interrumpido"};
        } catch (Exception e) {
            return new Object[]{0, "sin conexión con el proveedor (" + e.getClass().getSimpleName() + ")"};
        }
    }

    /** "951 123 4567", "+52 951..." o "52 1 951..." → "9511234567". null si no quedan 10 dígitos. */
    static String diezDigitos(String telefono) {
        if (telefono == null) {
            return null;
        }
        String d = telefono.replaceAll("\\D", "");
        if (d.length() == 13 && d.startsWith("521")) {
            d = d.substring(3);
        } else if (d.length() == 12 && d.startsWith("52")) {
            d = d.substring(2);
        }
        return d.length() == 10 ? d : null;
    }

    /** Saca un campo simple de un JSON sin depender de un parser ("message", "code", "id"...). */
    static String extraer(String json, String campo) {
        if (json == null) {
            return "";
        }
        Matcher m = Pattern.compile("\"" + campo + "\"\\s*:\\s*(\"((?:[^\"\\\\]|\\\\.)*)\"|(-?\\d+)|(true|false))")
                .matcher(json);
        if (!m.find()) {
            return "";
        }
        return m.group(2) != null ? m.group(2) : (m.group(3) != null ? m.group(3) : m.group(4));
    }

    /** Escapa texto para meterlo entre comillas en un JSON. */
    static String json(String valor) {
        if (valor == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (char c : valor.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n', '\r' -> sb.append(' ');
                default -> {
                    if (c < 0x20) {
                        sb.append(' ');
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }

    static boolean valido(String valor) {
        return valor != null && !valor.isBlank() && !valor.contains("CAMBIAR");
    }

    static String recortar(String texto, int max) {
        if (texto == null) {
            return null;
        }
        String limpio = texto.replace('\n', ' ').replace('\r', ' ');
        return limpio.length() > max ? limpio.substring(0, max) : limpio;
    }

    public record Resultado(EstadoEnvio estado, String detalle) {
    }
}
