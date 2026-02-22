package com.revplay.revplay.service.impl;

import com.mpatric.mp3agic.Mp3File;
import com.revplay.revplay.dto.response.SongResponse;
import com.revplay.revplay.dto.request.SongUpdateRequest;
import com.revplay.revplay.dto.request.SongUploadMeta;
import com.revplay.revplay.entity.Album;
import com.revplay.revplay.entity.Artist;
import com.revplay.revplay.entity.Song;
import com.revplay.revplay.enums.ContentVisibility;
import com.revplay.revplay.exception.BadRequestException;
import com.revplay.revplay.exception.ResourceNotFoundException;
import com.revplay.revplay.repository.AlbumRepository;
import com.revplay.revplay.repository.SongRepository;
import com.revplay.revplay.service.SongService;
import com.revplay.revplay.util.CurrentArtistResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SongServiceImpl implements SongService {

    private final SongRepository songRepository;
    private final AlbumRepository albumRepository;
    private final CurrentArtistResolver currentArtistResolver;

    @Value("${revplay.storage.base-path}")
    private String storageBasePath;

    @Override
    @Transactional
    public SongResponse uploadSong(MultipartFile file, SongUploadMeta meta) {

        Artist artist = currentArtistResolver.getCurrentArtistOrThrow();

        // Duplicate title check (same artist)
        if (songRepository.existsByArtist_ArtistIdAndTitleIgnoreCase(artist.getArtistId(), meta.getTitle())) {
            throw new BadRequestException("Song title already exists for this artist");
        }

        // Validate file
        validateMp3(file);

        // Save file to storage/songs
        String savedFileName = saveFileToSongsFolder(file);
        String publicUrl = "/media/songs/" + savedFileName;

        // Compute duration using mp3agic
        int durationSeconds = readDurationSeconds(Path.of(storageBasePath, "songs", savedFileName));

        Album album = null;
        if (meta.getAlbumId() != null) {
            album = albumRepository.findByAlbumIdAndArtist_ArtistId(meta.getAlbumId(), artist.getArtistId())
                    .orElseThrow(() -> new ResourceNotFoundException("Album not found for this artist"));
        }

        Song song = new Song();
        song.setArtist(artist);
        song.setAlbum(album);
        song.setTitle(meta.getTitle());
        song.setReleaseDate(meta.getReleaseDate());
        song.setFileUrl(publicUrl);
        song.setDurationSeconds(durationSeconds);
        song.setVisibility(meta.getVisibility());
        song.setCreatedAt(LocalDateTime.now());

        Song saved = songRepository.save(song);

        log.info("Song uploaded: songId={}, artistId={}, file={}",
                saved.getSongId(), artist.getArtistId(), savedFileName);

        return toResponse(saved);
    }

    @Override
    @Transactional
    public SongResponse updateSong(Long songId, SongUpdateRequest dto) {

        Artist artist = currentArtistResolver.getCurrentArtistOrThrow();

        Song song = songRepository.findBySongIdAndArtist_ArtistId(songId, artist.getArtistId())
                .orElseThrow(() -> new ResourceNotFoundException("Song not found"));

        // If title changed, prevent duplicates
        if (!song.getTitle().equalsIgnoreCase(dto.getTitle())
                && songRepository.existsByArtist_ArtistIdAndTitleIgnoreCase(artist.getArtistId(), dto.getTitle())) {
            throw new BadRequestException("Song title already exists for this artist");
        }

        song.setTitle(dto.getTitle());
        song.setReleaseDate(dto.getReleaseDate());

        if (dto.getVisibility() != null) {
            song.setVisibility(dto.getVisibility());
        }

        if (dto.getAlbumId() != null) {
            Album album = albumRepository.findByAlbumIdAndArtist_ArtistId(dto.getAlbumId(), artist.getArtistId())
                    .orElseThrow(() -> new ResourceNotFoundException("Album not found for this artist"));
            song.setAlbum(album);
        } else {
            song.setAlbum(null);
        }

        Song updated = songRepository.save(song);
        log.info("Song updated: songId={}, artistId={}", updated.getSongId(), artist.getArtistId());

        return toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteSong(Long songId) {

        Artist artist = currentArtistResolver.getCurrentArtistOrThrow();

        Song song = songRepository.findBySongIdAndArtist_ArtistId(songId, artist.getArtistId())
                .orElseThrow(() -> new ResourceNotFoundException("Song not found"));

        songRepository.delete(song);

        log.warn("Song deleted: songId={}, artistId={}", songId, artist.getArtistId());
    }

    private void validateMp3(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Audio file is required");
        }
        String ext = FilenameUtils.getExtension(file.getOriginalFilename());
        if (ext == null || !ext.toLowerCase(Locale.ROOT).equals("mp3")) {
            throw new BadRequestException("Only MP3 files are supported");
        }
    }

    private String saveFileToSongsFolder(MultipartFile file) {
        try {
            Path songsDir = Path.of(storageBasePath, "songs");
            Files.createDirectories(songsDir);

            String safeName = UUID.randomUUID() + ".mp3";
            Path target = songsDir.resolve(safeName);

            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return safeName;
        } catch (Exception e) {
            throw new BadRequestException("Failed to store audio file");
        }
    }

    private int readDurationSeconds(Path mp3Path) {
        try {
            Mp3File mp3 = new Mp3File(mp3Path.toString());
            long seconds = mp3.getLengthInSeconds();
            if (seconds <= 0) {
                throw new BadRequestException("Invalid MP3 duration");
            }
            return (int) seconds;
        } catch (BadRequestException ex) {
            throw ex;
        } catch (Exception e) {
            throw new BadRequestException("Unable to read MP3 duration");
        }
    }

    private SongResponse toResponse(Song song) {
        Long albumId = (song.getAlbum() != null) ? song.getAlbum().getAlbumId() : null;
        ContentVisibility visibility = song.getVisibility();

        return SongResponse.builder()
                .songId(song.getSongId())
                .artistId(song.getArtist().getArtistId())
                .albumId(albumId)
                .title(song.getTitle())
                .durationSeconds(song.getDurationSeconds())
                .fileUrl(song.getFileUrl())
                .releaseDate(song.getReleaseDate())
                .visibility(visibility)
                .createdAt(song.getCreatedAt())
                .build();
    }
}