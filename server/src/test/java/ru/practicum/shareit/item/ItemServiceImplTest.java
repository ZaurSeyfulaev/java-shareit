package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.emum.BookingStatus;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemServiceImplTest {
    @Autowired
    private ItemService itemService;
    @Autowired
    private UserService userService;
    @Autowired
    private ItemRequestService itemRequestService;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;

    private UserDto user(String name, String email) {
        UserDto dto = new UserDto();
        dto.setName(name);
        dto.setEmail(email);
        return userService.createUser(dto);
    }

    private ItemDto item(String name) {
        ItemDto dto = new ItemDto();
        dto.setName(name);
        dto.setDescription("desc " + name);
        dto.setAvailable(true);
        return dto;
    }

    @Test
    void createGetUpdateSearchByOwner() {
        UserDto owner = user("Owner", "owner@test.ru");
        ItemDto created = itemService.createItem(owner.getId(), item("Drill"));
        assertEquals("Drill", itemService.getItemById(created.getId()).getName());

        created.setName("New drill");
        assertEquals("New drill", itemService.updateItem(owner.getId(), created.getId(), created).getName());

        List<ItemDto> found = itemService.searchItems("new");
        assertEquals(1, found.size());
        assertTrue(itemService.searchItems("").isEmpty());
        assertEquals(1, itemService.getItemsByOwner(owner.getId()).size());
    }

    @Test
    void createItemWithRequestId() {
        UserDto requestor = user("Req", "req@test.ru");
        UserDto owner = user("Own", "own@test.ru");
        ItemRequestDto request = new ItemRequestDto();
        request.setDescription("Need a brush");
        ItemRequestDto savedRequest = itemRequestService.create(requestor.getId(), request);

        ItemDto dto = item("Brush");
        dto.setRequestId(savedRequest.getId());
        ItemDto created = itemService.createItem(owner.getId(), dto);
        assertEquals(savedRequest.getId(), created.getRequestId());
        assertEquals(owner.getId(), created.getOwnerId());
        assertFalse(itemRequestService.getOwn(requestor.getId()).get(0).getItems().isEmpty());
    }

    @Test
    void createCommentAfterBooking() {
        UserDto owner = user("Own2", "own2@test.ru");
        UserDto booker = user("Book", "book@test.ru");
        ItemDto saved = itemService.createItem(owner.getId(), item("Lamp"));
        Item itemEntity = itemRepository.findById(saved.getId()).orElseThrow();
        User bookerEntity = userRepository.findById(booker.getId()).orElseThrow();

        Booking past = new Booking();
        past.setStart(LocalDateTime.now().minusDays(4));
        past.setEnd(LocalDateTime.now().minusDays(3));
        past.setItem(itemEntity);
        past.setBooker(bookerEntity);
        past.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(past);

        Booking future = new Booking();
        future.setStart(LocalDateTime.now().plusDays(3));
        future.setEnd(LocalDateTime.now().plusDays(4));
        future.setItem(itemEntity);
        future.setBooker(bookerEntity);
        future.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(future);

        List<ItemDto> ownerItems = itemService.getItemsByOwner(owner.getId());
        assertNotNull(ownerItems.get(0).getLastBooking());
        assertNotNull(ownerItems.get(0).getNextBooking());

        CommentDto commentDto = new CommentDto();
        commentDto.setText("ok");
        assertEquals("ok", itemService.createComment(booker.getId(), saved.getId(), commentDto).getText());
        assertEquals(1, itemService.getItemById(saved.getId()).getComments().size());
    }

    @Test
    void updatePartAndErrors() {
        UserDto owner = user("OwnerP", "ownerp@test.ru");
        UserDto other = user("Other", "other@test.ru");
        ItemDto created = itemService.createItem(owner.getId(), item("Box"));

        ItemDto patch = new ItemDto();
        patch.setDescription("new desc");
        patch.setAvailable(false);
        ItemDto updated = itemService.updateItem(owner.getId(), created.getId(), patch);
        assertEquals("new desc", updated.getDescription());
        assertFalse(updated.getAvailable());

        assertThrows(RuntimeException.class,
                () -> itemService.updateItem(other.getId(), created.getId(), patch));

        CommentDto comment = new CommentDto();
        comment.setText("too soon");
        assertThrows(IllegalArgumentException.class,
                () -> itemService.createComment(other.getId(), created.getId(), comment));

        assertTrue(itemService.searchItems(null).isEmpty());
        assertThrows(RuntimeException.class,
                () -> itemService.getItemById(999L));
        assertThrows(RuntimeException.class,
                () -> itemService.createItem(999L, item("X")));
        assertThrows(RuntimeException.class,
                () -> itemService.getItemsByOwner(999L));
        assertThrows(RuntimeException.class,
                () -> itemService.updateItem(owner.getId(), 999L, patch));
    }
}
