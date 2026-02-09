package com.revplay.revplay.repository;

import com.revplay.revplay.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GenreRepository extends JpaRepository<Genre, Long> {

    @Query("SELECT CASE WHEN COUNT(g) > 0 THEN true ELSE false END " +
            "FROM Genre g WHERE LOWER(g.name) = LOWER(:name)")
    boolean existsByNameIgnoreCase(@Param("name") String name);

    @Query("SELECT g FROM Genre g WHERE LOWER(g.name) = LOWER(:name)")
    Optional<Genre> findByNameIgnoreCase(@Param("name") String name);

    @Query("SELECT CASE WHEN COUNT(g) > 0 THEN true ELSE false END " +
            "FROM Genre g WHERE LOWER(g.name) = LOWER(:name) AND g.genreId != :genreId")
    boolean existsByNameIgnoreCaseAndIdNot(
            @Param("name") String name,
            @Param("genreId") Long genreId
    );
}

