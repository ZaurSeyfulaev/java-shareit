package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository requestRepository;
    private final UserRepository userRepository;

    @Override
    public ItemRequestDto createRequest(Long userId, ItemRequestDto requestDto) {
        User requestor = userRepository.findById(userId);

        ItemRequest request = new ItemRequest();
        request.setDescription(requestDto.getDescription());
        request.setRequestor(requestor);
        request.setCreated(LocalDateTime.now());

        ItemRequest created = requestRepository.create(request);
        return toItemRequestDto(created);
    }

    @Override
    public List<ItemRequestDto> getRequestsByUser(Long userId) {
        userRepository.findById(userId);
        return requestRepository.findByRequestorId(userId).stream()
                .map(this::toItemRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDto> getAllRequests(Long userId) {
        userRepository.findById(userId);
        return requestRepository.findAll().stream()
                .map(this::toItemRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestDto getRequestById(Long userId, Long requestId) {
        userRepository.findById(userId);
        ItemRequest request = requestRepository.findById(requestId);
        return toItemRequestDto(request);
    }

    private ItemRequestDto toItemRequestDto(ItemRequest request) {
        return new ItemRequestDto(
                request.getId(),
                request.getDescription(),
                request.getRequestor().getId(),
                request.getCreated()
        );
    }
}