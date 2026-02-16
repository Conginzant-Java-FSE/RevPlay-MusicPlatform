
package com.revplay.revplay.service;

import com.revplay.revplay.dto.request.ArtistCreateRequest;
import com.revplay.revplay.dto.response.ArtistResponse;
import com.revplay.revplay.dto.request.ArtistUpdateRequest;

public interface ArtistService {

    ArtistResponse createArtistProfile(ArtistCreateRequest request);

    ArtistResponse updateMyArtistProfile(ArtistUpdateRequest request);

    ArtistResponse getArtistPublic(Long artistId);

    ArtistResponse getMyArtistProfile();
}
