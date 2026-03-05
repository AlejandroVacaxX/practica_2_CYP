package org.alejandro.vaca.domain;

import java.time.Instant;

public record Ticket(
        String ticketId,
        String usuarioId,
        String asunto,
        String descripcion,
        EstadoTicket estado,
        String agenteId,
        Instant creadoEn,
        Instant actualizadoEn
) {
    public static Ticket creadoDesde(String ticketId, SolicitudTicket solicitud, Instant ahora) {
        return new Ticket(
                ticketId,
                solicitud.usuarioId(),
                solicitud.asunto(),
                solicitud.descripcion(),
                EstadoTicket.CREADO,
                null,
                ahora,
                ahora
        );
    }

    public Ticket asignadoA(String agenteId, Instant ahora) {
        return new Ticket(ticketId, usuarioId, asunto, descripcion, EstadoTicket.ASIGNADO, agenteId, creadoEn, ahora);
    }

    public Ticket cerrado(Instant ahora) {
        return new Ticket(ticketId, usuarioId, asunto, descripcion, EstadoTicket.CERRADO, agenteId, creadoEn, ahora);
    }

    public Ticket cancelado(Instant ahora) {
        return new Ticket(ticketId, usuarioId, asunto, descripcion, EstadoTicket.CANCELADO, agenteId, creadoEn, ahora);
    }
}
