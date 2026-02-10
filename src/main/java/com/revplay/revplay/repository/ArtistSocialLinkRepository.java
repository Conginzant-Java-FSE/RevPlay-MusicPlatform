package com.revplay.revplay.repository;

import com.revplay.revplay.entity.ArtistSocialLink;
import com.revplay.revplay.enums.SocialPlatform;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArtistSocialLinkRepository extends JpaRepository<ArtistSocialLink, Long> {
    List<ArtistSocialLink> findByArtist_ArtistId(Long artistId);
    Optional<ArtistSocialLink> findByArtist_ArtistIdAndPlatform(Long artistId, SocialPlatform platform);
}
