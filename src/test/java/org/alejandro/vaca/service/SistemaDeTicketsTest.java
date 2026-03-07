package org.alejandro.vaca.service;

import org.alejandro.vaca.adapter.SolicitudTicketDTO;
import org.alejandro.vaca.adapter.TicketDTO;
import org.alejandro.vaca.domain.SolicitudTicket;
import org.alejandro.vaca.domain.Ticket;
import org.alejandro.vaca.gateway.GeneradorID;
import org.alejandro.vaca.gateway.NotificadorTickets;
import org.alejandro.vaca.gateway.Reloj;
import org.alejandro.vaca.repository.TicketRegister;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SistemaDeTicketsTest {

    @Mock
    TicketDTO adapter;
    @Mock
    TicketRegister repo;
    @Mock
    NotificadorTickets notificador;
    @Mock
    GeneradorID ids;
    @Mock
    Reloj reloj;

    @InjectMocks
    SistemaDeTickets service;

    @Nested
    @DisplayName("Pruebas para la creacion de Tickets")
    public class CreacionTicket {

        @Test
        @DisplayName("Este metodo crea un ticket y corrobora los datos en el")
        public void creacionTicketCorrecta() {
            SolicitudTicketDTO dto = new SolicitudTicketDTO("user1", "Asunto", "Desc");
            SolicitudTicket solicitud = new SolicitudTicket("user1", "Asunto", "Desc");
            Instant ahora = Instant.now();

            when(adapter.aDominio(dto)).thenReturn(solicitud);
            when(ids.nuevoId()).thenReturn("T-123");
            when(reloj.ahora()).thenReturn(ahora);

            // esto 'mockea' un metodo estatico, lo puse dentro de un try porque podria dar error si no se cierra el metodo al final
            try (MockedStatic<Ticket> ticketMockedStatic = mockStatic(Ticket.class)) {
                Ticket ticketMock = mock(Ticket.class);
                when(ticketMock.usuarioId()).thenReturn("user1");
                when(ticketMock.ticketId()).thenReturn("T-123");

                ticketMockedStatic.when(() -> Ticket.creadoDesde("T-123", solicitud, ahora)).thenReturn(ticketMock);

                Ticket resultado = service.crear(dto);

                assertEquals(ticketMock, resultado);
                verify(repo).guardar(ticketMock);
                verify(notificador).notificarCreacion("user1", "T-123");
            }
        }

        @Test
        @DisplayName("Este metodo debera lanzar una excepcion si falla la creacion del ticket desde el adapter")
        public void creacionTicketIncorrecta() {
            SolicitudTicketDTO dto = new SolicitudTicketDTO(null, "Asunto", "Desc");
            when(adapter.aDominio(dto)).thenThrow(new IllegalArgumentException("El UsuarioId No Puede Ser Nulo"));

            assertThrows(IllegalArgumentException.class, () -> service.crear(dto));
            verifyNoInteractions(repo, notificador);
        }
        @Test
        @DisplayName("Valida que al lanzar excepcion por DTO nulo, la variable de resultado se mantenga nula")
        public void creacionTicketDTONulo() {
            Ticket resultado = null;
            when(adapter.aDominio(null)).thenThrow(new IllegalArgumentException("La Solicitud No Puede Ser Nula"));

            assertThrows(IllegalArgumentException.class, () -> service.crear(null));
            assertNull(resultado);
        }

        @Test
        @DisplayName("Verifica que las dependencias de generador ID y Reloj sean consultadas correctamente")
        public void creacionTicketLlamadasExternas() {
            SolicitudTicketDTO dto = new SolicitudTicketDTO("user1", "Asunto", "Desc");
            SolicitudTicket solicitud = new SolicitudTicket("user1", "Asunto", "Desc");
            when(adapter.aDominio(dto)).thenReturn(solicitud);
            when(ids.nuevoId()).thenReturn("T-999");
            when(reloj.ahora()).thenReturn(Instant.now());

            try (MockedStatic<Ticket> ticketMockedStatic = mockStatic(Ticket.class)) {
                Ticket ticketMock = mock(Ticket.class);
                ticketMockedStatic.when(() -> Ticket.creadoDesde(any(), any(), any())).thenReturn(ticketMock);

                service.crear(dto);

                verify(ids).nuevoId();
                verify(reloj).ahora();
            }
        }

        @Test
        @DisplayName("Verifica que no existan mas interacciones con el notificador posterior a la creacion")
        public void creacionTicketNoMasInteracciones() {
            SolicitudTicketDTO dto = new SolicitudTicketDTO("usr", "Asunto", "Desc");
            when(adapter.aDominio(dto)).thenReturn(new SolicitudTicket("usr", "Asunto", "Desc"));

            try (MockedStatic<Ticket> ticketMockedStatic = mockStatic(Ticket.class)) {
                Ticket ticketMock = mock(Ticket.class);
                ticketMockedStatic.when(() -> Ticket.creadoDesde(any(), any(), any())).thenReturn(ticketMock);

                service.crear(dto);

                verify(notificador).notificarCreacion(any(), any());
                verifyNoMoreInteractions(notificador);
            }
        }
    }

    @Nested
    @DisplayName("Pruebas para la asignacion de Tickets")
    public class AsignarTicket {

        @Test
        @DisplayName("Asigna el ticket correctamente y valida que el estado retornado es distinto al original")
        public void asignarTicketCorrecto() {
            Ticket ticketOriginal = mock(Ticket.class);
            Ticket ticketAsignado = mock(Ticket.class);
            Instant ahora = Instant.now();

            when(reloj.ahora()).thenReturn(ahora);
            when(ticketOriginal.asignadoA("Agente1", ahora)).thenReturn(ticketAsignado);
            when(ticketAsignado.usuarioId()).thenReturn("User1");
            when(ticketAsignado.ticketId()).thenReturn("T-1");
            when(ticketAsignado.agenteId()).thenReturn("Agente1");

            Ticket resultado = service.asignar(ticketOriginal, "Agente1");

            assertEquals(ticketAsignado, resultado);
            assertNotEquals(ticketOriginal, resultado);
            verify(repo).actualizar(ticketAsignado);
            verify(notificador).notificarAsignacion("User1", "T-1", "Agente1");
        }

        @Test
        @DisplayName("Lanza excepcion si el ticket proveido para asignacion es nulo")
        public void asignarTicketNuloLanzaExcepcion() {
            assertThrows(IllegalArgumentException.class, () -> service.asignar(null, "Agente1"));
        }

        @Test
        @DisplayName("Lanza excepcion si el agenteId es nulo")
        public void asignarAgenteNuloLanzaExcepcion() {
            Ticket ticketOriginal = mock(Ticket.class);
            assertThrows(IllegalArgumentException.class, () -> service.asignar(ticketOriginal, null));
        }

        @Test
        @DisplayName("Lanza excepcion si el agenteId esta vacío o en blanco")
        public void asignarAgenteVacioLanzaExcepcion() {
            Ticket ticketOriginal = mock(Ticket.class);
            assertThrows(IllegalArgumentException.class, () -> service.asignar(ticketOriginal, "   "));
        }

        @Test
        @DisplayName("Verifica que no hay interacciones con el repositorio en caso de fallo por validacion")
        public void asignarFalloNoInteractuaConDependencias() {
            assertThrows(IllegalArgumentException.class, () -> service.asignar(null, "Agente1"));
            // si algo falla el repo 'mockeado' no deberia verse modificado
            verifyNoInteractions(repo);
            verifyNoInteractions(notificador);
        }
    }

    @Nested
    @DisplayName("Pruebas para el cierre de Tickets")
    public class CerrarTicket {

        @Test
        @DisplayName("Cierra un ticket exitosamente y notifica los eventos correspondientes")
        public void cerrarTicketCorrectamente() {
            Ticket ticketOriginal = mock(Ticket.class);
            Ticket ticketCerrado = mock(Ticket.class);
            Instant ahora = Instant.now();

            when(reloj.ahora()).thenReturn(ahora);
            when(ticketOriginal.cerrado(ahora)).thenReturn(ticketCerrado);
            when(ticketCerrado.usuarioId()).thenReturn("User1");
            when(ticketCerrado.ticketId()).thenReturn("T-1");

            Ticket resultado = service.cerrar(ticketOriginal);

            assertEquals(ticketCerrado, resultado);
            verify(repo).actualizar(ticketCerrado);
            verify(notificador).notificarCierre("User1", "T-1");
        }

        @Test
        @DisplayName("Lanza excepcion cuando se intenta cerrar un ticket nulo")
        public void cerrarTicketNuloLanzaExcepcion() {
            assertThrows(IllegalArgumentException.class, () -> service.cerrar(null));
        }

        @Test
        @DisplayName("Verifica que el reloj es consultado al momento exacto de cerrar")
        public void cerrarVerificaLlamadaAlReloj() {
            Ticket ticketOriginal = mock(Ticket.class);
            Ticket ticketCerrado = mock(Ticket.class);
            Instant ahora = Instant.now();


            // esto dice, cuando te pida la hora en este momento damela
            when(reloj.ahora()).thenReturn(ahora);
            when(ticketOriginal.cerrado(ahora)).thenReturn(ticketCerrado);

            service.cerrar(ticketOriginal);
            // cuando cerramos el ticket verificamos que la hora cerrada para ticket y reloj sea la misma.
            verify(reloj).ahora();
            verify(ticketOriginal).cerrado(ahora);
        }

        @Test
        @DisplayName("Si el repositorio falla al actualizar, no se debe notificar el cierre")
        public void cerrarFalloRepoImpideNotificacion() {
            Ticket ticketOriginal = mock(Ticket.class);
            Ticket ticketCerrado = mock(Ticket.class);
            Instant ahora = Instant.now();

            when(reloj.ahora()).thenReturn(ahora);
            when(ticketOriginal.cerrado(ahora)).thenReturn(ticketCerrado);
            // mandamos un error en la 'bd' mockeada, para preveer que no mande una notirifacion de ticket
            doThrow(new RuntimeException("Error en DB")).when(repo).actualizar(ticketCerrado);

            assertThrows(RuntimeException.class, () -> service.cerrar(ticketOriginal));
            verifyNoInteractions(notificador);
        }

        @Test
        @DisplayName("Verifica que no hay mas interacciones de las esperadas al cerrar")
        public void cerrarVerificaNoMoreInteractions() {
            Ticket ticketOriginal = mock(Ticket.class);
            Ticket ticketCerrado = mock(Ticket.class);

            when(ticketOriginal.cerrado(any())).thenReturn(ticketCerrado);

            service.cerrar(ticketOriginal);

            verify(repo).actualizar(ticketCerrado);
            verifyNoMoreInteractions(repo);
        }
    }

    @Nested
    @DisplayName("Pruebas para la cancelacion de Tickets")
    public class CancelarTicket {

        @Test
        @DisplayName("Cancela un ticket exitosamente ")
        public void cancelarTicketCorrecto() {
            service.cancelar("User1", "T-1", "Ya no usare el servicio");
            verify(notificador).notificarCancelacion("User1", "T-1", "Ya no usare el servicio");
        }

        @Test
        @DisplayName("Lanza excepcion si el ID de usuario es nulo")
        public void cancelarUsuarioNuloLanzaExcepcion() {
            assertThrows(IllegalArgumentException.class, () -> service.cancelar(null, "T-1", "id null"));
            verifyNoInteractions(notificador);
        }

        @Test
        @DisplayName("Lanza excepcion si el ID del ticket esta en blanco")
        public void cancelarTicketVacioLanzaExcepcion() {
            assertThrows(IllegalArgumentException.class, () -> service.cancelar("User1", "   ", "id en blanco"));
        }

        @Test
        @DisplayName("Lanza excepcion si el motivo es nulo")
        public void cancelarMotivoNuloLanzaExcepcion() {
            assertThrows(IllegalArgumentException.class, () -> service.cancelar("User1", "T-1", null));
        }

        @Test
        @DisplayName("Verifica que una excepcion sea lanzada cuando el id del ticket es null")
        public void cancelarticketIdEsNull() {
            assertThrows(IllegalArgumentException.class, () -> service.cancelar(" User1 ", null, " Motivo "));


        }
        @Test
        @DisplayName("Verifica que una excepcion sea lanzada cuando el id del usuario es null")
        public void cancelarUsuarioTicketEsNull() {
            assertThrows(IllegalArgumentException.class, () -> service.cancelar(null, " T-1 ", " Motivo "));


        }
    }
}