package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.emum.BookingState;
import ru.practicum.shareit.emum.BookingStatus;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;

    @Override
    @Transactional
    public BookingResponseDto createBooking(Long userId, BookingDto bookingDto) {
        validateDates(bookingDto);

        User booker = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException(
                        "Пользователь с id: " + userId + " не найден")
        );

        Long itemId = bookingDto.getItemId();
        Item item = itemRepository.findById(itemId).orElseThrow(
                () -> new NotFoundException(
                        "Вещь с id: " + itemId + " не найдена")
        );

        if (item.getOwner().getId().equals(userId)) {
            throw new NotFoundException(
                    "Владелец не может бронировать свою вещь");
        }

        if (!item.getAvailable()) {
            throw new IllegalArgumentException(
                    "Вещь недоступна для бронирования");
        }

        Booking booking = new Booking();
        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);

        return BookingMapper.toBookingResponseDto(
                bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingResponseDto approveBooking(
            Long userId, Long bookingId, Boolean approved) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(
                () -> new NotFoundException(
                        "Бронирование с id: " + bookingId + " не найдено")
        );

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new IllegalArgumentException(
                    "Только владелец может подтвердить бронирование");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new IllegalArgumentException(
                    "Бронирование уже обработано");
        }

        booking.setStatus(
                approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);

        return BookingMapper.toBookingResponseDto(
                bookingRepository.save(booking));
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponseDto getBookingById(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(
                () -> new NotFoundException(
                        "Бронирование с id: " + bookingId + " не найдено")
        );

        boolean isBooker = booking.getBooker().getId().equals(userId);
        boolean isOwner = booking.getItem().getOwner().getId().equals(userId);

        if (!isBooker && !isOwner) {
            throw new NotFoundException(
                    "Нет доступа к бронированию с id: " + bookingId);
        }

        return BookingMapper.toBookingResponseDto(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDto> getBookingsByBooker(
            Long userId, String state) {
        checkUserExists(userId);
        LocalDateTime now = LocalDateTime.now();

        BookingState bookingState = BookingState.valueOf(state.toUpperCase());

        List<Booking> bookings = switch (bookingState) {
            case ALL -> bookingRepository
                    .findAllByBookerIdOrderByStartDesc(userId);
            case CURRENT -> bookingRepository
                    .findAllByBookerIdAndStartLessThanEqualAndEndGreaterThanEqualOrderByStartDesc(
                            userId, now, now);
            case PAST -> bookingRepository
                    .findAllByBookerIdAndEndBeforeOrderByStartDesc(
                            userId, now);
            case FUTURE -> bookingRepository
                    .findAllByBookerIdAndStartAfterOrderByStartDesc(
                            userId, now);
            case WAITING -> bookingRepository
                    .findAllByBookerIdAndStatusOrderByStartDesc(
                            userId, BookingStatus.WAITING);
            case REJECTED -> bookingRepository
                    .findAllByBookerIdAndStatusOrderByStartDesc(
                            userId, BookingStatus.REJECTED);
        };

        return bookings.stream()
                .map(BookingMapper::toBookingResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDto> getBookingsByOwner(
            Long userId, String state) {
        checkUserExists(userId);

        BookingState bookingState = BookingState.valueOf(state.toUpperCase());

        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = switch (bookingState) {
            case ALL -> bookingRepository
                    .findAllByItemOwnerIdOrderByStartDesc(userId);
            case CURRENT -> bookingRepository
                    .findAllByItemOwnerIdAndStartLessThanEqualAndEndGreaterThanEqualOrderByStartDesc(
                            userId, now, now);
            case PAST -> bookingRepository
                    .findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(
                            userId, now);
            case FUTURE -> bookingRepository
                    .findAllByItemOwnerIdAndStartAfterOrderByStartDesc(
                            userId, now);
            case WAITING -> bookingRepository
                    .findAllByItemOwnerIdAndStatusOrderByStartDesc(
                            userId, BookingStatus.WAITING);
            case REJECTED -> bookingRepository
                    .findAllByItemOwnerIdAndStatusOrderByStartDesc(
                            userId, BookingStatus.REJECTED);
        };

        return bookings.stream()
                .map(BookingMapper::toBookingResponseDto)
                .toList();
    }

    private void validateDates(BookingDto bookingDto) {
        if (bookingDto.getStart() == null || bookingDto.getEnd() == null) {
            throw new IllegalArgumentException(
                    "Даты бронирования обязательны");
        }
        if (!bookingDto.getStart().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException(
                    "Дата начала должна быть в будущем");
        }
        if (!bookingDto.getEnd().isAfter(bookingDto.getStart())) {
            throw new IllegalArgumentException(
                    "Дата окончания должна быть позже даты начала");
        }
    }

    private void checkUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(
                    "Пользователь с id: " + userId + " не найден");
        }
    }
}