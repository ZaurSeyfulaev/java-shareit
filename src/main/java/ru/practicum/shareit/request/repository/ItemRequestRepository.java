package ru.practicum.shareit.request.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.request.ItemRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class ItemRequestRepository {
    private final Map<Long, ItemRequest> requests = new HashMap<>();
    private Long idCounter = 1L;

    public ItemRequest create(ItemRequest request) {
        request.setId(idCounter++);
        requests.put(request.getId(), request);
        return request;
    }

    public ItemRequest findById(Long id) {
        ItemRequest request = requests.get(id);
        if (request == null) {
            throw new RuntimeException("Запрос с id " + id + " не найден");
        }
        return request;
    }

    public List<ItemRequest> findByRequestorId(Long requestorId) {
        return requests.values().stream()
                .filter(r -> r.getRequestor().getId().equals(requestorId))
                .collect(Collectors.toList());
    }

    public List<ItemRequest> findAll() {
        return new ArrayList<>(requests.values());
    }
}