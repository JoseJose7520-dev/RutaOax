package com.rutaoax.pedido;

import com.rutaoax.auth.UsuarioPrincipal;
import com.rutaoax.common.ConflictException;
import com.rutaoax.common.Geo;
import com.rutaoax.common.NotFoundException;
import com.rutaoax.common.Zona;
import com.rutaoax.common.ZonaCatalogo;
import com.rutaoax.negocio.Negocio;
import com.rutaoax.negocio.NegocioContexto;
import com.rutaoax.negocio.Repartidor;
import com.rutaoax.negocio.RepartidorRepository;
import com.rutaoax.pedido.dto.AsignarRequest;
import com.rutaoax.pedido.dto.PedidoDTO;
import com.rutaoax.recorrido.EstadoRecorrido;
import com.rutaoax.recorrido.Recorrido;
import com.rutaoax.recorrido.RecorridoRepository;
import com.rutaoax.recorrido.RecorridoService;
import com.rutaoax.recorrido.dto.RecorridoDTO;
import com.rutaoax.vehiculo.EstadoVehiculo;
import com.rutaoax.vehiculo.Vehiculo;
import com.rutaoax.vehiculo.VehiculoRepository;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AsignacionService {

    private final NegocioContexto contexto;
    private final PedidoRepository pedidos;
    private final RepartidorRepository repartidores;
    private final VehiculoRepository vehiculos;
    private final RecorridoRepository recorridos;
    private final RecorridoService recorridoService;

    public AsignacionService(NegocioContexto contexto, PedidoRepository pedidos, RepartidorRepository repartidores,
                             VehiculoRepository vehiculos, RecorridoRepository recorridos,
                             RecorridoService recorridoService) {
        this.contexto = contexto;
        this.pedidos = pedidos;
        this.repartidores = repartidores;
        this.vehiculos = vehiculos;
        this.recorridos = recorridos;
        this.recorridoService = recorridoService;
    }

    @Transactional
    public RecorridoDTO asignar(UsuarioPrincipal yo, AsignarRequest req) {
        Negocio negocio = contexto.negocioDe(yo);
        Repartidor repartidor = repartidores.findByIdAndNegocioId(req.repartidorId(), negocio.getId())
                .filter(r -> r.getUsuario().isActivo())
                .orElseThrow(() -> new NotFoundException("El repartidor no existe"));
        Vehiculo vehiculo = vehiculos.findById(req.vehiculoId())
                .filter(v -> v.getNegocio().getId().equals(negocio.getId()))
                .orElseThrow(() -> new NotFoundException("El vehículo no existe"));
        if (vehiculo.getEstado() != EstadoVehiculo.ACTIVO) {
            throw new ConflictException("El vehículo " + vehiculo.getPlaca() + " está en mantenimiento");
        }

        LinkedHashSet<Long> ids = new LinkedHashSet<>(req.pedidoIds());
        List<Pedido> lista = pedidos.findAllById(ids);
        if (lista.size() != ids.size()) {
            throw new NotFoundException("Alguno de los pedidos no existe");
        }
        for (Pedido p : lista) {
            if (p.getNegocio() == null || !p.getNegocio().getId().equals(negocio.getId())) {
                throw new NotFoundException("Alguno de los pedidos no existe");
            }
            if (p.getEstado() != EstadoPedido.PENDIENTE) {
                throw new ConflictException("El pedido #" + p.getId() + " ya no está sin asignar");
            }
        }

        Recorrido recorrido = recorridos.findByRepartidorIdAndFecha(repartidor.getId(), req.fecha()).orElse(null);
        if (recorrido != null) {
            if (recorrido.getEstado() != EstadoRecorrido.PLANEADO) {
                throw new ConflictException("El repartidor ya salió a ruta o terminó su recorrido ese día");
            }
            if (recorrido.getVehiculo() != null && !recorrido.getVehiculo().getId().equals(vehiculo.getId())) {
                throw new ConflictException("El repartidor ya tiene otro vehículo asignado ese día");
            }
        }
        for (Recorrido otro : recorridos.findByVehiculoIdAndFecha(vehiculo.getId(), req.fecha())) {
            if (recorrido == null || !otro.getId().equals(recorrido.getId())) {
                throw new ConflictException("El vehículo ya está asignado a otro repartidor ese día");
            }
        }

        long actuales = recorrido == null ? 0 : pedidos.countByRecorridoId(recorrido.getId());
        long cupo = vehiculo.getCapacidad() - actuales;
        if (lista.size() > cupo) {
            throw new ConflictException("Excede la capacidad del vehículo. Cupo restante: " + Math.max(cupo, 0));
        }

        if (recorrido == null) {
            recorrido = new Recorrido();
            recorrido.setRepartidor(repartidor);
            recorrido.setFecha(req.fecha());
            recorrido.setEstado(EstadoRecorrido.PLANEADO);
        }
        recorrido.setVehiculo(vehiculo);
        recorrido = recorridos.save(recorrido);

        double[] punto = puntoDePartida(recorrido.getId(), actuales);
        List<Pedido> restantes = new ArrayList<>(lista);
        int orden = (int) actuales;
        while (!restantes.isEmpty()) {
            Pedido mejor = null;
            double mejorDistancia = Double.MAX_VALUE;
            for (Pedido candidato : restantes) {
                double d = Geo.distanciaKm(punto[0], punto[1], candidato.getLat().doubleValue(),
                        candidato.getLng().doubleValue());
                if (d < mejorDistancia) {
                    mejorDistancia = d;
                    mejor = candidato;
                }
            }
            restantes.remove(mejor);
            orden++;
            mejor.setRecorrido(recorrido);
            mejor.setOrdenParada(orden);
            mejor.setEstado(EstadoPedido.ASIGNADO);
            mejor.setModalidad(Modalidad.NEGOCIO);
            punto = new double[]{mejor.getLat().doubleValue(), mejor.getLng().doubleValue()};
        }
        pedidos.saveAll(lista);
        return recorridoService.aDTO(recorrido.getId());
    }

    @Transactional
    public PedidoDTO desasignar(UsuarioPrincipal yo, Long pedidoId) {
        Negocio negocio = contexto.negocioDe(yo);
        Pedido p = pedidos.detalle(pedidoId)
                .filter(x -> x.getNegocio() != null && x.getNegocio().getId().equals(negocio.getId()))
                .orElseThrow(() -> new NotFoundException("El pedido no existe"));
        if (p.getEstado() != EstadoPedido.ASIGNADO) {
            throw new ConflictException("Solo se puede desasignar un pedido asignado que todavía no sale a ruta");
        }
        Recorrido recorrido = p.getRecorrido();
        p.setRecorrido(null);
        p.setOrdenParada(null);
        p.setEstado(EstadoPedido.PENDIENTE);
        pedidos.saveAndFlush(p);
        recorridoService.compactar(recorrido);
        return PedidoDTO.from(p);
    }

    /** Un pedido que no se pudo entregar vuelve a estar sin asignar y sale de su recorrido. */
    @Transactional
    public PedidoDTO reasignar(UsuarioPrincipal yo, Long pedidoId) {
        Negocio negocio = contexto.negocioDe(yo);
        Pedido p = pedidos.detalle(pedidoId)
                .filter(x -> x.getNegocio() != null && x.getNegocio().getId().equals(negocio.getId()))
                .orElseThrow(() -> new NotFoundException("El pedido no existe"));
        if (p.getEstado() != EstadoPedido.NO_ENTREGADO) {
            throw new ConflictException("Solo se puede reasignar un pedido que no se pudo entregar");
        }
        p.setRecorrido(null);
        p.setOrdenParada(null);
        p.setEstado(EstadoPedido.PENDIENTE);
        return PedidoDTO.from(pedidos.save(p));
    }

    /** Último punto del recorrido si ya tiene paradas; si no, el Centro Histórico. */
    private double[] puntoDePartida(Long recorridoId, long paradasActuales) {
        if (paradasActuales > 0) {
            List<Pedido> paradas = pedidos.findByRecorridoIdOrderByOrdenParada(recorridoId);
            Pedido ultima = paradas.get(paradas.size() - 1);
            return new double[]{ultima.getLat().doubleValue(), ultima.getLng().doubleValue()};
        }
        Zona centro = ZonaCatalogo.todas().get(0);
        return new double[]{centro.lat(), centro.lng()};
    }
}
