package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.emum.BookingStatus;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.CommentMapper;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    public List<ItemDto> getItemsByOwner(Long ownerId) {
        userRepository.findById(ownerId).orElseThrow(
                () -> new NotFoundException("Владелец с id: " + ownerId + " не найден")
        );
        LocalDateTime now = LocalDateTime.now();
        return itemRepository.findAllByOwnerId(ownerId).stream()
                .map(item -> {
                    ItemDto dto = ItemMapper.toItemDto(item);
                    dto.setLastBooking(BookingMapper.toBookingShortsDto(
                            bookingRepository
                                    .findFirstByItemIdAndStartBeforeAndStatusOrderByStartDesc(
                                            item.getId(), now, BookingStatus.APPROVED)
                                    .orElse(null)));
                    dto.setNextBooking(BookingMapper.toBookingShortsDto(
                            bookingRepository
                                    .findFirstByItemIdAndStartAfterAndStatusOrderByStartAsc(
                                            item.getId(), now, BookingStatus.APPROVED)
                                    .orElse(null)));
                    dto.setComments(getComments(item.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public ItemDto getItemById(Long id) {
        Item item = itemRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Item не найден по id: " + id)
        );
        ItemDto dto = ItemMapper.toItemDto(item);
        dto.setComments(getComments(item.getId()));
        return dto;
    }

    @Override
    @Transactional
    public ItemDto createItem(Long userId, ItemDto itemDto) {
        User owner = userRepository.findById(userId)
                .orElseThrow(
                        () -> new NotFoundException("Пользователь с id: " + userId + " не найден")
                );
        Item item = ItemMapper.toItem(itemDto);
        itemParamValidator(item);
        item.setOwner(owner);
        Item createdItem = itemRepository.save(item);
        return ItemMapper.toItemDto(createdItem);
    }

    @Override
    @Transactional
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        Item existingItem = itemRepository.findById(itemId).orElseThrow(
                () -> new NotFoundException("Item не найден по id: " + itemId)
        );

        if (!existingItem.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не является владельцем вещи с id " + itemId);
        }

        Item updatedItem = ItemMapper.toItem(itemDto);
        updatedItem.setId(itemId);

        if (updatedItem.getName() != null && !updatedItem.getName().isBlank()) {
            existingItem.setName(updatedItem.getName());
        }

        if (updatedItem.getDescription() != null && !updatedItem.getDescription().isBlank()) {
            existingItem.setDescription(updatedItem.getDescription());
        }

        if (updatedItem.getAvailable() != null) {
            existingItem.setAvailable(updatedItem.getAvailable());
        }

        Item result = itemRepository.save(existingItem);
        return ItemMapper.toItemDto(result);
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        return itemRepository.search(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    private void itemParamValidator(Item item) {
        if (item.getName() == null || item.getName().isBlank()) {
            throw new IllegalArgumentException("Название не может быть пустым");
        }
        if (item.getDescription() == null || item.getDescription().isBlank()) {
            throw new IllegalArgumentException("Описание не может быть пустым");
        }
        if (item.getAvailable() == null) {
            throw new IllegalArgumentException("Статус доступности должен быть указан");
        }
    }
    @Override
    @Transactional
    public CommentDto createComment(Long userId, Long itemId, CommentDto commentDto) {
        User author = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("Пользователь с id: " + userId + " не найден")
        );
        Item item = itemRepository.findById(itemId).orElseThrow(
                () -> new NotFoundException("Вещь с id: " + itemId + " не найдена")
        );

        if (commentDto.getText() == null || commentDto.getText().isBlank()) {
            throw new IllegalArgumentException("Текст комментария не может быть пустым");
        }

        boolean rented = bookingRepository.existsByBookerIdAndItemIdAndEndBeforeAndStatus(
                userId, itemId, LocalDateTime.now(), BookingStatus.APPROVED);
        if (!rented) {
            throw new IllegalArgumentException(
                    "Комментарий можно оставить только после завершённой аренды");
        }

        Comment comment = new Comment();
        comment.setText(commentDto.getText());
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());

        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    private List<CommentDto> getComments(Long itemId) {
        return commentRepository.findAllByItemIdOrderByCreatedAsc(itemId).stream()
                .map(CommentMapper::toCommentDto)
                .toList();
    }
}