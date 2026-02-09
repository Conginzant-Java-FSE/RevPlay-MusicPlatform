package com.revplay.revplay.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

    @Entity
    @Table(name = "genres")
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public class Genre {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "genre_id")
        private Long genreId;

        @Column(name = "name", nullable = false, unique = true)
        private String name;

        @Column(name = "description", columnDefinition = "TEXT")
        private String description;
    }


