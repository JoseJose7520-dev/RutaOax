package com.rutaoax.vehiculo;

import com.rutaoax.auth.UsuarioPrincipal;
import com.rutaoax.common.ConflictException;
import com.rutaoax.common.NotFoundException;
import com.rutaoax.common.PageResponse;
import com.rutaoax.negocio.Negocio;
import com.rutaoax.negocio.NegocioContexto;
import com.rutaoax.recorrido.EstadoRecorrido;
import com.rutaoax.recorrido.Recorrido;
import com.rutaoax.recorrido.RecorridoRepository;
import com.rutaoax.vehiculo.dto.VehiculoDTO;
import com.rutaoax.vehiculo.dto.VehiculoDisponibleDTO;
import com.rutaoax.vehiculo.dto.VehiculoRequest;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VehiculoService {

    private final VehiculoRepository vehiculos;
    private final RecorridoRepository recorridos;
    private final NegocioContexto contexto;

    public VehiculoService(VehiculoRepository vehiculos, RecorridoRepository recorridos, NegocioContexto contexto) {
        this.vehiculos = vehiculos;
        this.recorridos = recorridos;
        this.contexto = contexto;
    }

    @Transactional(readOnly = true)
    public PageResponse<VehiculoDTO> listar(UsuarioPrincipal yo, String q, TipoVehiculo tipo, EstadoVehiculo estado,
                                            int page, int size) {
        Long negocioId = contexto.ambito(yo);
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50),
                Sort.by("placa").ascending());
        Page<Vehiculo> pagina = vehiculos.buscar(negocioId, q == null ? "" : q.trim(), tipo, estado, pageable);
        Map<Long, String> uso = usoDeHoy(pagina.getContent().stream().map(Vehiculo::getId).toList());
        return PageResponse.from(pagina.map(v -> VehiculoDTO.from(v, uso.get(v.getId()), uso.containsKey(v.getId()))));
    }

    @Transactional(readOnly = true)
    public VehiculoDTO obtener(UsuarioPrincipal yo, Long id) {
        return aDTO(buscarPropio(yo, id));
    }

    @Transactional
    public VehiculoDTO crear(UsuarioPrincipal yo, VehiculoRequest req) {
        Negocio negocio = contexto.negocioDe(yo);
        String placa = normalizar(req.placa());
        if (vehiculos.existsByPlaca(placa)) {
            throw new ConflictException("Ya existe un vehículo con esa placa");
        }
        Vehiculo v = new Vehiculo();
        v.setNegocio(negocio);
        aplicar(v, req, placa);
        return aDTO(vehiculos.save(v));
    }

    @Transactional
    public VehiculoDTO actualizar(UsuarioPrincipal yo, Long id, VehiculoRequest req) {
        Vehiculo v = buscarPropio(yo, id);
        String placa = normalizar(req.placa());
        if (!placa.equals(v.getPlaca()) && vehiculos.existsByPlacaAndIdNot(placa, id)) {
            throw new ConflictException("Ya existe un vehículo con esa placa");
        }
        aplicar(v, req, placa);
        return aDTO(vehiculos.save(v));
    }

    @Transactional
    public void eliminar(UsuarioPrincipal yo, Long id) {
        Vehiculo v = buscarPropio(yo, id);
        if (recorridos.existsByVehiculoIdAndFechaGreaterThanEqualAndEstadoNot(id, LocalDate.now(), EstadoRecorrido.FINALIZADO)) {
            throw new ConflictException("El vehículo tiene recorridos programados. Espera a que terminen o reasígnalos antes de eliminarlo");
        }
        vehiculos.delete(v);
    }

    @Transactional(readOnly = true)
    public List<VehiculoDisponibleDTO> disponibles(UsuarioPrincipal yo, LocalDate fecha) {
        Negocio negocio = contexto.negocioDe(yo);
        LocalDate dia = fecha == null ? LocalDate.now() : fecha;
        Set<Long> ocupados = new HashSet<>(recorridos.vehiculoIdsConRecorrido(dia));
        return vehiculos.findByNegocioIdAndEstadoOrderByPlaca(negocio.getId(), EstadoVehiculo.ACTIVO).stream()
                .filter(v -> !ocupados.contains(v.getId()))
                .map(v -> new VehiculoDisponibleDTO(v.getId(), v.getTipo(), v.getPlaca(), v.getCapacidad(), v.getCapacidad()))
                .toList();
    }

    private Vehiculo buscarPropio(UsuarioPrincipal yo, Long id) {
        Long negocioId = contexto.ambito(yo);
        Vehiculo v = vehiculos.findById(id).orElseThrow(() -> new NotFoundException("El vehículo no existe"));
        if (negocioId != null && !v.getNegocio().getId().equals(negocioId)) {
            throw new NotFoundException("El vehículo no existe");
        }
        return v;
    }

    private void aplicar(Vehiculo v, VehiculoRequest req, String placa) {
        v.setTipo(req.tipo());
        v.setPlaca(placa);
        v.setCapacidad(req.capacidad());
        v.setEstado(req.estado());
        v.setSeguroVence(req.seguroVence());
    }

    private VehiculoDTO aDTO(Vehiculo v) {
        Map<Long, String> uso = usoDeHoy(List.of(v.getId()));
        return VehiculoDTO.from(v, uso.get(v.getId()), uso.containsKey(v.getId()));
    }

    /** Vehículo → nombre del repartidor que lo usa hoy en un recorrido sin finalizar. */
    private Map<Long, String> usoDeHoy(List<Long> vehiculoIds) {
        Map<Long, String> uso = new HashMap<>();
        if (vehiculoIds.isEmpty()) {
            return uso;
        }
        for (Recorrido r : recorridos.activosPorVehiculos(LocalDate.now(), vehiculoIds)) {
            uso.putIfAbsent(r.getVehiculo().getId(), r.getRepartidor().getUsuario().getNombre());
        }
        return uso;
    }

    private String normalizar(String placa) {
        return placa.trim().toUpperCase();
    }
}
