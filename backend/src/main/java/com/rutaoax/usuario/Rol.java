package com.rutaoax.usuario;

import jakarta.persistence.*;

@Entity
@Table(name = "roles")
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private RolNombre nombre;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public RolNombre getNombre() { return nombre; }
    public void setNombre(RolNombre nombre) { this.nombre = nombre; }
}
