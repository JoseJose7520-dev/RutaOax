package com.rutaoax.vehiculo.dto;

import com.rutaoax.vehiculo.TipoVehiculo;

public record VehiculoDisponibleDTO(Long id, TipoVehiculo tipo, String placa, int capacidad, int cupoLibre) {
}
