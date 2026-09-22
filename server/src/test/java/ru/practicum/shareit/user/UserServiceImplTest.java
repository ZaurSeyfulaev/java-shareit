package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.DuplicateEmailException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceImplTest {
    @Autowired
    private UserService userService;

    private UserDto user(String name, String email) {
        UserDto dto = new UserDto();
        dto.setName(name);
        dto.setEmail(email);
        return dto;
    }

    @Test
    void createGetUpdateDeleteUser() {
        UserDto created = userService.createUser(user("Ann", "ann@test.ru"));
        assertNotNull(created.getId());
        assertEquals("Ann", userService.getUserById(created.getId()).getName());

        created.setName("Anna");
        UserDto updated = userService.updateUser(created.getId(), created);
        assertEquals("Anna", updated.getName());

        userService.deleteUser(created.getId());
        assertThrows(RuntimeException.class, () -> userService.getUserById(created.getId()));
    }

    @Test
    void createUserWithSameEmailFails() {
        userService.createUser(user("Ann", "same@test.ru"));
        assertThrows(DuplicateEmailException.class,
                () -> userService.createUser(user("Bob", "same@test.ru")));
    }

    @Test
    void updateEmail() {
        UserDto first = userService.createUser(user("Ann", "ann2@test.ru"));
        UserDto second = userService.createUser(user("Bob", "bob2@test.ru"));

        UserDto sameEmail = new UserDto();
        sameEmail.setEmail(first.getEmail());
        assertEquals(first.getEmail(), userService.updateUser(first.getId(), sameEmail).getEmail());

        UserDto taken = new UserDto();
        taken.setEmail(first.getEmail());
        assertThrows(DuplicateEmailException.class,
                () -> userService.updateUser(second.getId(), taken));

        UserDto newMail = new UserDto();
        newMail.setEmail("new@test.ru");
        assertEquals("new@test.ru",
                userService.updateUser(second.getId(), newMail).getEmail());
        assertThrows(RuntimeException.class,
                () -> userService.updateUser(999L, newMail));
    }
}
