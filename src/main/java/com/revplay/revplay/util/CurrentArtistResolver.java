
package com.revplay.revplay.util;

import com.revplay.revplay.entity.Artist;
import com.revplay.revplay.entity.User;
import com.revplay.revplay.exception.ResourceNotFoundException;
import com.revplay.revplay.exception.UnauthorizedException;
import com.revplay.revplay.repository.ArtistRepository;
import com.revplay.revplay.repository.UserRepository;
import org.springframework.stereotype.Component;

@Component
public class CurrentArtistResolver {

    private final UserRepository userRepository;
    private final ArtistRepository artistRepository;

    public CurrentArtistResolver(UserRepository userRepository, ArtistRepository artistRepository) {
        this.userRepository = userRepository;
        this.artistRepository = artistRepository;
    }

    public Artist getCurrentArtistOrThrow() {
        String email = SecurityUtil.getCurrentUserEmail();
        if (email == null) throw new UnauthorizedException("Unauthorized");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Unauthorized"));

        return artistRepository.findByUserId(user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Artist profile not found for current user"));
    }
}
