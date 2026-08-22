package ru.practicum.shareit.user.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.DuplicateEmailException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.model.User;

import java.util.HashMap;

import java.util.Map;

@Repository
public class UserRepository {
    private final Map<Long, User> users = new HashMap<>();
    private Long idCounter = 1L;

    public User findById(Long id) {
        User user = users.get(id);
        if (user == null) {
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }
        return user;
    }

    public User create(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email не может быть пустым");
        }
        validateEmail(user.getEmail(), null);
        user.setId(idCounter++);
        users.put(user.getId(), user);
        return user;
    }

    public User update(User user) {
        User existingUser = findById(user.getId());

        if (user.getEmail() != null) {
            if (user.getEmail().isBlank()) {
                throw new IllegalArgumentException("Email не может быть пустым");
            }
            if (!user.getEmail().equals(existingUser.getEmail())) {
                validateEmail(user.getEmail(), user.getId());
                existingUser.setEmail(user.getEmail());
            }
        }

        if (user.getName() != null) {
            existingUser.setName(user.getName());
        }

        return existingUser;
    }

    public void delete(Long id) {
        if (users.remove(id) == null) {
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }
    }

    private void validateEmail(String email, Long userId) {
        boolean emailExists = users.values().stream()
                .filter(u -> !u.getId().equals(userId))
                .anyMatch(u -> u.getEmail().equals(email));

        if (emailExists) {
            throw new DuplicateEmailException("Email " + email + " уже используется");
        }
    }
}