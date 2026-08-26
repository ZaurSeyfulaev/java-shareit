package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.model.User;

import java.util.Optional;


public interface UserRepository {


    Optional<User> findById(Long id);

    User create(User user);

    User update(User user);

    void delete(Long id);

    boolean validateEmail(String email, Long userId);
}