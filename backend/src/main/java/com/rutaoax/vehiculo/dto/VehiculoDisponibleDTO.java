package com.rutaoax.vehiculo.dto;

import com.rutaoax.vehiculo.TipoVehiculo;

/** repartidorId viene lleno cuando el vehículo ya está en una ruta planeada ese día (solo puede usarlo ese repartidor). */
public record VehiculoDisponibleDTO(Long id, TipoVehiculo tipo, String placa, int capacidad, int cupoLibre,
                                    Long repartidorId) {
}
