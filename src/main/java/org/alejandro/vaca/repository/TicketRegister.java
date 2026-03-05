package org.alejandro.vaca.repository;

import org.alejandro.vaca.domain.Ticket;

public interface TicketRegister {
    void guardar(Ticket ticket);
    void actualizar(Ticket ticket);
}
