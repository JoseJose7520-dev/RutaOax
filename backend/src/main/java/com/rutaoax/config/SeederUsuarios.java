package com.rutaoax.config;

import com.rutaoax.negocio.Negocio;
import com.rutaoax.negocio.NegocioRepository;
import com.rutaoax.negocio.Repartidor;
import com.rutaoax.negocio.RepartidorRepository;
import com.rutaoax.usuario.Rol;
import com.rutaoax.usuario.RolNombre;
import com.rutaoax.usuario.RolRepository;
import com.rutaoax.usuario.Usuario;
import com.rutaoax.usuario.UsuarioRepository;
import java.util.EnumMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(1)
public class SeederUsuarios implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SeederUsuarios.class);
    private static final String CLAVE_ADMIN = "Admin#2026";
    private static final String CLAVE_DEMO = "Ruta#2026";

    private final RolRepository roles;
    private final UsuarioRepository usuarios;
    private final NegocioRepository negocios;
    private final RepartidorRepository repartidores;
    private final PasswordEncoder encoder;

    public SeederUsuarios(RolRepository roles, UsuarioRepository usuarios, NegocioRepository negocios,
                          RepartidorRepository repartidores, PasswordEncoder encoder) {
        this.roles = roles;
        this.usuarios = usuarios;
        this.negocios = negocios;
        this.repartidores = repartidores;
        this.encoder = encoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (roles.count() > 0) {
            return;
        }
        Map<RolNombre, Rol> rol = new EnumMap<>(RolNombre.class);
        for (RolNombre nombre : RolNombre.values()) {
            Rol r = new Rol();
            r.setNombre(nombre);
            rol.put(nombre, roles.save(r));
        }

        crear("Administrador RutaOax", "admin@rutaoax.com", CLAVE_ADMIN, "9515550100", rol.get(RolNombre.ADMIN));
        Usuario marta = crear("Marta Valle", "negocio1@rutaoax.com", CLAVE_DEMO, "9515550101", rol.get(RolNombre.NEGOCIO));
        Usuario hector = crear("Héctor Mora", "negocio2@rutaoax.com", CLAVE_DEMO, "9515550102", rol.get(RolNombre.NEGOCIO));

        Negocio dulces = crearNegocio("Dulces del Valle", "Calle Hidalgo 45, Centro Histórico", "9515550101", marta);
        Negocio espiga = crearNegocio("Panadería La Espiga", "Av. Independencia 210, Centro Histórico", "9515550102", hector);

        Rol repartidor = rol.get(RolNombre.REPARTIDOR);
        crearRepartidor(crear("Luis Martínez", "repartidor1@rutaoax.com", CLAVE_DEMO, "9515550103", repartidor), dulces);
        crearRepartidor(crear("Ana Ruiz", "repartidor2@rutaoax.com", CLAVE_DEMO, "9515550104", repartidor), dulces);
        crearRepartidor(crear("Jorge Pérez", "repartidor3@rutaoax.com", CLAVE_DEMO, "9515550105", repartidor), dulces);
        crearRepartidor(crear("Rosa Cruz", "repartidor4@rutaoax.com", CLAVE_DEMO, "9515550106", repartidor), espiga);
        crearRepartidor(crear("Pablo Hernández", "independiente1@rutaoax.com", CLAVE_DEMO, "9515550107", repartidor), null);
        crearRepartidor(crear("Iván Torres", "independiente2@rutaoax.com", CLAVE_DEMO, "9515550108", repartidor), null);

        Rol cliente = rol.get(RolNombre.CLIENTE);
        crear("Carla Ríos", "cliente1@rutaoax.com", CLAVE_DEMO, "9515550109", cliente);
        crear("Mario Cruz", "cliente2@rutaoax.com", CLAVE_DEMO, "9515550110", cliente);
        crear("Elena Gómez", "cliente3@rutaoax.com", CLAVE_DEMO, "9515550111", cliente);
        crear("Raúl Silva", "cliente4@rutaoax.com", CLAVE_DEMO, "9515550112", cliente);

        log.info("SeederUsuarios: {} usuarios, {} negocios y {} repartidores creados",
                usuarios.count(), negocios.count(), repartidores.count());
    }

    private Usuario crear(String nombre, String email, String clave, String telefono, Rol rol) {
        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setEmail(email);
        u.setPasswordHash(encoder.encode(clave));
        u.setTelefono(telefono);
        u.setActivo(true);
        u.getRoles().add(rol);
        return usuarios.save(u);
    }

    private Negocio crearNegocio(String nombre, String direccion, String telefono, Usuario propietario) {
        Negocio n = new Negocio();
        n.setNombre(nombre);
        n.setDireccion(direccion);
        n.setTelefono(telefono);
        n.setPropietario(propietario);
        return negocios.save(n);
    }

    private void crearRepartidor(Usuario usuario, Negocio negocio) {
        Repartidor r = new Repartidor();
        r.setUsuario(usuario);
        r.setNegocio(negocio);
        repartidores.save(r);
    }
}
