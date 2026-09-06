package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query("select i from Item i " +
            "where i.available = true" +
            " AND (UPPER(i.name) like UPPER((concat('%', ?1, '%')))" +
            " or UPPER(i.description) like UPPER(concat('%', ?1, '%')))")
    List<Item> search(String search);

    List<Item> findAllByOwnerId(Long id);
}