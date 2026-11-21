package com.rutaoax.config;

import com.rutaoax.common.ZonaCatalogo;
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
import com.rutaoax.usuario.Rol;
import com.rutaoax.usuario.RolNombre;
import com.rutaoax.usuario.RolRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * HU-39. Agrega 8 negocios más (negocio3 a negocio10), cada uno con un repartidor, un vehículo,
 * un recorrido ya terminado y un pedido entregado, para tener de 10 a 15 registros en las tablas principales.
 * No modifica a negocio1 ni a negocio2, así que las pruebas de Bruno no cambian.
 * Si negocio3@rutaoax.com ya existe no hace nada, por eso se puede correr en una base que ya tiene datos.
 */
@Component
@Order(3)
public class SeederAmpliacion implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SeederAmpliacion.class);
    private static final String CLAVE_DEMO = "Ruta#2026";

    // nombre del negocio, dirección, propietario, repartidor, tipo de vehículo, placa, capacidad, zona, cliente final
    private static final String[][] NEGOCIOS = {
            {"Mezcalería El Agave", "Calle Macedonio Alcalá 401, Centro Histórico", "Sergio Ramos", "Diego Santiago", "CAMIONETA", "OAX-610", "25", "Centro Histórico", "Patricia Luna"},
            {"Chocolates Mayordomo Norte", "Av. Eduardo Mata 1205, Colonia Reforma", "Laura Díaz", "Fernando López", "MOTOCICLETA", "OAX-611", "10", "Colonia Reforma", "Andrés Vásquez"},
            {"Tlayudas Doña Mary", "Calle Porfirio Díaz 112, Xochimilco", "María Bautista", "Ricardo Jiménez", "MOTOCICLETA", "OAX-612", "8", "Xochimilco", "Gabriela Mendoza"},
            {"Florería Jalatlaco", "Calle Aldama 305, Jalatlaco", "Claudia Reyes", "Hugo Martínez", "AUTOMOVIL", "OAX-613", "12", "Jalatlaco", "Verónica Castillo"},
            {"Farmacia San Felipe", "Av. San Felipe 88, San Felipe del Agua", "Arturo Sánchez", "Óscar Morales", "BICICLETA", "BIC-14", "4", "San Felipe del Agua", "Jaime Ortega"},
            {"Papelería Volcanes", "Calle Popocatépetl 230, Colonia Volcanes", "Adriana Flores", "Emilio García", "MOTOCICLETA", "OAX-615", "10", "Colonia Volcanes", "Rocío Hernández"},
            {"Abarrotes Cinco Señores", "Av. Símbolos Patrios 540, Cinco Señores", "Gerardo Velasco", "Samuel Pacheco", "CAMIONETA", "OAX-616", "30", "Cinco Señores", "Lorena Aquino"},
            {"Café Xoxocotlán", "Av. Ferrocarril 77, Santa Cruz Xoxocotlán", "Isabel Zárate", "Manuel Gutiérrez", "COMBI", "OAX-617", "40", "Santa Cruz Xoxocotlán", "Felipe Cervantes"},
    };

    private final RolRepository roles;
    private final UsuarioRepository usuarios;
    private final NegocioRepository negocios;
    private final RepartidorRepository repartidores;
    private final VehiculoRepository vehiculos;
    private final RecorridoRepository recorridos;
    private final PedidoRepository pedidos;
    private final PasswordEncoder encoder;

    public SeederAmpliacion(RolRepository roles, UsuarioRepository usuarios, NegocioRepository negocios,
                            RepartidorRepository repartidores, VehiculoRepository vehiculos,
                            RecorridoRepository recorridos, PedidoRepository pedidos, PasswordEncoder encoder) {
        this.roles = roles;
        this.usuarios = usuarios;
        this.negocios = negocios;
        this.repartidores = repartidores;
        this.vehiculos = vehiculos;
        this.recorridos = recorridos;
        this.pedidos = pedidos;
        this.encoder = encoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (usuarios.count() == 0 || usuarios.findByEmail("negocio3@rutaoax.com").isPresent()) {
            return;
        }
        Rol rolNegocio = roles.findByNombre(RolNombre.NEGOCIO).orElseThrow();
        Rol rolRepartidor = roles.findByNombre(RolNombre.REPARTIDOR).orElseThrow();
        LocalDate hoy = LocalDate.now();

        for (int i = 0; i < NEGOCIOS.length; i++) {
            String[] d = NEGOCIOS[i];
            int n = i + 3;   // negocio3 ... negocio10
            int r = i + 5;   // repartidor5 ... repartidor12

            Usuario propietario = crearUsuario(d[2], "negocio" + n + "@rutaoax.com", telefono(200 + n), rolNegocio);
            Negocio negocio = new Negocio();
            negocio.setNombre(d[0]);
            negocio.setDireccion(d[1]);
            negocio.setTelefono(propietario.getTelefono());
            negocio.setPropietario(propietario);
            negocio = negocios.save(negocio);

            Repartidor repartidor = new Repartidor();
            repartidor.setUsuario(crearUsuario(d[3], "repartidor" + r + "@rutaoax.com", telefono(300 + r), rolRepartidor));
            repartidor.setNegocio(negocio);
            repartidor = repartidores.save(repartidor);

            Vehiculo vehiculo = new Vehiculo();
            vehiculo.setNegocio(negocio);
            vehiculo.setTipo(TipoVehiculo.valueOf(d[4]));
            vehiculo.setPlaca(d[5]);
            vehiculo.setCapacidad(Integer.parseInt(d[6]));
            vehiculo.setEstado(EstadoVehiculo.ACTIVO);
            vehiculo.setSeguroVence(vehiculo.getTipo() == TipoVehiculo.BICICLETA ? null : hoy.plusDays(90 + 20L * i));
            vehiculo = vehiculos.save(vehiculo);

            // recorrido de días pasados con un pedido entregado, para que el historial tenga algo
            Recorrido recorrido = new Recorrido();
            recorrido.setRepartidor(repartidor);
            recorrido.setVehiculo(vehiculo);
            recorrido.setFecha(hoy.minusDays(2 + i));
            recorrido.setEstado(EstadoRecorrido.FINALIZADO);
            recorrido = recorridos.save(recorrido);

            double[] c = ZonaCatalogo.coordenadas(d[7], 100 + i);
            Pedido p = new Pedido();
            p.setNegocio(negocio);
            p.setClienteNombre(d[8]);
            p.setClienteTelefono(telefono(400 + n));
            p.setDireccion("Calle " + (10 + i * 7) + " de " + d[7]);
            p.setZona(d[7]);
            p.setLat(BigDecimal.valueOf(c[0]).setScale(6, RoundingMode.HALF_UP));
            p.setLng(BigDecimal.valueOf(c[1]).setScale(6, RoundingMode.HALF_UP));
            p.setArticulos(1 + i % 4);
            p.setTotal(new BigDecimal(150 + 35 * i).setScale(2, RoundingMode.HALF_UP));
            p.setModalidad(Modalidad.NEGOCIO);
            p.setEstado(EstadoPedido.ENTREGADO);
            p.setRecorrido(recorrido);
            p.setOrdenParada(1);
            pedidos.save(p);
        }

        log.info("SeederAmpliacion: ahora hay {} usuarios, {} negocios, {} repartidores, {} vehículos, {} recorridos y {} pedidos",
                usuarios.count(), negocios.count(), repartidores.count(), vehiculos.count(), recorridos.count(), pedidos.count());
    }

    private Usuario crearUsuario(String nombre, String email, String telefono, Rol rol) {
        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setEmail(email);
        u.setPasswordHash(encoder.encode(CLAVE_DEMO));
        u.setTelefono(telefono);
        u.setActivo(true);
        u.getRoles().add(rol);
        return usuarios.save(u);
    }

    private static String telefono(int sufijo) {
        return "951555" + String.format("%04d", sufijo);
    }
}
