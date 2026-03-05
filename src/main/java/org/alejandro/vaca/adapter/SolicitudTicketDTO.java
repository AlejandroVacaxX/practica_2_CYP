package org.alejandro.vaca.adapter;

public record SolicitudTicketDTO(
        String usuarioId,
        String asunto,
        String descripcion
) {}