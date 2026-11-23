package com.rutaoax.notificacion;

import java.text.Normalizer;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * SMS con la API de SMS Masivos (empresa mexicana con rutas directas a Telcel, Movistar, AT&T y Altán).
 * POST https://api.smsmasivos.com.mx/sms/send con el encabezado "apikey".
 */
@Service
public class SmsMasivosService extends ProveedorMensajes {

    private static final int MAX_SMS = 160;

    private final String url;
    private final String apiKey;
    private final boolean ladaSeparada;

    public SmsMasivosService(NotificacionLogRepository registros,
                             @Value("${app.sms.url:https://api.smsmasivos.com.mx/sms/send}") String url,
                             @Value("${app.sms.api-key:}") String apiKey,
                             @Value("${app.sms.lada-separada:false}") boolean ladaSeparada) {
        super(registros);
        this.url = url;
        this.apiKey = apiKey;
        this.ladaSeparada = ladaSeparada;
    }

    @Override
    protected Canal canal() {
        return Canal.SMS;
    }

    @Override
    protected boolean configurado() {
        return valido(apiKey);
    }

    @Override
    protected String formatoDestino(String digitos) {
        return ladaSeparada ? digitos : "52" + digitos;
    }

    /** Sin acentos ni emojis el SMS cabe en 160 caracteres (con acentos el límite baja a 70). */
    static String plano(String texto) {
        String limpio = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "")
                .replace('¡', ' ').replace('¿', ' ').replaceAll("[^\\x20-\\x7E]", "").replaceAll(" +", " ").trim();
        return limpio.length() > MAX_SMS ? limpio.substring(0, MAX_SMS - 3) + "..." : limpio;
    }

    @Override
    protected Resultado llamar(String destino, String texto, Long pedidoId) {
        String mensaje = plano(texto);
        String cuerpo = "{\"numbers\":\"" + destino + "\",\"message\":\"" + json(mensaje) + "\""
                + (ladaSeparada ? ",\"country_code\":\"52\"" : "") + "}";
        Object[] r = postJson(url, Map.of("apikey", apiKey), cuerpo);
        int codigo = (int) r[0];
        String respuesta = (String) r[1];
        if (codigo == 0) {
            return new Resultado(EstadoEnvio.FALLIDO, respuesta);
        }
        if (codigo / 100 == 2 && !"false".equals(extraer(respuesta, "success"))) {
            String id = extraer(respuesta, "request_id");
            return new Resultado(EstadoEnvio.ENVIADO, "Aceptado por SMS Masivos" + (id.isEmpty() ? "" : " (" + id + ")"));
        }
        String detalle = extraer(respuesta, "message");
        return new Resultado(EstadoEnvio.FALLIDO, "SMS Masivos respondió " + codigo
                + (detalle.isEmpty() ? ": " + recortar(respuesta, 180) : ": " + detalle));
    }
}
