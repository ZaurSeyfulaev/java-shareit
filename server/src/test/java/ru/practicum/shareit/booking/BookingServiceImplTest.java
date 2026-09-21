package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.emum.BookingStatus;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
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

        assertThrows(NotFoundException.class,
                () -> bookingService.createBooking(owner.getId(), bookingDto));
    }

    @Test
    void bookingsByState() {
        UserDto owner = user("OwnerS", "owners@test.ru");
        UserDto booker = user("BookerS", "bookers@test.ru");
        ItemDto savedItem = itemService.createItem(owner.getId(), item("Bike"));

        BookingDto waitingDto = booking(savedItem.getId(), 2, 3);
        bookingService.createBooking(booker.getId(), waitingDto);

        BookingDto rejectDto = booking(savedItem.getId(), 4, 5);
        BookingResponseDto toReject = bookingService.createBooking(booker.getId(), rejectDto);
        assertEquals(BookingStatus.REJECTED,
                bookingService.approveBooking(owner.getId(), toReject.getId(), false).getStatus());

        Item itemEntity = itemRepository.findById(savedItem.getId()).orElseThrow();
        User bookerEntity = userRepository.findById(booker.getId()).orElseThrow();
        saveBooking(itemEntity, bookerEntity, -1, 1, BookingStatus.APPROVED);
        saveBooking(itemEntity, bookerEntity, -5, -4, BookingStatus.APPROVED);

        assertEquals(1, bookingService.getBookingsByBooker(booker.getId(), "CURRENT").size());
        assertEquals(1, bookingService.getBookingsByBooker(booker.getId(), "PAST").size());
        assertEquals(2, bookingService.getBookingsByBooker(booker.getId(), "FUTURE").size());
        assertEquals(1, bookingService.getBookingsByBooker(booker.getId(), "WAITING").size());
        assertEquals(1, bookingService.getBookingsByBooker(booker.getId(), "REJECTED").size());

        assertEquals(1, bookingService.getBookingsByOwner(owner.getId(), "CURRENT").size());
        assertEquals(1, bookingService.getBookingsByOwner(owner.getId(), "PAST").size());
        assertEquals(2, bookingService.getBookingsByOwner(owner.getId(), "FUTURE").size());
        assertEquals(1, bookingService.getBookingsByOwner(owner.getId(), "WAITING").size());
        assertEquals(1, bookingService.getBookingsByOwner(owner.getId(), "REJECTED").size());
    }

    @Test
    void bookingErrors() {
        UserDto owner = user("OwnerE", "ownere@test.ru");
        UserDto booker = user("BookerE", "bookere@test.ru");
        UserDto stranger = user("Stranger", "stranger@test.ru");
        ItemDto savedItem = itemService.createItem(owner.getId(), item("Drill"));

        BookingDto badDates = booking(savedItem.getId(), 2, 1);
        assertThrows(IllegalArgumentException.class,
                () -> bookingService.createBooking(booker.getId(), badDates));

        BookingDto noDates = new BookingDto();
        noDates.setItemId(savedItem.getId());
        assertThrows(IllegalArgumentException.class,
                () -> bookingService.createBooking(booker.getId(), noDates));

        ItemDto unavailable = item("Locked");
        unavailable.setAvailable(false);
        ItemDto locked = itemService.createItem(owner.getId(), unavailable);
        assertThrows(IllegalArgumentException.class,
                () -> bookingService.createBooking(booker.getId(), booking(locked.getId(), 1, 2)));

        BookingResponseDto created = bookingService.createBooking(
                booker.getId(), booking(savedItem.getId(), 1, 2));
        assertThrows(IllegalArgumentException.class,
                () -> bookingService.approveBooking(booker.getId(), created.getId(), true));
        bookingService.approveBooking(owner.getId(), created.getId(), true);
        assertThrows(IllegalArgumentException.class,
                () -> bookingService.approveBooking(owner.getId(), created.getId(), true));

        assertThrows(NotFoundException.class,
                () -> bookingService.getBookingById(stranger.getId(), created.getId()));
        assertThrows(NotFoundException.class,
                () -> bookingService.getBookingsByBooker(999L, "ALL"));
        assertThrows(NotFoundException.class,
                () -> bookingService.createBooking(999L, booking(savedItem.getId(), 1, 2)));
    }

    private ItemDto item(String name) {
        ItemDto dto = new ItemDto();
        dto.setName(name);
        dto.setDescription("desc " + name);
        dto.setAvailable(true);
        return dto;
    }

    private BookingDto booking(Long itemId, int startDays, int endDays) {
        BookingDto dto = new BookingDto();
        dto.setItemId(itemId);
        dto.setStart(LocalDateTime.now().plusDays(startDays));
        dto.setEnd(LocalDateTime.now().plusDays(endDays));
        return dto;
    }

    private void saveBooking(Item item, User booker, int startDays, int endDays, BookingStatus status) {
        Booking booking = new Booking();
        booking.setStart(LocalDateTime.now().plusDays(startDays));
        booking.setEnd(LocalDateTime.now().plusDays(endDays));
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(status);
        bookingRepository.save(booking);
    }
}
