package com.rutaoax.notificacion;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * SMS con Twilio. En la cuenta de prueba solo llega a números verificados y solo acepta plantillas
 * predefinidas (error 572006 con texto libre): con app.twilio.sms-plantilla=sms_delivery_updates
 * se manda esa plantilla; si está vacío, se manda el texto de RutaOax (cuenta de pago).
 */
@Service
public class TwilioSmsService extends ProveedorMensajes {

    private final TwilioCliente twilio;
    private final String remitente;
    private final String plantilla;

    public TwilioSmsService(NotificacionLogRepository registros, TwilioCliente twilio,
                            @Value("${app.twilio.sms-from:}") String remitente,
                            @Value("${app.twilio.sms-plantilla:}") String plantilla) {
        super(registros);
        this.twilio = twilio;
        this.remitente = remitente;
        this.plantilla = plantilla;
    }

    @Override
    protected Canal canal() {
        return Canal.SMS;
    }

    @Override
    protected boolean configurado() {
        return twilio.configurado() && valido(remitente);
    }

    @Override
    protected String formatoDestino(String digitos) {
        return "+52" + digitos;
    }

    @Override
    protected Resultado llamar(String destino, String texto, Long pedidoId) {
        String cuerpo = valido(plantilla) ? plantilla.trim() : SmsMasivosService.plano(texto);
        return twilio.enviar(destino, remitente, "&Body=" + TwilioCliente.codificar(cuerpo));
    }
}
