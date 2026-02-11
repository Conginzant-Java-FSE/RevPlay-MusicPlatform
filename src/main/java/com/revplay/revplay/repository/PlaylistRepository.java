package com.revplay.revplay.repository;

import com.revplay.revplay.entity.Playlist;
import com.revplay.revplay.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {


    List<Playlist> findByUser(User user);


    List<Playlist> findByIsPublicTrue();


    List<Playlist> findByUserAndIsPublicTrue(User user);
}
