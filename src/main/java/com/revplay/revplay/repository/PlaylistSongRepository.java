package com.revplay.revplay.repository;

import com.revplay.revplay.entity.Playlist;
import com.revplay.revplay.entity.PlaylistSong;
import com.revplay.revplay.entity.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaylistSongRepository extends JpaRepository<PlaylistSong, Long> {


    List<PlaylistSong> findByPlaylistOrderByPositionAsc(Playlist playlist);


    PlaylistSong findByPlaylistAndSong(Playlist playlist, Song song);


    void deleteByPlaylist(Playlist playlist);
}
