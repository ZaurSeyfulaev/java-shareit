package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestDtoJsonTest {
    @Autowired
    private JacksonTester<ItemRequestDto> json;

    @Test
    void shouldSerializeCreated() throws Exception {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(1L);
        dto.setDescription("Need a drill");
        dto.setCreated(LocalDateTime.of(2026, 9, 19, 20, 0));
        dto.setItems(List.of());

        assertThat(json.write(dto)).extractingJsonPathStringValue("$.created")
                .startsWith("2026-09-19T20:00:00");
        assertThat(json.write(dto)).extractingJsonPathStringValue("$.description")
                .isEqualTo("Need a drill");
    }
}
