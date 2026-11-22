package com.rutaoax.notificacion;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Elige el proveedor de cada canal según la configuración del servidor, sin tocar código:
 *   app.notificaciones.sms=twilio | smsmasivos
 *   app.notificaciones.whatsapp=twilio | meta
 */
@Component
public class Mensajeros {

    private final ProveedorMensajes sms;
    private final ProveedorMensajes whatsapp;

    public Mensajeros(TwilioSmsService twilioSms, SmsMasivosService smsMasivos,
                      TwilioWhatsAppService twilioWhatsApp, WhatsAppCloudService meta,
                      @Value("${app.notificaciones.sms:twilio}") String proveedorSms,
                      @Value("${app.notificaciones.whatsapp:twilio}") String proveedorWhatsApp) {
        this.sms = "smsmasivos".equalsIgnoreCase(proveedorSms) ? smsMasivos : twilioSms;
        this.whatsapp = "meta".equalsIgnoreCase(proveedorWhatsApp) ? meta : twilioWhatsApp;
    }

    public ProveedorMensajes sms() {
        return sms;
    }

    public ProveedorMensajes whatsapp() {
        return whatsapp;
    }
}
