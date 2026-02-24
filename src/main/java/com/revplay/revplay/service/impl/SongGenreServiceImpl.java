package com.revplay.revplay.service.impl;

import com.revplay.revplay.dto.request.SongGenreAssignRequest;
import com.revplay.revplay.dto.response.SongGenreResponse;
import com.revplay.revplay.entity.Artist;
import com.revplay.revplay.entity.Song;
import com.revplay.revplay.entity.SongGenre;
import com.revplay.revplay.exception.BadRequestException;
import com.revplay.revplay.exception.ResourceNotFoundException;
import com.revplay.revplay.repository.SongGenreRepository;
import com.revplay.revplay.repository.SongRepository;
import com.revplay.revplay.service.SongGenreService;
import com.revplay.revplay.util.CurrentArtistResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SongGenreServiceImpl implements SongGenreService {

    private final SongRepository songRepository;
    private final SongGenreRepository songGenreRepository;
    private final CurrentArtistResolver currentArtistResolver;

    @Override
    @Transactional
    public List<SongGenreResponse> assignGenres(Long songId, SongGenreAssignRequest request) {

        Artist artist = currentArtistResolver.getCurrentArtistOrThrow();

        Song song = songRepository.findBySongIdAndArtist_ArtistId(songId, artist.getArtistId())
                .orElseThrow(() -> new ResourceNotFoundException("Song not found"));

        if (request == null || request.getGenreIds() == null || request.getGenreIds().isEmpty()) {
            throw new BadRequestException("genreIds is required");
        }

        List<SongGenreResponse> response = new ArrayList<>();

        for (Long genreId : request.getGenreIds()) {

            if (genreId == null || genreId <= 0) {
                throw new BadRequestException("Invalid genreId");
            }

            boolean exists = songGenreRepository.existsBySong_SongIdAndGenreId(songId, genreId);
            if (exists) {
                continue;
            }

            SongGenre sg = new SongGenre();
            sg.setSong(song);
            sg.setGenreId(genreId);

            SongGenre saved = songGenreRepository.save(sg);

            response.add(SongGenreResponse.builder()
                    .songGenreId(saved.getSongGenreId())
                    .songId(songId)
                    .genreId(genreId)
                    .build());
        }

        log.info("Genres assigned: songId={}, count={}", songId, response.size());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SongGenreResponse> getGenres(Long songId) {

        Artist artist = currentArtistResolver.getCurrentArtistOrThrow();

        songRepository.findBySongIdAndArtist_ArtistId(songId, artist.getArtistId())
                .orElseThrow(() -> new ResourceNotFoundException("Song not found"));

        return songGenreRepository.findBySong_SongId(songId)
                .stream()
                .map(sg -> SongGenreResponse.builder()
                        .songGenreId(sg.getSongGenreId())
                        .songId(songId)
                        .genreId(sg.getGenreId())
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public void clearGenres(Long songId) {

        Artist artist = currentArtistResolver.getCurrentArtistOrThrow();

        songRepository.findBySongIdAndArtist_ArtistId(songId, artist.getArtistId())
                .orElseThrow(() -> new ResourceNotFoundException("Song not found"));

        songGenreRepository.deleteBySong_SongId(songId);

        log.warn("Genres cleared: songId={}", songId);
    }
}