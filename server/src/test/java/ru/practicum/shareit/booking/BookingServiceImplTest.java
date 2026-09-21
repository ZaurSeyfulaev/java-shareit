package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.emum.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BookingServiceImplTest {
    @Autowired
    private BookingService bookingService;
    @Autowired
    private UserService userService;
    @Autowired
    private ItemService itemService;

    private UserDto user(String name, String email) {
        UserDto dto = new UserDto();
        dto.setName(name);
        dto.setEmail(email);
        return userService.createUser(dto);
    }

    @Test
    void createApproveAndGetBookings() {
        UserDto owner = user("Owner", "owner-b@test.ru");
        UserDto booker = user("Booker", "booker@test.ru");
        ItemDto item = new ItemDto();
        item.setName("Saw");
        item.setDescription("Hand saw");
        item.setAvailable(true);
        ItemDto savedItem = itemService.createItem(owner.getId(), item);

        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(savedItem.getId());
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));

        BookingResponseDto created = bookingService.createBooking(booker.getId(), bookingDto);
        assertEquals(BookingStatus.WAITING, created.getStatus());

        BookingResponseDto approved = bookingService.approveBooking(owner.getId(), created.getId(), true);
        assertEquals(BookingStatus.APPROVED, approved.getStatus());
        assertEquals(created.getId(),
                bookingService.getBookingById(booker.getId(), created.getId()).getId());
        assertEquals(1, bookingService.getBookingsByBooker(booker.getId(), "ALL").size());
        assertEquals(1, bookingService.getBookingsByOwner(owner.getId(), "ALL").size());
    }

    @Test
    void ownerCannotBookOwnItem() {
        UserDto owner = user("Owner2", "owner2@test.ru");
        ItemDto item = new ItemDto();
        item.setName("Hammer");
        item.setDescription("Hammer");
        item.setAvailable(true);
        ItemDto savedItem = itemService.createItem(owner.getId(), item);

        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(savedItem.getId());
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));

        assertThrows(RuntimeException.class,
                () -> bookingService.createBooking(owner.getId(), bookingDto));
    }
}
