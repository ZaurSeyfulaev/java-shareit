package ru.practicum.shareit.user.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * TODO Sprint add-controllers.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @NotBlank(message = "Имя не может быть пустым")
    @Column(columnDefinition = "TEXT")
    String name;

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Неверный формат email")
    @Column(columnDefinition = "TEXT", unique = true)
    String email;
}
