package com.revplay.revplay.service;

import com.revplay.revplay.dto.response.SongResponse;
import com.revplay.revplay.dto.request.SongUpdateRequest;
import com.revplay.revplay.dto.request.SongUploadMeta;
import org.springframework.web.multipart.MultipartFile;

public interface SongService {

    SongResponse uploadSong(MultipartFile file, SongUploadMeta meta);

    SongResponse updateSong(Long songId, SongUpdateRequest dto);

    void deleteSong(Long songId);
}

