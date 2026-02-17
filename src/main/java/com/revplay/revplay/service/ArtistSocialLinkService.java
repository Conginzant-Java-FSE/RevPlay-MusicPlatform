package com.revplay.revplay.service;

import com.revplay.revplay.dto.request.ArtistSocialLinkCreate;
import com.revplay.revplay.dto.response.ArtistSocialLinkResponse;

import java.util.List;

public interface ArtistSocialLinkService {

    ArtistSocialLinkResponse addMySocialLink(ArtistSocialLinkCreate request);

    List<ArtistSocialLinkResponse> getArtistSocialLinks(Long artistId);

    void deleteMySocialLink(Long linkId);
}
