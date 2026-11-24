package com.rutaoax.usuario;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    @EntityGraph(attributePaths = "roles")
    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    // Busqueda del administrador: texto en nombre o correo, rol y estado (cada filtro es opcional)
    @Query("select u from Usuario u where (:q is null or lower(u.nombre) like :q or lower(u.email) like :q)"
            + " and (:rol is null or exists (select 1 from Usuario x join x.roles r where x.id = u.id and r.nombre = :rol))"
            + " and (:activo is null or u.activo = :activo)")
    Page<Usuario> buscarAdmin(@Param("q") String q, @Param("rol") RolNombre rol, @Param("activo") Boolean activo,
                              Pageable pageable);

    @Query("select count(u) from Usuario u join u.roles r where r.nombre = :rol and u.activo = true")
    long contarActivosConRol(@Param("rol") RolNombre rol);

    // Historial que impide borrar una cuenta (en ese caso se desactiva)
    @Query("select count(r) from Recorrido r where r.repartidor.usuario.id = :id")
    long contarRecorridosDe(@Param("id") Long id);

    @Query("select count(p) from Pedido p where p.negocio.propietario.id = :id")
    long contarPedidosDelNegocioDe(@Param("id") Long id);
}
