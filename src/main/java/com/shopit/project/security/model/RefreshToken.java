package com.shopit.project.security.model;

import com.shopit.project.model.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity(name = "refresh_tokens")
@Data
@NoArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long refreshTokenId;

    @NotNull
    @Column(unique = true)
    private String refreshToken;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @NotNull
    private LocalDateTime expiryDate;

    private LocalDateTime createdDate;
}
