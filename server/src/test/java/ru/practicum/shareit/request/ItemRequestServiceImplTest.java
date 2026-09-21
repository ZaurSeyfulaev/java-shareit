package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemRequestServiceImplTest {
    @Autowired
    private ItemRequestService itemRequestService;
    @Autowired
    private UserService userService;

    private UserDto user(String name, String email) {
        UserDto dto = new UserDto();
        dto.setName(name);
        dto.setEmail(email);
        return userService.createUser(dto);
    }

    @Test
    void createAndGetRequests() {
        UserDto first = user("First", "first@test.ru");
        UserDto second = user("Second", "second@test.ru");

        ItemRequestDto dto = new ItemRequestDto();
        dto.setDescription("Need a saw");
        ItemRequestDto created = itemRequestService.create(first.getId(), dto);
        assertNotNull(created.getId());
        assertNotNull(created.getCreated());

        assertEquals(1, itemRequestService.getOwn(first.getId()).size());
        assertTrue(itemRequestService.getAll(first.getId()).isEmpty());
        assertEquals(1, itemRequestService.getAll(second.getId()).size());
        assertEquals("Need a saw",
                itemRequestService.getById(second.getId(), created.getId()).getDescription());
    }
}
