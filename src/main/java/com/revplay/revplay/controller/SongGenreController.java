package com.revplay.revplay.controller;

import com.revplay.revplay.dto.request.SongGenreAssignRequest;
import com.revplay.revplay.dto.response.SongGenreResponse;
import com.revplay.revplay.service.SongGenreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/songs")
@RequiredArgsConstructor
public class SongGenreController {

    private final SongGenreService songGenreService;

    @PostMapping("/{songId}/genres")
    public List<SongGenreResponse> assignGenres(
            @PathVariable Long songId,
            @Valid @RequestBody SongGenreAssignRequest request
    ) {
        return songGenreService.assignGenres(songId, request);
    }

    @GetMapping("/{songId}/genres")
    public List<SongGenreResponse> getGenres(@PathVariable Long songId) {
        return songGenreService.getGenres(songId);
    }

    @DeleteMapping("/{songId}/genres")
    public void clearGenres(@PathVariable Long songId) {
        songGenreService.clearGenres(songId);
    }
}
