package com.revplay.revplay.service;

import com.revplay.revplay.dto.request.AlbumCreateRequest;
import com.revplay.revplay.dto.response.AlbumResponse;
import com.revplay.revplay.dto.request.AlbumUpdateRequest;

public interface AlbumService {

    AlbumResponse createAlbum(AlbumCreateRequest request);

    AlbumResponse updateAlbum(Long albumId, AlbumUpdateRequest request);

    AlbumResponse getAlbumById(Long albumId);
}