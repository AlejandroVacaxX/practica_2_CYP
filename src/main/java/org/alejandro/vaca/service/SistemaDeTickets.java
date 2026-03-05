package org.alejandro.vaca.service;

import org.alejandro.vaca.adapter.SolicitudTicketDTO;
import org.alejandro.vaca.adapter.TicketDTO;
import org.alejandro.vaca.domain.SolicitudTicket;
import org.alejandro.vaca.domain.Ticket;
import org.alejandro.vaca.gateway.GeneradorID;
import org.alejandro.vaca.gateway.NotificadorTickets;
import org.alejandro.vaca.gateway.Reloj;
import org.alejandro.vaca.repository.TicketRegister;

import java.time.Instant;

public class SistemaDeTickets {

    private final TicketDTO adapter;
    private final TicketRegister repo;
    private final NotificadorTickets notificador;
    private final GeneradorID ids;
    private final Reloj reloj;

    public SistemaDeTickets(
            TicketDTO adapter,
            TicketRegister repo,
            NotificadorTickets notificador,
            GeneradorID ids,
            Reloj reloj
    ) {
        this.adapter = adapter;
        this.repo = repo;
        this.notificador = notificador;
        this.ids = ids;
        this.reloj = reloj;
    }

    public Ticket crear(SolicitudTicketDTO dto) {
        SolicitudTicket solicitud = adapter.aDominio(dto);

        String ticketId = ids.nuevoId();
        Instant ahora = reloj.ahora();

        Ticket creado = Ticket.creadoDesde(ticketId, solicitud, ahora);
        repo.guardar(creado);

        notificador.notificarCreacion(creado.usuarioId(), creado.ticketId());
        return creado;
    }

    public Ticket asignar(Ticket ticket, String agenteId) {
        if (ticket == null) throw new IllegalArgumentException("El Ticket No Puede Ser Nulo");
        if (agenteId == null || agenteId.isBlank()) throw new IllegalArgumentException("El AgenteId No Puede Ser Nulo O Vacío");

        Ticket asignado = ticket.asignadoA(agenteId.trim(), reloj.ahora());
        repo.actualizar(asignado);
        notificador.notificarAsignacion(asignado.usuarioId(), asignado.ticketId(), asignado.agenteId());
        return asignado;
    }

    public Ticket cerrar(Ticket ticket) {
        if (ticket == null) throw new IllegalArgumentException("El Ticket No Puede Ser Nulo");

        Ticket cerrado = ticket.cerrado(reloj.ahora());
        repo.actualizar(cerrado);
        notificador.notificarCierre(cerrado.usuarioId(), cerrado.ticketId());
        return cerrado;
    }

    public void cancelar(String usuarioId, String ticketId, String motivo) {
        if (usuarioId == null || usuarioId.isBlank()) throw new IllegalArgumentException("El UsuarioId No Puede Ser Nulo O Vacío");
        if (ticketId == null || ticketId.isBlank()) throw new IllegalArgumentException("El TicketId No Puede Ser Nulo O Vacío");
        if (motivo == null || motivo.isBlank()) throw new IllegalArgumentException("El Motivo No Puede Ser Nulo O Vacío");

        notificador.notificarCancelacion(usuarioId.trim(), ticketId.trim(), motivo.trim());
    }
}
