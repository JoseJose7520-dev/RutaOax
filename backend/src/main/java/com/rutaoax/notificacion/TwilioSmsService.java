package com.rutaoax.notificacion;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** SMS con Twilio (texto libre). En la cuenta de prueba solo llega a números verificados. */
@Service
public class TwilioSmsService extends ProveedorMensajes {

    private final TwilioCliente twilio;
    private final String remitente;

    public TwilioSmsService(NotificacionLogRepository registros, TwilioCliente twilio,
                            @Value("${app.twilio.sms-from:}") String remitente) {
        super(registros);
        this.twilio = twilio;
        this.remitente = remitente;
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
        return twilio.enviar(destino, remitente, "&Body=" + TwilioCliente.codificar(SmsMasivosService.plano(texto)));
    }
}
