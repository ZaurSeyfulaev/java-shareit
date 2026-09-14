package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.DuplicateEmailException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Пользователь с id: " + id + " не найден")
        );
        return UserMapper.toUserDto(user);
    }

    @Override
    @Transactional
    public UserDto createUser(UserDto userDto) {
        User user = UserMapper.toUser(userDto);

        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email не может быть пустым");
        }

        boolean emailExists = userRepository.existsByEmail(user.getEmail());
        if (emailExists) {
            throw new DuplicateEmailException("Email " + user.getEmail() + " уже используется");
        }

        User createdUser = userRepository.save(user);
        return UserMapper.toUserDto(createdUser);
    }

    @Override
    @Transactional
    public UserDto updateUser(Long id, UserDto userDto) {
        User existingUser = userRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Пользователь с id: " + id + " не найден")
        );
        User user = UserMapper.toUser(userDto);
        user.setId(id);

        if (user.getEmail() != null) {
            if (user.getEmail().isBlank()) {
                throw new IllegalArgumentException("Email не может быть пустым");
            }
            if (!user.getEmail().equals(existingUser.getEmail())) {
                boolean emailExists = userRepository.existsByEmailAndIdNot(user.getEmail(), user.getId());
                if (emailExists) {
                    throw new DuplicateEmailException("Email " + user.getEmail() + " уже используется");
                }
                existingUser.setEmail(user.getEmail());
            }
        }

        if (user.getName() != null) {
            existingUser.setName(user.getName());
        }

        User updatedUser = userRepository.save(existingUser);
        return UserMapper.toUserDto(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Пользователь с id: " + id + " не найден")
        );

        userRepository.delete(user);
    }
}