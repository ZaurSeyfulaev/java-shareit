package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.emum.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper mapper;
    @MockBean
    private BookingService bookingService;

    @Test
    void bookingEndpoints() throws Exception {
        BookingResponseDto response = new BookingResponseDto();
        response.setId(1L);
        response.setStatus(BookingStatus.WAITING);
        response.setStart(LocalDateTime.now().plusDays(1));
        response.setEnd(LocalDateTime.now().plusDays(2));
        when(bookingService.createBooking(eq(2L), any())).thenReturn(response);
        when(bookingService.approveBooking(1L, 1L, true)).thenReturn(response);
        when(bookingService.getBookingById(2L, 1L)).thenReturn(response);
        when(bookingService.getBookingsByBooker(2L, "ALL")).thenReturn(List.of(response));
        when(bookingService.getBookingsByOwner(1L, "ALL")).thenReturn(List.of(response));

        BookingDto dto = new BookingDto();
        dto.setItemId(5L);
        dto.setStart(LocalDateTime.now().plusDays(1));
        dto.setEnd(LocalDateTime.now().plusDays(2));

        mvc.perform(post("/bookings").header("X-Sharer-User-Id", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
        mvc.perform(patch("/bookings/1").header("X-Sharer-User-Id", 1)
                        .param("approved", "true"))
                .andExpect(status().isOk());
        mvc.perform(get("/bookings/1").header("X-Sharer-User-Id", 2))
                .andExpect(status().isOk());
        mvc.perform(get("/bookings").header("X-Sharer-User-Id", 2))
                .andExpect(status().isOk());
        mvc.perform(get("/bookings/owner").header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk());
    }
}
