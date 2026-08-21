package ru.practicum.shareit.booking.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.Booking;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class BookingRepository {
    private final Map<Long, Booking> bookings = new HashMap<>();
    private Long idCounter = 1L;

    public Booking create(Booking booking) {
        booking.setId(idCounter++);
        bookings.put(booking.getId(), booking);
        return booking;
    }

    public Booking findById(Long id) {
        Booking booking = bookings.get(id);
        if (booking == null) {
            throw new RuntimeException("Бронирование с id " + id + " не найдено");
        }
        return booking;
    }

    public Booking update(Booking booking) {
        if (!bookings.containsKey(booking.getId())) {
            throw new RuntimeException("Бронирование с id " + booking.getId() + " не найдено");
        }
        bookings.put(booking.getId(), booking);
        return booking;
    }

    public List<Booking> findByBookerId(Long bookerId) {
        return bookings.values().stream().filter(b -> b.getBooker().getId().equals(bookerId)).collect(Collectors.toList());
    }

    public List<Booking> findByItemOwnerId(Long ownerId) {
        return bookings.values().stream().filter(b -> b.getItem().getOwner().getId().equals(ownerId)).collect(Collectors.toList());
    }
}