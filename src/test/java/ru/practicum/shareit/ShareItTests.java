package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ShareItTests {

    @Autowired
    private UserController userController;

    @Autowired
    private ItemController itemController;

    private UserDto testUserDto;
    private ItemDto testItemDto;

    @BeforeEach
    void setUp() {
        testUserDto = createTestUserDto();
        testItemDto = createTestItemDto();
    }

    private UserDto createTestUserDto() {
        UserDto userDto = new UserDto();
        userDto.setName("User_" + UUID.randomUUID());
        userDto.setEmail("user_" + UUID.randomUUID() + "@test.ru");
        return userDto;
    }

    private ItemDto createTestItemDto() {
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Item_" + UUID.randomUUID());
        itemDto.setDescription("Description_" + UUID.randomUUID());
        itemDto.setAvailable(true);
        return itemDto;
    }

    @Test
    void createUserShouldSetIdAndReturnUser() {
        UserDto created = userController.createUser(testUserDto);
        assertNotNull(created.getId(), "ID пользователя должен быть установлен");
        assertTrue(created.getId() > 0, "ID пользователя должен быть больше нуля");
        assertEquals(testUserDto.getName(), created.getName(), "Имя пользователя должно совпадать с переданным");
        assertEquals(testUserDto.getEmail(), created.getEmail(), "Email пользователя должен совпадать с переданным");
    }

    @Test
    void getUserByIdShouldReturnUser() {
        UserDto created = userController.createUser(testUserDto);
        UserDto found = userController.getUserById(created.getId());
        assertEquals(created.getId(), found.getId(), "ID найденного пользователя должен совпадать с ID созданного");
        assertEquals(created.getName(), found.getName(), "Имя найденного пользователя должно совпадать");
    }

    @Test
    void updateUserShouldUpdateFields() {
        UserDto created = userController.createUser(testUserDto);
        created.setName("Обновлённое имя");
        created.setEmail("updated@test.ru");

        UserDto updated = userController.updateUser(created.getId(), created);
        assertEquals("Обновлённое имя", updated.getName(), "Имя пользователя должно обновиться");
        assertEquals("updated@test.ru", updated.getEmail(), "Email пользователя должен обновиться");
    }

    @Test
    void createItemShouldSetIdAndReturnItem() {
        UserDto user = userController.createUser(testUserDto);
        ItemDto created = itemController.createItem(user.getId(), testItemDto);
        assertNotNull(created.getId(), "ID вещи должен быть установлен");
        assertTrue(created.getId() > 0, "ID вещи должен быть больше нуля");
        assertEquals(testItemDto.getName(), created.getName(), "Название вещи должно совпадать с переданным");
        assertEquals(testItemDto.getDescription(), created.getDescription(), "Описание вещи должно совпадать с переданным");
        assertTrue(created.getAvailable(), "Статус доступности вещи должен быть true");
    }

    @Test
    void getItemByIdShouldReturnItem() {
        UserDto user = userController.createUser(testUserDto);
        ItemDto created = itemController.createItem(user.getId(), testItemDto);
        ItemDto found = itemController.getItemById(created.getId());
        assertEquals(created.getId(), found.getId(), "ID найденной вещи должен совпадать с ID созданной");
        assertEquals(created.getName(), found.getName(), "Название найденной вещи должно совпадать");
    }

    @Test
    void getItemsByOwnerShouldReturnUserItems() {
        UserDto user = userController.createUser(testUserDto);
        itemController.createItem(user.getId(), testItemDto);
        ItemDto secondItem = createTestItemDto();
        itemController.createItem(user.getId(), secondItem);

        List<ItemDto> items = itemController.getItemsByOwner(user.getId());
        assertEquals(2, items.size(), "У владельца должно быть две вещи");
    }

    @Test
    void updateItemShouldUpdateFields() {
        UserDto user = userController.createUser(testUserDto);
        ItemDto created = itemController.createItem(user.getId(), testItemDto);
        created.setName("Обновлённая вещь");
        created.setDescription("Новое описание");
        created.setAvailable(false);

        ItemDto updated = itemController.updateItem(user.getId(), created.getId(), created);
        assertEquals("Обновлённая вещь", updated.getName(), "Название вещи должно обновиться");
        assertEquals("Новое описание", updated.getDescription(), "Описание вещи должно обновиться");
        assertFalse(updated.getAvailable(), "Статус доступности вещи должен стать false");
    }

    @Test
    void searchItemsShouldReturnMatchingItems() {
        UserDto user = userController.createUser(testUserDto);
        itemController.createItem(user.getId(), testItemDto);

        ItemDto searchItem = createTestItemDto();
        String searchName = "ИскатьМеня" + UUID.randomUUID();
        searchItem.setName(searchName);
        itemController.createItem(user.getId(), searchItem);

        List<ItemDto> results = itemController.searchItems(searchName);
        assertEquals(1, results.size(), "Должна найтись ровно одна вещь");
        assertEquals(searchName, results.get(0).getName(), "Имя найденной вещи должно совпадать с искомым");
    }
}