package com.rutaoax.negocio.dto;

/** vehiculoId y paradas vienen llenos cuando el repartidor ya tiene una ruta planeada ese día. */
public record RepartidorDisponibleDTO(Long id, String nombre, Long vehiculoId, int paradas) {
}
