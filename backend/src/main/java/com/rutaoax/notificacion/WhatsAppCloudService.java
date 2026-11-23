package com.rutaoax.notificacion;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * WhatsApp con la API oficial de Meta (WhatsApp Cloud API).
 * POST https://graph.facebook.com/{version}/{phone-number-id}/messages con token Bearer.
 *  - modo "plantilla": manda una plantilla aprobada (sirve siempre; con el número de prueba, "hello_world").
 *    Si la plantilla tiene variables, app.whatsapp.parametros=2 manda {{1}} = pedido y {{2}} = el aviso.
 *  - modo "texto": texto libre; WhatsApp solo lo permite 24 h después de que la persona le escribió al número.
 */
@Service
public class WhatsAppCloudService extends ProveedorMensajes {

    private final String version;
    private final String phoneNumberId;
    private final String token;
    private final String modo;
    private final String plantilla;
    private final String idioma;
    private final int parametros;
    private final String prefijo;

    public WhatsAppCloudService(NotificacionLogRepository registros,
                                @Value("${app.whatsapp.api-version:v21.0}") String version,
                                @Value("${app.whatsapp.phone-number-id:}") String phoneNumberId,
                                @Value("${app.whatsapp.token:}") String token,
                                @Value("${app.whatsapp.modo:plantilla}") String modo,
                                @Value("${app.whatsapp.plantilla:hello_world}") String plantilla,
                                @Value("${app.whatsapp.idioma:en_US}") String idioma,
                                @Value("${app.whatsapp.parametros:0}") int parametros,
                                @Value("${app.whatsapp.prefijo:52}") String prefijo) {
        super(registros);
        this.version = version;
        this.phoneNumberId = phoneNumberId;
        this.token = token;
        this.modo = modo;
        this.plantilla = plantilla;
        this.idioma = idioma;
        this.parametros = parametros;
        this.prefijo = prefijo;
    }

    @Override
    protected Canal canal() {
        return Canal.WHATSAPP;
    }

    @Override
    protected boolean configurado() {
        return valido(phoneNumberId) && valido(token);
    }

    @Override
    protected String formatoDestino(String digitos) {
        return prefijo + digitos;
    }

    @Override
    protected Resultado llamar(String destino, String texto, Long pedidoId) {
        String url = "https://graph.facebook.com/" + version + "/" + phoneNumberId + "/messages";
        Object[] r = postJson(url, Map.of("Authorization", "Bearer " + token), cuerpo(destino, texto, pedidoId));
        int codigo = (int) r[0];
        String respuesta = (String) r[1];
        if (codigo == 0) {
            return new Resultado(EstadoEnvio.FALLIDO, respuesta);
        }
        if (codigo / 100 == 2) {
            String id = extraer(respuesta, "id");
            return new Resultado(EstadoEnvio.ENVIADO, "Aceptado por WhatsApp" + (id.isEmpty() ? "" : " (" + recortar(id, 60) + ")"));
        }
        String mensaje = extraer(respuesta, "message");
        String error = extraer(respuesta, "code");
        return new Resultado(EstadoEnvio.FALLIDO, "WhatsApp respondió " + codigo
                + (error.isEmpty() ? "" : " (código " + error + ")") + (mensaje.isEmpty() ? "" : ": " + mensaje));
    }

    String cuerpo(String destino, String texto, Long pedidoId) {
        String inicio = "{\"messaging_product\":\"whatsapp\",\"recipient_type\":\"individual\",\"to\":\"" + destino + "\",";
        if ("texto".equalsIgnoreCase(modo)) {
            return inicio + "\"type\":\"text\",\"text\":{\"body\":\"" + json(texto) + "\"}}";
        }
        StringBuilder sb = new StringBuilder(inicio)
                .append("\"type\":\"template\",\"template\":{\"name\":\"").append(json(plantilla))
                .append("\",\"language\":{\"code\":\"").append(json(idioma)).append("\"}");
        if (parametros > 0) {
            String[] valores = {pedidoId == null ? "prueba" : "#" + pedidoId, texto};
            sb.append(",\"components\":[{\"type\":\"body\",\"parameters\":[");
            for (int i = 0; i < Math.min(parametros, valores.length); i++) {
                if (i > 0) {
                    sb.append(',');
                }
                sb.append("{\"type\":\"text\",\"text\":\"").append(json(valores[i])).append("\"}");
            }
            sb.append("]}]");
        }
        return sb.append("}}").toString();
    }
}
