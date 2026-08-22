package ru.practicum.shareit.item.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class ItemRepository {
    private final Map<Long, Item> items = new HashMap<>();
    private Long idCounter = 1L;

    public List<Item> findByOwner(Long ownerId) {
        return items.values().stream()
                .filter(item -> item.getOwner() != null && item.getOwner().getId().equals(ownerId))
                .collect(Collectors.toList());
    }

    public Item findById(Long id) {
        Item item = items.get(id);
        if (item == null) {
            throw new NotFoundException("Вещь с id " + id + " не найдена");
        }
        return item;
    }

    public Item create(Item item) {
        if (item.getName() == null || item.getName().isBlank()) {
            throw new IllegalArgumentException("Название не может быть пустым");
        }
        if (item.getDescription() == null || item.getDescription().isBlank()) {
            throw new IllegalArgumentException("Описание не может быть пустым");
        }
        if (item.getAvailable() == null) {
            throw new IllegalArgumentException("Статус доступности должен быть указан");
        }
        item.setId(idCounter++);
        items.put(item.getId(), item);
        return item;
    }

    public Item update(Item item) {
        if (!items.containsKey(item.getId())) {
            throw new NotFoundException("Вещь с id " + item.getId() + " не найдена");
        }

        Item existingItem = items.get(item.getId());

        if (item.getName() != null && !item.getName().isBlank()) {
            existingItem.setName(item.getName());
        }

        if (item.getDescription() != null && !item.getDescription().isBlank()) {
            existingItem.setDescription(item.getDescription());
        }

        if (item.getAvailable() != null) {
            existingItem.setAvailable(item.getAvailable());
        }

        items.put(item.getId(), existingItem);
        return existingItem;
    }

    public List<Item> search(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        String lowerCaseText = text.toLowerCase().trim();
        return items.values().stream()
                .filter(item -> Boolean.TRUE.equals(item.getAvailable()))
                .filter(item ->
                        (item.getName() != null && item.getName().toLowerCase().contains(lowerCaseText)) ||
                                (item.getDescription() != null && item.getDescription().toLowerCase().contains(lowerCaseText))
                )
                .collect(Collectors.toList());
    }
}