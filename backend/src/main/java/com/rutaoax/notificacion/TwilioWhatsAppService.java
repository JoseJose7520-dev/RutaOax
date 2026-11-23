package com.rutaoax.notificacion;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * WhatsApp con Twilio. La cuenta de prueba solo acepta plantillas aprobadas: si se configura
 * app.twilio.whatsapp-content-sid (empieza con HX) se manda esa plantilla; si no, texto libre.
 */
@Service
public class TwilioWhatsAppService extends ProveedorMensajes {

    private final TwilioCliente twilio;
    private final String remitente;
    private final String plantilla;
    private final String prefijo;

    public TwilioWhatsAppService(NotificacionLogRepository registros, TwilioCliente twilio,
                                 @Value("${app.twilio.whatsapp-from:}") String remitente,
                                 @Value("${app.twilio.whatsapp-content-sid:}") String plantilla,
                                 @Value("${app.twilio.whatsapp-prefijo:+521}") String prefijo) {
        super(registros);
        this.twilio = twilio;
        this.remitente = remitente;
        this.plantilla = plantilla;
        this.prefijo = prefijo;
    }

    @Override
    protected Canal canal() {
        return Canal.WHATSAPP;
    }

    @Override
    protected boolean configurado() {
        return twilio.configurado() && valido(remitente);
    }

    @Override
    protected String formatoDestino(String digitos) {
        return "whatsapp:" + prefijo + digitos;
    }

    @Override
    protected Resultado llamar(String destino, String texto, Long pedidoId) {
        String de = remitente.startsWith("whatsapp:") ? remitente : "whatsapp:" + remitente;
        String contenido = valido(plantilla)
                ? "&ContentSid=" + TwilioCliente.codificar(plantilla)
                : "&Body=" + TwilioCliente.codificar(texto);
        return twilio.enviar(destino, de, contenido);
    }
}
