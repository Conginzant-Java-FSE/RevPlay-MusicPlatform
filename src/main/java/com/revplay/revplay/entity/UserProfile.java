package com.revplay.revplay.entity;

import com.revplay.revplay.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_profiles")
@Getter
@Setter
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long profileId;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private String fullName;

    @Column(columnDefinition = "TEXT")
    private String bio;

    private String profilePictureUrl;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}