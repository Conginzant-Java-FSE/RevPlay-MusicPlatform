package com.revplay.revplay.controller;

import com.revplay.revplay.common.ApiResponse;
import com.revplay.revplay.dto.request.ArtistCreateRequest;
import com.revplay.revplay.dto.request.ArtistUpdateRequest;
import com.revplay.revplay.dto.response.ArtistResponse;
import com.revplay.revplay.service.ArtistService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/artists")
public class ArtistController {

    private final ArtistService artistService;

    public ArtistController(ArtistService artistService) {
        this.artistService = artistService;
    }

    /**
     * Create artist profile for currently logged-in user
     * POST /api/v1/artists
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ArtistResponse>> create(
            @Valid @RequestBody ArtistCreateRequest request
    ) {
        ArtistResponse data = artistService.createArtistProfile(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Artist profile created", data));
    }

    /**
     * Update my artist profile
     * PUT /api/v1/artists/me
     */
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<ArtistResponse>> updateMe(
            @Valid @RequestBody ArtistUpdateRequest request
    ) {
        ArtistResponse data = artistService.updateMyArtistProfile(request);
        return ResponseEntity.ok(ApiResponse.ok("Artist profile updated", data));
    }

    /**
     * Get my artist profile
     * GET /api/v1/artists/me
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<ArtistResponse>> getMe() {
        ArtistResponse data = artistService.getMyArtistProfile();
        return ResponseEntity.ok(ApiResponse.ok("Artist profile fetched", data));
    }

    /**
     * Public view of artist profile
     * GET /api/v1/artists/{artistId}
     */
    @GetMapping("/{artistId}")
    public ResponseEntity<ApiResponse<ArtistResponse>> getPublic(@PathVariable Long artistId) {
        ArtistResponse data = artistService.getArtistPublic(artistId);
        return ResponseEntity.ok(ApiResponse.ok("Artist profile fetched", data));
    }
}
