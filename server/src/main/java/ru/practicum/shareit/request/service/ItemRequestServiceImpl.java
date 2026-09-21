package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestMapper;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public ItemRequestDto create(Long userId, ItemRequestDto itemRequestDto) {
        User requestor = getUserOrThrow(userId);
        if (itemRequestDto.getDescription() == null || itemRequestDto.getDescription().isBlank()) {
            throw new IllegalArgumentException("Описание запроса не может быть пустым");
        }
        ItemRequest saved = itemRequestRepository.save(
                ItemRequestMapper.toItemRequest(itemRequestDto, requestor));
        return ItemRequestMapper.toItemRequestDto(saved);
    }

    @Override
    public List<ItemRequestDto> getOwn(Long userId) {
        getUserOrThrow(userId);
        List<ItemRequest> requests =
                itemRequestRepository.findAllByRequestorIdOrderByCreatedDesc(userId);
        return toDtosWithItems(requests);
    }

    @Override
    public List<ItemRequestDto> getAll(Long userId) {
        getUserOrThrow(userId);
        List<ItemRequest> requests =
                itemRequestRepository.findAllByRequestorIdNotOrderByCreatedDesc(userId);
        return toDtosWithItems(requests);
    }

    @Override
    public ItemRequestDto getById(Long userId, Long requestId) {
        getUserOrThrow(userId);
        ItemRequest request = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException(
                        "Запрос с id: " + requestId + " не найден"));

        List<Long> ids = new ArrayList<>();
        ids.add(requestId);
        List<ItemDto> answers = new ArrayList<>();
        for (Item item : itemRepository.findAllByRequestIdIn(ids)) {
            answers.add(ItemMapper.toItemDto(item));
        }
        return ItemRequestMapper.toItemRequestDto(request, answers);
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        "Пользователь с id: " + userId + " не найден"));
    }

    private List<ItemRequestDto> toDtosWithItems(List<ItemRequest> requests) {
        if (requests.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> requestIds = new ArrayList<>();
        for (ItemRequest request : requests) {
            requestIds.add(request.getId());
        }

        List<Item> items = itemRepository.findAllByRequestIdIn(requestIds);

        Map<Long, List<ItemDto>> itemsByRequestId = new HashMap<>();
        for (Item item : items) {
            Long requestId = item.getRequestId();
            if (!itemsByRequestId.containsKey(requestId)) {
                itemsByRequestId.put(requestId, new ArrayList<>());
            }
            itemsByRequestId.get(requestId).add(ItemMapper.toItemDto(item));
        }

        List<ItemRequestDto> result = new ArrayList<>();
        for (ItemRequest request : requests) {
            List<ItemDto> answers = itemsByRequestId.get(request.getId());
            if (answers == null) {
                answers = new ArrayList<>();
            }
            result.add(ItemRequestMapper.toItemRequestDto(request, answers));
        }
        return result;
    }
}