package com.rutaoax.pedido;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.rutaoax.negocio.Negocio;
import com.rutaoax.recorrido.Recorrido;
import com.rutaoax.usuario.Usuario;

@Entity
@Table(name = "pedidos")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "negocio_id")
    private Negocio negocio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Usuario cliente;

    private String clienteNombre;

    private String clienteTelefono;

    private String direccion;

    private String zona;

    private BigDecimal lat;

    private BigDecimal lng;

    private int articulos = 1;

    private BigDecimal total = BigDecimal.ZERO;

    private String notas;

    private String origen;

    private BigDecimal pagoRepartidor;

    @Enumerated(EnumType.STRING)
    private Modalidad modalidad = Modalidad.NEGOCIO;

    @Enumerated(EnumType.STRING)
    private EstadoPedido estado = EstadoPedido.PENDIENTE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorrido_id")
    private Recorrido recorrido;

    private Integer ordenParada;

    private LocalDateTime creadoEn;

    private LocalDateTime actualizadoEn;

    @PrePersist
    void antesDeGuardar() {
        if (creadoEn == null) creadoEn = LocalDateTime.now();
        if (actualizadoEn == null) actualizadoEn = LocalDateTime.now();
    }

    @PreUpdate
    void antesDeActualizar() {
        actualizadoEn = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Negocio getNegocio() { return negocio; }
    public void setNegocio(Negocio negocio) { this.negocio = negocio; }
    public Usuario getCliente() { return cliente; }
    public void setCliente(Usuario cliente) { this.cliente = cliente; }
    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }
    public String getClienteTelefono() { return clienteTelefono; }
    public void setClienteTelefono(String clienteTelefono) { this.clienteTelefono = clienteTelefono; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getZona() { return zona; }
    public void setZona(String zona) { this.zona = zona; }
    public BigDecimal getLat() { return lat; }
    public void setLat(BigDecimal lat) { this.lat = lat; }
    public BigDecimal getLng() { return lng; }
    public void setLng(BigDecimal lng) { this.lng = lng; }
    public int getArticulos() { return articulos; }
    public void setArticulos(int articulos) { this.articulos = articulos; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }
    public String getOrigen() { return origen; }
    public void setOrigen(String origen) { this.origen = origen; }
    public BigDecimal getPagoRepartidor() { return pagoRepartidor; }
    public void setPagoRepartidor(BigDecimal pagoRepartidor) { this.pagoRepartidor = pagoRepartidor; }
    public Modalidad getModalidad() { return modalidad; }
    public void setModalidad(Modalidad modalidad) { this.modalidad = modalidad; }
    public EstadoPedido getEstado() { return estado; }
    public void setEstado(EstadoPedido estado) { this.estado = estado; }
    public Recorrido getRecorrido() { return recorrido; }
    public void setRecorrido(Recorrido recorrido) { this.recorrido = recorrido; }
    public Integer getOrdenParada() { return ordenParada; }
    public void setOrdenParada(Integer ordenParada) { this.ordenParada = ordenParada; }
    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }
    public LocalDateTime getActualizadoEn() { return actualizadoEn; }
    public void setActualizadoEn(LocalDateTime actualizadoEn) { this.actualizadoEn = actualizadoEn; }
}
