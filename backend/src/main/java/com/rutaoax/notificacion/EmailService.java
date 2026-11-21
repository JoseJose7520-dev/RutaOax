package com.rutaoax.notificacion;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender correo;
    private final NotificacionLogRepository registros;
    private final String remitente;

    public EmailService(JavaMailSender correo, NotificacionLogRepository registros,
                        @Value("${app.mail.from}") String remitente) {
        this.correo = correo;
        this.registros = registros;
        this.remitente = remitente;
    }

    /** Envía un correo sin propagar errores: el resultado siempre queda en notificaciones_log. */
    @Async
    public void enviar(String destino, String asunto, String cuerpoHtml, String evento, Long usuarioId) {
        NotificacionLog registro = new NotificacionLog();
        registro.setCanal(Canal.EMAIL);
        registro.setDestino(destino);
        registro.setEvento(evento);
        registro.setUsuarioId(usuarioId);
        try {
            MimeMessage mensaje = correo.createMimeMessage();
            MimeMessageHelper ayudante = new MimeMessageHelper(mensaje, "UTF-8");
            ayudante.setFrom(remitente);
            ayudante.setTo(destino);
            ayudante.setSubject(asunto);
            ayudante.setText(cuerpoHtml, true);
            correo.send(mensaje);
            registro.setEstado(EstadoEnvio.ENVIADO);
            registro.setDetalle("Correo entregado al servidor de salida");
        } catch (Exception e) {
            log.warn("No se pudo enviar el correo '{}' a {}: {}", evento, destino, e.getMessage());
            registro.setEstado(EstadoEnvio.FALLIDO);
            registro.setDetalle(recortar(e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
        }
        try {
            registros.save(registro);
        } catch (Exception e) {
            log.error("No se pudo guardar el registro de la notificación", e);
        }
    }

    private String recortar(String texto) {
        String limpio = texto.replace('\n', ' ').replace('\r', ' ');
        return limpio.length() > 250 ? limpio.substring(0, 250) : limpio;
    }
}
