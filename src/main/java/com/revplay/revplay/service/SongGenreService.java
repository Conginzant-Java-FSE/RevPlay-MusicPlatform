package com.revplay.revplay.service;

import com.revplay.revplay.dto.request.SongGenreAssignRequest;
import com.revplay.revplay.dto.response.SongGenreResponse;

import java.util.List;

public interface SongGenreService {

    List<SongGenreResponse> assignGenres(Long songId, SongGenreAssignRequest request);

    List<SongGenreResponse> getGenres(Long songId);

    void clearGenres(Long songId);
}