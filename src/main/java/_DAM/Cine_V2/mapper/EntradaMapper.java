package _DAM.Cine_V2.mapper;

import _DAM.Cine_V2.dto.entrada.EntradaInputDTO;
import _DAM.Cine_V2.dto.entrada.EntradaOutputDTO;
import _DAM.Cine_V2.modelo.Entrada;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EntradaMapper {

    @Mapping(target = "funcionId", source = "funcion.id")
    @Mapping(target = "ventaId", source = "venta.id")
    @Mapping(target = "peliculaTitulo", source = "funcion.pelicula.titulo")
    @Mapping(target = "fechaHora", source = "funcion.fechaHora")
    EntradaOutputDTO toDTO(Entrada entrada);

    @Mapping(target = "funcion", ignore = true)
    @Mapping(target = "venta", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "codigo", ignore = true)
    @Mapping(target = "estado", ignore = true)
    Entrada toEntity(EntradaInputDTO entradaInputDTO);

    @Mapping(target = "funcion", ignore = true)
    @Mapping(target = "venta", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "codigo", ignore = true)
    @Mapping(target = "estado", ignore = true)
    void update(EntradaInputDTO entradaInputDTO, @MappingTarget Entrada entrada);
}
