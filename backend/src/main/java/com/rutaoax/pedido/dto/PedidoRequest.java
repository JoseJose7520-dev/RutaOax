package com.rutaoax.pedido.dto;

import com.rutaoax.common.ZonaCatalogo;
import com.rutaoax.pedido.Modalidad;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record PedidoRequest(
        @NotBlank(message = "El nombre del cliente es obligatorio")
        @Size(max = 100, message = "El nombre del cliente es demasiado largo") String clienteNombre,
        @Pattern(regexp = "^(\\d{10})?$", message = "El teléfono debe tener 10 dígitos") String clienteTelefono,
        @Email(message = "El correo no tiene un formato válido") String clienteEmail,
        @NotBlank(message = "La dirección es obligatoria")
        @Size(max = 200, message = "La dirección es demasiado larga") String direccion,
        @NotBlank(message = "Elige una zona de entrega") String zona,
        @NotNull(message = "El número de artículos es obligatorio")
        @Min(value = 1, message = "Debe haber al menos 1 artículo") Integer articulos,
        @NotNull(message = "El total es obligatorio")
        @DecimalMin(value = "0.00", message = "El total no puede ser negativo")
        @Digits(integer = 8, fraction = 2, message = "El total debe tener máximo 2 decimales") BigDecimal total,
        @Size(max = 255, message = "Las notas son demasiado largas") String notas,
        @Size(max = 150, message = "El origen es demasiado largo") String origen,
        @DecimalMin(value = "0.00", message = "El pago no puede ser negativo")
        @Digits(integer = 6, fraction = 2, message = "El pago debe tener máximo 2 decimales") BigDecimal pagoRepartidor,
        Modalidad modalidad) {

    @AssertTrue(message = "Elige una zona de entrega válida")
    public boolean isZonaValido() {
        return zona == null || zona.isBlank() || ZonaCatalogo.existe(zona);
    }
}
