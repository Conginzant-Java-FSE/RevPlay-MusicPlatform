package com.revplay.revplay.repository;

import com.revplay.revplay.entity.Playlist;
import com.revplay.revplay.entity.PlaylistFollow;
import com.revplay.revplay.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaylistFollowRepository extends JpaRepository<PlaylistFollow, Long> {


    Optional<PlaylistFollow> findByUserAndPlaylist(User user, Playlist playlist);

    List<PlaylistFollow> findByUser(User user);

    List<PlaylistFollow> findByPlaylist(Playlist playlist);

    void deleteByUserAndPlaylist(User user, Playlist playlist);
}
