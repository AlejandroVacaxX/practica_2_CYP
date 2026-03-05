package org.alejandro.vaca.adapter;

public class TicketDTO {

    public SolicitudTicket aDominio(SolicitudTicketDTO dto) {
        if (dto == null) throw new IllegalArgumentException("La Solicitud No Puede Ser Nula");

        String usuarioId = normalizar(dto.usuarioId());
        String asunto = normalizar(dto.asunto());
        String descripcion = normalizar(dto.descripcion());

        if (usuarioId.isBlank()) throw new IllegalArgumentException("El UsuarioId No Puede Ser Nulo O Vacío");
        if (asunto.isBlank()) throw new IllegalArgumentException("El Asunto No Puede Ser Nulo O Vacío");
        if (descripcion.isBlank()) throw new IllegalArgumentException("La Descripción No Puede Ser Nula O Vacía");

        return new SolicitudTicket(usuarioId, asunto, descripcion);
    }

    private String normalizar(String v) {
        return v == null ? "" : v.trim();
    }
}


