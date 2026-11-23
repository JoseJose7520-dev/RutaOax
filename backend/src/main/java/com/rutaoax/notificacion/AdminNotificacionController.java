package com.rutaoax.notificacion;

import com.rutaoax.common.PageResponse;
import com.rutaoax.notificacion.dto.NotificacionDTO;
import com.rutaoax.notificacion.dto.PruebaNotificacionDTO;
import com.rutaoax.notificacion.dto.PruebaNotificacionRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Historial de avisos enviados y envío de prueba para comprobar la configuración (solo ADMIN). */
@RestController
@RequestMapping("/api/admin/notificaciones")
@PreAuthorize("hasRole('ADMIN')")
public class AdminNotificacionController {

    private final NotificacionLogRepository registros;
    private final Mensajeros mensajeros;
    private final EmailService correo;

    public AdminNotificacionController(NotificacionLogRepository registros, Mensajeros mensajeros, EmailService correo) {
        this.registros = registros;
        this.mensajeros = mensajeros;
        this.correo = correo;
    }

    @GetMapping
    public PageResponse<NotificacionDTO> listar(@RequestParam(required = false) Canal canal,
                                                @RequestParam(required = false) EstadoEnvio estado,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "10") int size) {
        PageRequest pagina = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50),
                Sort.by("creadoEn").descending().and(Sort.by("id").descending()));
        return PageResponse.from(registros.buscar(canal, estado, pagina).map(NotificacionDTO::from));
    }

    @PostMapping("/prueba")
    public PruebaNotificacionDTO prueba(@Valid @RequestBody PruebaNotificacionRequest req) {
        Canal canal = Canal.valueOf(req.canal());
        if (canal == Canal.EMAIL) {
            correo.enviar(req.destino(), "Prueba de correo RutaOax",
                    "<p>Este es un correo de prueba enviado desde el panel de administración de RutaOax.</p>",
                    "PRUEBA", null);
            return new PruebaNotificacionDTO(EstadoEnvio.ENVIADO, "Correo puesto en cola; revisa el historial en unos segundos");
        }
        String texto = "RutaOax: mensaje de prueba enviado desde el panel de administracion.";
        ProveedorMensajes proveedor = canal == Canal.SMS ? mensajeros.sms() : mensajeros.whatsapp();
        ProveedorMensajes.Resultado r = proveedor.enviar(req.destino(), texto, "PRUEBA", null, null);
        return new PruebaNotificacionDTO(r.estado(), r.detalle());
    }
}
