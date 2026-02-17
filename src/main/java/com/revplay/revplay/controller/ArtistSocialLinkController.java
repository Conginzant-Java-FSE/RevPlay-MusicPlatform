package com.revplay.revplay.controller;

import com.revplay.revplay.common.ApiResponse;
import com.revplay.revplay.dto.request.ArtistSocialLinkCreate;
import com.revplay.revplay.dto.response.ArtistSocialLinkResponse;
import com.revplay.revplay.service.ArtistSocialLinkService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

        import java.util.List;

@RestController
@RequestMapping("/api/v1/artists")
public class ArtistSocialLinkController {

    private final ArtistSocialLinkService service;

    public ArtistSocialLinkController(ArtistSocialLinkService service) {
        this.service = service;
    }

    // Add social link for current logged-in artist
    @PostMapping("/me/social-links")
    public ResponseEntity<ApiResponse<ArtistSocialLinkResponse>> addMyLink(
            @Valid @RequestBody ArtistSocialLinkCreate request
    ) {
        ArtistSocialLinkResponse data = service.addMySocialLink(request);
        return ResponseEntity.ok(ApiResponse.ok("Social link added", data));
    }

    // Public: get social links of any artist by artistId
    @GetMapping("/{artistId}/social-links")
    public ResponseEntity<ApiResponse<List<ArtistSocialLinkResponse>>> getArtistLinks(@PathVariable Long artistId) {
        List<ArtistSocialLinkResponse> data = service.getArtistSocialLinks(artistId);
        return ResponseEntity.ok(ApiResponse.ok("Social links fetched", data));
    }

    // Delete my social link by linkId
    @DeleteMapping("/me/social-links/{linkId}")
    public ResponseEntity<ApiResponse<Void>> deleteMyLink(@PathVariable Long linkId) {
        service.deleteMySocialLink(linkId);
        return ResponseEntity.ok(ApiResponse.ok("Social link deleted", null));
    }
}

