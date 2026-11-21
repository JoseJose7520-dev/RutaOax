package com.rutaoax.common;

import java.util.List;
import java.util.Random;

public final class ZonaCatalogo {

    private static final List<Zona> ZONAS = List.of(
            new Zona("Centro Histórico", 17.0609, -96.7253),
            new Zona("Jalatlaco", 17.0663, -96.7176),
            new Zona("Colonia Reforma", 17.0745, -96.7160),
            new Zona("Xochimilco", 17.0720, -96.7270),
            new Zona("Colonia América", 17.0700, -96.7300),
            new Zona("Colonia Volcanes", 17.0835, -96.7100),
            new Zona("Cinco Señores", 17.0415, -96.7081),
            new Zona("Santa Lucía del Camino", 17.0707, -96.6906),
            new Zona("Santa Cruz Xoxocotlán", 17.0242, -96.7370),
            new Zona("San Felipe del Agua", 17.1020, -96.7180));

    private ZonaCatalogo() {
    }

    public static List<Zona> todas() {
        return ZONAS;
    }

    public static boolean existe(String nombre) {
        return ZONAS.stream().anyMatch(z -> z.nombre().equals(nombre));
    }

    public static double[] coordenadas(String zona, long semilla) {
        Zona z = ZONAS.stream().filter(x -> x.nombre().equals(zona)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Zona desconocida: " + zona));
        Random azar = new Random(semilla);
        double lat = z.lat() + (azar.nextDouble() - 0.5) * 0.008;
        double lng = z.lng() + (azar.nextDouble() - 0.5) * 0.008;
        return new double[]{lat, lng};
    }
}
