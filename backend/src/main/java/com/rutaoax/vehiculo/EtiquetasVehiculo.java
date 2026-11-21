package com.rutaoax.vehiculo;

public final class EtiquetasVehiculo {

    private EtiquetasVehiculo() {
    }

    public static String etiqueta(TipoVehiculo tipo) {
        return switch (tipo) {
            case COMBI -> "Combi";
            case MOTOCICLETA -> "Motocicleta";
            case CAMIONETA -> "Camioneta";
            case BICICLETA -> "Bicicleta";
            case AUTOMOVIL -> "Automóvil";
        };
    }
}
