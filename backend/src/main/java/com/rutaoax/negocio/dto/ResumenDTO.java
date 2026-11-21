package com.rutaoax.negocio.dto;

import java.util.List;

public record ResumenDTO(long sinAsignar, long enCamino, long entregadosHoy, long mensajesNuevos,
                         List<RepartidorHoyDTO> repartidoresHoy, List<AtencionDTO> atencion) {
}
