package com.rutaoax.vehiculo;

import jakarta.persistence.*;
import java.time.LocalDate;
import com.rutaoax.negocio.Negocio;

@Entity
@Table(name = "vehiculos")
public class Vehiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "negocio_id", nullable = false)
    private Negocio negocio;

    @Enumerated(EnumType.STRING)
    private TipoVehiculo tipo;

    private String placa;

    private int capacidad = 1;

    @Enumerated(EnumType.STRING)
    private EstadoVehiculo estado = EstadoVehiculo.ACTIVO;

    private LocalDate seguroVence;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Negocio getNegocio() { return negocio; }
    public void setNegocio(Negocio negocio) { this.negocio = negocio; }
    public TipoVehiculo getTipo() { return tipo; }
    public void setTipo(TipoVehiculo tipo) { this.tipo = tipo; }
    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }
    public int getCapacidad() { return capacidad; }
    public void setCapacidad(int capacidad) { this.capacidad = capacidad; }
    public EstadoVehiculo getEstado() { return estado; }
    public void setEstado(EstadoVehiculo estado) { this.estado = estado; }
    public LocalDate getSeguroVence() { return seguroVence; }
    public void setSeguroVence(LocalDate seguroVence) { this.seguroVence = seguroVence; }
}
