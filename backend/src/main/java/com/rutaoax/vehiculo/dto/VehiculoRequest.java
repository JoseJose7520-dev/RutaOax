package com.rutaoax.vehiculo.dto;

import com.rutaoax.vehiculo.EstadoVehiculo;
import com.rutaoax.vehiculo.TipoVehiculo;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

public record VehiculoRequest(
        @NotNull(message = "Elige el tipo de vehículo") TipoVehiculo tipo,
        @NotBlank(message = "La placa es obligatoria")
        @Pattern(regexp = "^[A-Za-z0-9-]{3,20}$", message = "La placa solo puede tener letras, números y guiones (3 a 20 caracteres)") String placa,
        @NotNull(message = "La capacidad es obligatoria")
        @Min(value = 1, message = "La capacidad debe ser al menos 1")
        @Max(value = 999, message = "La capacidad no puede ser mayor a 999") Integer capacidad,
        @NotNull(message = "Elige el estado del vehículo") EstadoVehiculo estado,
        LocalDate seguroVence) {
}
