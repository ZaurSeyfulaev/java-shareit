package ru.practicum.shareit.booking.service;


import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    public BookingServiceImpl(BookingRepository bookingRepository,
                              UserRepository userRepository,
                              ItemRepository itemRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
    }

    @Override
    public BookingDto createBooking(Long userId, BookingDto bookingDto) {
        User booker = userRepository.findById(userId);
        Item item = itemRepository.findById(bookingDto.getItemId());

        if (item.getOwner().getId().equals(userId)) {
            throw new IllegalArgumentException("Владелец не может бронировать свою вещь");
        }

        if (!item.getAvailable()) {
            throw new IllegalArgumentException("Вещь недоступна для бронирования");
        }

        Booking booking = new Booking();
        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);

        Booking created = bookingRepository.create(booking);
        return toBookingDto(created);
    }

    @Override
    public BookingDto approveBooking(Long userId, Long bookingId, Boolean approved) {
        Booking booking = bookingRepository.findById(bookingId);

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new NotFoundException("Только владелец может подтвердить бронирование");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new IllegalArgumentException("Бронирование уже обработано");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        Booking updated = bookingRepository.update(booking);
        return toBookingDto(updated);
    }

    @Override
    public BookingDto getBookingById(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId);

        if (!booking.getBooker().getId().equals(userId) &&
                !booking.getItem().getOwner().getId().equals(userId)) {
            throw new NotFoundException("У вас нет доступа к этому бронированию");
        }

        return toBookingDto(booking);
    }

    @Override
    public List<BookingDto> getBookingsByUser(Long userId, String state) {
        userRepository.findById(userId);
        List<Booking> bookings = bookingRepository.findByBookerId(userId);
        return filterByState(bookings, state).stream()
                .map(this::toBookingDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> getBookingsByOwner(Long userId, String state) {
        userRepository.findById(userId);
        List<Booking> bookings = bookingRepository.findByItemOwnerId(userId);
        return filterByState(bookings, state).stream()
                .map(this::toBookingDto)
                .collect(Collectors.toList());
    }

    private List<Booking> filterByState(List<Booking> bookings, String state) {
        if (state == null || state.equals("ALL")) {
            return bookings;
        }
        return bookings.stream()
                .filter(b -> b.getStatus().name().equals(state))
                .collect(Collectors.toList());
    }

    private BookingDto toBookingDto(Booking booking) {
        return new BookingDto(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                booking.getItem().getId(),
                booking.getBooker().getId(),
                booking.getStatus()
        );
    }
}