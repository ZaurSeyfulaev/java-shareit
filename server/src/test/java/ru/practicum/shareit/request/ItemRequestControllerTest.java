package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper mapper;
    @MockBean
    private ItemRequestService itemRequestService;

    @Test
    void requestEndpoints() throws Exception {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(1L);
        dto.setDescription("Need a saw");
        dto.setCreated(LocalDateTime.now());
        dto.setItems(List.of());
        when(itemRequestService.create(eq(1L), any())).thenReturn(dto);
        when(itemRequestService.getOwn(1L)).thenReturn(List.of(dto));
        when(itemRequestService.getAll(2L)).thenReturn(List.of(dto));
        when(itemRequestService.getById(2L, 1L)).thenReturn(dto);

        mvc.perform(post("/requests").header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value("Need a saw"));
        mvc.perform(get("/requests").header("X-Sharer-User-Id", 1)).andExpect(status().isOk());
        mvc.perform(get("/requests/all").header("X-Sharer-User-Id", 2)).andExpect(status().isOk());
        mvc.perform(get("/requests/1").header("X-Sharer-User-Id", 2)).andExpect(status().isOk());
    }
}
