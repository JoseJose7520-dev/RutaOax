package com.rutaoax.config;

import com.rutaoax.common.ZonaCatalogo;
import com.rutaoax.mensaje.Mensaje;
import com.rutaoax.mensaje.MensajeRepository;
import com.rutaoax.negocio.Negocio;
import com.rutaoax.negocio.NegocioRepository;
import com.rutaoax.negocio.Repartidor;
import com.rutaoax.negocio.RepartidorRepository;
import com.rutaoax.pedido.EstadoPedido;
import com.rutaoax.pedido.Modalidad;
import com.rutaoax.pedido.Pedido;
import com.rutaoax.pedido.PedidoRepository;
import com.rutaoax.recorrido.EstadoRecorrido;
import com.rutaoax.recorrido.Recorrido;
import com.rutaoax.recorrido.RecorridoRepository;
import com.rutaoax.usuario.Usuario;
import com.rutaoax.usuario.UsuarioRepository;
import com.rutaoax.vehiculo.EstadoVehiculo;
import com.rutaoax.vehiculo.TipoVehiculo;
import com.rutaoax.vehiculo.Vehiculo;
import com.rutaoax.vehiculo.VehiculoRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(2)
public class SeederOperacion implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SeederOperacion.class);

    private final UsuarioRepository usuarios;
    private final NegocioRepository negocios;
    private final RepartidorRepository repartidores;
    private final VehiculoRepository vehiculos;
    private final RecorridoRepository recorridos;
    private final PedidoRepository pedidos;
    private final MensajeRepository mensajes;

    private int semilla = 0;

    public SeederOperacion(UsuarioRepository usuarios, NegocioRepository negocios, RepartidorRepository repartidores,
                           VehiculoRepository vehiculos, RecorridoRepository recorridos, PedidoRepository pedidos,
                           MensajeRepository mensajes) {
        this.usuarios = usuarios;
        this.negocios = negocios;
        this.repartidores = repartidores;
        this.vehiculos = vehiculos;
        this.recorridos = recorridos;
        this.pedidos = pedidos;
        this.mensajes = mensajes;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (vehiculos.count() > 0 || usuarios.count() == 0) {
            return;
        }
        LocalDate hoy = LocalDate.now();
        LocalDate ayer = hoy.minusDays(1);

        Usuario marta = usuario("negocio1@rutaoax.com");
        Usuario ind1 = usuario("independiente1@rutaoax.com");
        Usuario c1 = usuario("cliente1@rutaoax.com");
        Usuario c2 = usuario("cliente2@rutaoax.com");
        Usuario c3 = usuario("cliente3@rutaoax.com");
        Usuario c4 = usuario("cliente4@rutaoax.com");
        Negocio dulces = negocios.findByPropietarioId(marta.getId()).orElseThrow();
        Negocio espiga = negocios.findByPropietarioId(usuario("negocio2@rutaoax.com").getId()).orElseThrow();
        Repartidor rep1 = repartidor("repartidor1@rutaoax.com");
        Repartidor rep2 = repartidor("repartidor2@rutaoax.com");
        Repartidor rep4 = repartidor("repartidor4@rutaoax.com");
        Repartidor indep1 = repartidor("independiente1@rutaoax.com");

        Vehiculo moto214 = vehiculo(dulces, TipoVehiculo.MOTOCICLETA, "OAX-214", 10, EstadoVehiculo.ACTIVO, hoy.plusDays(200));
        Vehiculo camioneta880 = vehiculo(dulces, TipoVehiculo.CAMIONETA, "OAX-880", 30, EstadoVehiculo.ACTIVO, hoy.plusDays(5));
        vehiculo(dulces, TipoVehiculo.BICICLETA, "BIC-03", 4, EstadoVehiculo.ACTIVO, null);
        vehiculo(dulces, TipoVehiculo.MOTOCICLETA, "OAX-301", 10, EstadoVehiculo.ACTIVO, hoy.plusDays(120));
        vehiculo(dulces, TipoVehiculo.COMBI, "OAX-502", 40, EstadoVehiculo.MANTENIMIENTO, hoy.plusDays(90));
        vehiculo(dulces, TipoVehiculo.AUTOMOVIL, "OAX-777", 15, EstadoVehiculo.ACTIVO, hoy.plusDays(300));
        Vehiculo moto120 = vehiculo(espiga, TipoVehiculo.MOTOCICLETA, "OAX-120", 8, EstadoVehiculo.ACTIVO, hoy.plusDays(150));
        vehiculo(espiga, TipoVehiculo.CAMIONETA, "OAX-455", 25, EstadoVehiculo.ACTIVO, hoy.plusDays(210));
        vehiculo(espiga, TipoVehiculo.BICICLETA, "BIC-11", 4, EstadoVehiculo.ACTIVO, null);
        vehiculo(espiga, TipoVehiculo.AUTOMOVIL, "OAX-908", 12, EstadoVehiculo.ACTIVO, hoy.plusDays(60));

        Recorrido r1 = recorrido(rep1, moto214, hoy, EstadoRecorrido.EN_CURSO);
        Recorrido r2 = recorrido(rep2, camioneta880, hoy, EstadoRecorrido.PLANEADO);
        recorrido(rep4, moto120, hoy, EstadoRecorrido.PLANEADO);
        Recorrido r4 = recorrido(indep1, null, hoy, EstadoRecorrido.PLANEADO);
        Recorrido r5 = recorrido(rep1, moto214, ayer, EstadoRecorrido.FINALIZADO);
        Recorrido r6 = recorrido(rep2, camioneta880, ayer, EstadoRecorrido.FINALIZADO);

        pedido(null, c1, "Carla Ríos", "9515550109", "Calle Hidalgo 22", "Centro Histórico", 3, "420.00", Modalidad.ABIERTO, EstadoPedido.PENDIENTE, null, null, "Mercado 20 de Noviembre", "40.00");
        pedido(null, c2, "Mario Cruz", "9515550110", "Av. Universidad 310", "Xochimilco", 1, "180.00", Modalidad.ABIERTO, EstadoPedido.PENDIENTE, null, null, "Farmacia Centro", "55.00");
        pedido(dulces, null, "Lucía Vera", "9515550120", "Calle Murguía 8", "Jalatlaco", 2, "260.00", Modalidad.ABIERTO, EstadoPedido.PENDIENTE, null, null, "Dulces del Valle", "45.00");
        pedido(dulces, c3, "Elena Gómez", "9515550111", "Calle Reforma 71", "Colonia Reforma", 1, "150.00", Modalidad.ABIERTO, EstadoPedido.PENDIENTE, null, null, "Dulces del Valle", "50.00");
        pedido(dulces, null, "Sofía León", "9515550121", "Av. Juárez 105", "Centro Histórico", 4, "540.00", Modalidad.NEGOCIO, EstadoPedido.PENDIENTE, null, null, null, null);
        pedido(dulces, null, "Tomás Aguilar", "9515550122", "Calle Alcalá 330", "Centro Histórico", 2, "310.00", Modalidad.NEGOCIO, EstadoPedido.PENDIENTE, null, null, null, null);
        pedido(espiga, null, "Beatriz Núñez", "9515550123", "Av. Independencia 801", "Colonia América", 5, "625.00", Modalidad.NEGOCIO, EstadoPedido.PENDIENTE, null, null, null, null);
        Pedido p8 = pedido(dulces, c3, "Elena Gómez", "9515550111", "Calle Murguía 8", "Jalatlaco", 2, "275.00", Modalidad.NEGOCIO, EstadoPedido.ASIGNADO, r2, 1, null, null);
        pedido(dulces, c4, "Raúl Silva", "9515550112", "Av. Juárez 105", "Colonia Reforma", 1, "120.00", Modalidad.NEGOCIO, EstadoPedido.ASIGNADO, r2, 2, null, null);
        Pedido p10 = pedido(null, c1, "Carla Ríos", "9515550109", "Calle Hidalgo 22", "Centro Histórico", 2, "210.00", Modalidad.ABIERTO, EstadoPedido.ASIGNADO, r4, 1, "Panadería La Espiga", "45.00");
        Pedido p11 = pedido(dulces, c1, "Carla Ríos", "9515550109", "Calle Hidalgo 22", "Centro Histórico", 3, "420.00", Modalidad.NEGOCIO, EstadoPedido.EN_CAMINO, r1, 1, null, null);
        Pedido p12 = pedido(dulces, c2, "Mario Cruz", "9515550110", "Av. Universidad 310", "Xochimilco", 1, "180.00", Modalidad.NEGOCIO, EstadoPedido.EN_CAMINO, r1, 2, null, null);
        Pedido p13 = pedido(dulces, c3, "Elena Gómez", "9515550111", "Calle Reforma 71", "Colonia Reforma", 2, "230.00", Modalidad.NEGOCIO, EstadoPedido.ENTREGADO, r5, 1, null, null);
        pedido(dulces, c4, "Raúl Silva", "9515550112", "Calle Alcalá 140", "Centro Histórico", 1, "95.00", Modalidad.NEGOCIO, EstadoPedido.ENTREGADO, r6, 1, null, null);
        pedido(dulces, null, "Daniela Ortiz", "9515550124", "Camino Real 12", "Santa Lucía del Camino", 2, "205.00", Modalidad.NEGOCIO, EstadoPedido.NO_ENTREGADO, r6, 2, null, null);

        mensaje(p11, c1, marta, "Hola, ¿pueden llegar después de las 5 p. m.?", true);
        mensaje(p11, marta, c1, "Claro, avisamos al repartidor.", true);
        mensaje(p11, c1, marta, "Muchas gracias.", false);
        mensaje(p12, c2, marta, "¿Me confirman la hora estimada de llegada?", false);
        mensaje(p10, c1, ind1, "Estoy en la puerta azul.", true);
        mensaje(p10, ind1, c1, "Listo, llego en 5 minutos.", true);
        mensaje(p10, c1, ind1, "Perfecto, aquí espero.", false);
        mensaje(p8, c3, marta, "¿Pueden cambiar la dirección de entrega?", true);
        mensaje(p8, marta, c3, "Sí, dime la nueva dirección.", false);
        mensaje(p13, c3, marta, "Gracias, ya lo recibí.", true);

        log.info("SeederOperacion: {} vehículos, {} recorridos, {} pedidos y {} mensajes creados",
                vehiculos.count(), recorridos.count(), pedidos.count(), mensajes.count());
    }

    private Usuario usuario(String email) {
        return usuarios.findByEmail(email).orElseThrow();
    }

    private Repartidor repartidor(String email) {
        return repartidores.findByUsuarioId(usuario(email).getId()).orElseThrow();
    }

    private Vehiculo vehiculo(Negocio negocio, TipoVehiculo tipo, String placa, int capacidad, EstadoVehiculo estado,
                              LocalDate seguroVence) {
        Vehiculo v = new Vehiculo();
        v.setNegocio(negocio);
        v.setTipo(tipo);
        v.setPlaca(placa);
        v.setCapacidad(capacidad);
        v.setEstado(estado);
        v.setSeguroVence(seguroVence);
        return vehiculos.save(v);
    }

    private Recorrido recorrido(Repartidor repartidor, Vehiculo vehiculo, LocalDate fecha, EstadoRecorrido estado) {
        Recorrido r = new Recorrido();
        r.setRepartidor(repartidor);
        r.setVehiculo(vehiculo);
        r.setFecha(fecha);
        r.setEstado(estado);
        return recorridos.save(r);
    }

    private Pedido pedido(Negocio negocio, Usuario cliente, String nombre, String telefono, String direccion,
                          String zona, int articulos, String total, Modalidad modalidad, EstadoPedido estado,
                          Recorrido recorrido, Integer orden, String origen, String pago) {
        semilla++;
        double[] c = ZonaCatalogo.coordenadas(zona, semilla);
        Pedido p = new Pedido();
        p.setNegocio(negocio);
        p.setCliente(cliente);
        p.setClienteNombre(nombre);
        p.setClienteTelefono(telefono);
        p.setDireccion(direccion);
        p.setZona(zona);
        p.setLat(BigDecimal.valueOf(c[0]).setScale(6, RoundingMode.HALF_UP));
        p.setLng(BigDecimal.valueOf(c[1]).setScale(6, RoundingMode.HALF_UP));
        p.setArticulos(articulos);
        p.setTotal(new BigDecimal(total));
        p.setModalidad(modalidad);
        p.setEstado(estado);
        p.setRecorrido(recorrido);
        p.setOrdenParada(orden);
        p.setOrigen(origen);
        p.setPagoRepartidor(pago == null ? null : new BigDecimal(pago));
        return pedidos.save(p);
    }

    private void mensaje(Pedido pedido, Usuario remitente, Usuario destinatario, String texto, boolean leido) {
        Mensaje m = new Mensaje();
        m.setPedido(pedido);
        m.setRemitente(remitente);
        m.setDestinatario(destinatario);
        m.setTexto(texto);
        m.setLeido(leido);
        mensajes.save(m);
    }
}
