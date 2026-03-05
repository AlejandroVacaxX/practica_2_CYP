package org.alejandro.vaca.gateway;

public interface NotificadorTickets {
    void notificarCreacion(String usuarioId, String ticketId);
    void notificarAsignacion(String usuarioId, String ticketId, String agenteId);
    void notificarCierre(String usuarioId, String ticketId);
    void notificarCancelacion(String usuarioId, String ticketId, String motivo);
}
