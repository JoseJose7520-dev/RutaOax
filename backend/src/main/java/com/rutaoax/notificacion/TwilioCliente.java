package com.rutaoax.notificacion;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Llamada a la API REST de Twilio (POST /Messages.json). La usan el SMS y el WhatsApp de Twilio. */
@Component
public class TwilioCliente {

    private static final String API = "https://api.twilio.com/2010-04-01/Accounts/%s/Messages.json";
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    private final String accountSid;
    private final String authToken;

    public TwilioCliente(@Value("${app.twilio.account-sid:}") String accountSid,
                         @Value("${app.twilio.auth-token:}") String authToken) {
        this.accountSid = accountSid;
        this.authToken = authToken;
    }

    public boolean configurado() {
        return ProveedorMensajes.valido(accountSid) && ProveedorMensajes.valido(authToken);
    }

    /** contenido: "&Body=..." o "&ContentSid=...". */
    public ProveedorMensajes.Resultado enviar(String para, String de, String contenido) {
        String cuerpo = "To=" + codificar(para) + "&From=" + codificar(de) + contenido;
        String credenciales = Base64.getEncoder()
                .encodeToString((accountSid + ":" + authToken).getBytes(StandardCharsets.UTF_8));
        HttpRequest peticion = HttpRequest.newBuilder(URI.create(String.format(API, accountSid)))
                .timeout(Duration.ofSeconds(15))
                .header("Authorization", "Basic " + credenciales)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(cuerpo))
                .build();
        try {
            HttpResponse<String> r = HTTP.send(peticion, HttpResponse.BodyHandlers.ofString());
            if (r.statusCode() / 100 == 2) {
                return new ProveedorMensajes.Resultado(EstadoEnvio.ENVIADO,
                        "Aceptado por Twilio (" + ProveedorMensajes.extraer(r.body(), "sid") + ")");
            }
            String mensaje = ProveedorMensajes.extraer(r.body(), "message");
            String codigo = ProveedorMensajes.extraer(r.body(), "code");
            return new ProveedorMensajes.Resultado(EstadoEnvio.FALLIDO, "Twilio respondió " + r.statusCode()
                    + (codigo.isEmpty() ? "" : " (código " + codigo + ")") + (mensaje.isEmpty() ? "" : ": " + mensaje));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new ProveedorMensajes.Resultado(EstadoEnvio.FALLIDO, "Envío interrumpido");
        } catch (Exception e) {
            return new ProveedorMensajes.Resultado(EstadoEnvio.FALLIDO, "Sin conexión con Twilio: " + e.getClass().getSimpleName());
        }
    }

    static String codificar(String valor) {
        return URLEncoder.encode(valor, StandardCharsets.UTF_8);
    }
}
