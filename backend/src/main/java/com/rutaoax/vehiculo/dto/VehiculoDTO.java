package com.rutaoax.vehiculo.dto;

import com.rutaoax.pedido.dto.NegocioRefDTO;
import com.rutaoax.vehiculo.EstadoVehiculo;
import com.rutaoax.vehiculo.TipoVehiculo;
import com.rutaoax.vehiculo.Vehiculo;
import java.time.LocalDate;

public record VehiculoDTO(Long id, TipoVehiculo tipo, String placa, int capacidad, EstadoVehiculo estado,
                          LocalDate seguroVence, boolean enUsoHoy, String repartidorHoy,
                          NegocioRefDTO negocio) {

    public static VehiculoDTO from(Vehiculo v, String repartidorHoy, boolean enUsoHoy) {
        return new VehiculoDTO(v.getId(), v.getTipo(), v.getPlaca(), v.getCapacidad(), v.getEstado(),
                v.getSeguroVence(), enUsoHoy, repartidorHoy,
                new NegocioRefDTO(v.getNegocio().getId(), v.getNegocio().getNombre()));
    }
}
