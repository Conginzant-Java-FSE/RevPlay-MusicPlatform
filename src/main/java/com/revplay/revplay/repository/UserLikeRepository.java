package com.revplay.revplay.repository;

import com.revplay.revplay.entity.Song;
import com.revplay.revplay.entity.User;
import com.revplay.revplay.entity.UserLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserLikeRepository extends JpaRepository<UserLike, Long> {


    Optional<UserLike> findByUserAndSong(User user, Song song);


    List<UserLike> findByUser(User user);


    List<UserLike> findBySong(Song song);


    void deleteByUserAndSong(User user, Song song);
}
