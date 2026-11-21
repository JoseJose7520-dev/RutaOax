package com.rutaoax.usuario;

import com.rutaoax.negocio.Negocio;
import com.rutaoax.negocio.NegocioRepository;
import com.rutaoax.negocio.Repartidor;
import com.rutaoax.negocio.RepartidorRepository;
import com.rutaoax.usuario.dto.PerfilDTO;
import com.rutaoax.usuario.dto.UsuarioDTO;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private final NegocioRepository negocios;
    private final RepartidorRepository repartidores;

    public UsuarioService(NegocioRepository negocios, RepartidorRepository repartidores) {
        this.negocios = negocios;
        this.repartidores = repartidores;
    }

    @Transactional(readOnly = true)
    public UsuarioDTO aDTO(Usuario u) {
        List<String> roles = u.getRoles().stream().map(r -> r.getNombre().name()).sorted().toList();
        Long negocioId = null;
        String negocioNombre = null;
        String tipoRepartidor = null;

        if (roles.contains(RolNombre.NEGOCIO.name())) {
            Negocio propio = negocios.findByPropietarioId(u.getId()).orElse(null);
            if (propio != null) {
                negocioId = propio.getId();
                negocioNombre = propio.getNombre();
            }
        }
        if (roles.contains(RolNombre.REPARTIDOR.name())) {
            Repartidor r = repartidores.findByUsuarioId(u.getId()).orElse(null);
            if (r != null) {
                if (r.getNegocio() != null) {
                    negocioId = r.getNegocio().getId();
                    negocioNombre = r.getNegocio().getNombre();
                    tipoRepartidor = "EMPRESA";
                } else {
                    tipoRepartidor = "INDEPENDIENTE";
                }
            }
        }
        return new UsuarioDTO(u.getId(), u.getNombre(), u.getEmail(), u.getTelefono(), u.getAvatarUrl(), u.isActivo(),
                roles, new PerfilDTO(negocioId, negocioNombre, tipoRepartidor));
    }
}
