package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper mapper;
    @MockBean
    private ItemService itemService;

    @Test
    void itemEndpoints() throws Exception {
        ItemDto dto = new ItemDto();
        dto.setId(1L);
        dto.setName("Drill");
        dto.setDescription("desc");
        dto.setAvailable(true);
        when(itemService.createItem(eq(1L), any())).thenReturn(dto);
        when(itemService.getItemById(1L)).thenReturn(dto);
        when(itemService.getItemsByOwner(1L)).thenReturn(List.of(dto));
        when(itemService.updateItem(eq(1L), eq(1L), any())).thenReturn(dto);
        when(itemService.searchItems("drill")).thenReturn(List.of(dto));
        CommentDto comment = new CommentDto();
        comment.setId(1L);
        comment.setText("ok");
        when(itemService.createComment(eq(1L), eq(1L), any())).thenReturn(comment);

        mvc.perform(post("/items").header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Drill"));
        mvc.perform(get("/items/1")).andExpect(status().isOk());
        mvc.perform(get("/items").header("X-Sharer-User-Id", 1)).andExpect(status().isOk());
        mvc.perform(patch("/items/1").header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
        mvc.perform(get("/items/search").param("text", "drill")).andExpect(status().isOk());
        mvc.perform(post("/items/1/comment").header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(comment)))
                .andExpect(status().isOk());
    }
}
