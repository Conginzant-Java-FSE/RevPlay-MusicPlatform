package com.revplay.revplay.controller;

import com.revplay.revplay.common.ApiResponse;
import com.revplay.revplay.dto.request.AlbumCreateRequest;
import com.revplay.revplay.dto.response.AlbumResponse;
import com.revplay.revplay.dto.request.AlbumUpdateRequest;
import com.revplay.revplay.service.AlbumService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/albums")
public class AlbumController {

    private final AlbumService albumService;

    public AlbumController(AlbumService albumService) {
        this.albumService = albumService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AlbumResponse>> createAlbum(@Valid @RequestBody AlbumCreateRequest request) {
        AlbumResponse data = albumService.createAlbum(request);
        return ResponseEntity.ok(ApiResponse.ok("Album created", data));
    }

    @PutMapping("/{albumId}")
    public ResponseEntity<ApiResponse<AlbumResponse>> updateAlbum(
            @PathVariable Long albumId,
            @Valid @RequestBody AlbumUpdateRequest request) {

        AlbumResponse data = albumService.updateAlbum(albumId, request);
        return ResponseEntity.ok(ApiResponse.ok("Album updated", data));
    }

    @GetMapping("/{albumId}")
    public ResponseEntity<ApiResponse<AlbumResponse>> getAlbum(@PathVariable Long albumId) {
        AlbumResponse data = albumService.getAlbumById(albumId);
        return ResponseEntity.ok(ApiResponse.ok("Album fetched", data));
    }
}
