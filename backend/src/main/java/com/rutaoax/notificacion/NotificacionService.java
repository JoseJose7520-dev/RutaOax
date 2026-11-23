package com.rutaoax.notificacion;

import com.rutaoax.pedido.Pedido;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Punto único para avisar al cliente (HU-25). Cada aviso se manda en segundo plano para no
 * frenar al repartidor, y cada intento queda en notificaciones_log (ENVIADO o FALLIDO).
 *  - Recorrido iniciado: SMS "tu pedido va en camino".
 *  - Pedido entregado: WhatsApp de confirmación (y correo si el cliente tiene cuenta).
 *  - No entregado: WhatsApp con el motivo.
 */
@Service
public class NotificacionService {

    private final Mensajeros mensajeros;
    private final EmailService correo;
    private final String frontendUrl;

    public NotificacionService(Mensajeros mensajeros, EmailService correo,
                               @Value("${app.frontend-url}") String frontendUrl) {
        this.mensajeros = mensajeros;
        this.correo = correo;
        this.frontendUrl = frontendUrl;
    }

    @Async
    public void pedidoEnCamino(Long pedidoId, String telefono, String negocio, String repartidor, Long clienteId) {
        String texto = "RutaOax: tu pedido #" + pedidoId + " ya va en camino con " + repartidor
                + ". Siguelo en " + frontendUrl + "/cliente";
        mensajeros.sms().enviar(telefono, texto, "RECORRIDO_INICIADO", clienteId, pedidoId);
    }

    @Async
    public void pedidoEntregado(Long pedidoId, String telefono, String negocio,
                                Long clienteId, String clienteEmail, String clienteNombre) {
        String texto = "RutaOax: tu pedido #" + pedidoId + (negocio != null ? " de " + negocio : "")
                + " fue entregado. ¡Gracias por tu preferencia!";
        mensajeros.whatsapp().enviar(telefono, texto, "PEDIDO_ENTREGADO", clienteId, pedidoId);
        if (clienteEmail != null && !clienteEmail.isBlank()) {
            correo.enviar(clienteEmail, "Tu pedido #" + pedidoId + " fue entregado",
                    "<p>Hola " + (clienteNombre != null ? clienteNombre : "") + ",</p><p>Tu pedido <strong>#" + pedidoId
                            + "</strong>" + (negocio != null ? " de " + negocio : "") + " fue entregado.</p>"
                            + "<p>Puedes ver el detalle en <a href=\"" + frontendUrl + "/cliente?pedido=" + pedidoId
                            + "\">RutaOax</a>.</p>",
                    "PEDIDO_ENTREGADO", clienteId);
        }
    }

    @Async
    public void pedidoNoEntregado(Long pedidoId, String telefono, String negocio, String motivo, Long clienteId) {
        String texto = "RutaOax: no pudimos entregar tu pedido #" + pedidoId + (negocio != null ? " de " + negocio : "")
                + (motivo != null && !motivo.isBlank() ? ". Motivo: " + motivo.trim() : "")
                + ". El negocio se pondrá en contacto contigo.";
        mensajeros.whatsapp().enviar(telefono, texto, "PEDIDO_NO_ENTREGADO", clienteId, pedidoId);
    }

    /** Los avisos reciben datos sueltos (no entidades) porque corren en otro hilo, fuera de la transacción. */
    public static String nombreNegocio(Pedido p) {
        return p.getNegocio() != null ? p.getNegocio().getNombre() : null;
    }
}
