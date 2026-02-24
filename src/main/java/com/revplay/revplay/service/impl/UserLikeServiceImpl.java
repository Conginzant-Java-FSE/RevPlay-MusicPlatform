package com.revplay.revplay.service.impl;

import com.revplay.revplay.dto.request.UserLikeCreateRequest;
import com.revplay.revplay.dto.response.UserLikeResponse;
import com.revplay.revplay.entity.Song;
import com.revplay.revplay.entity.User;
import com.revplay.revplay.entity.UserLike;
import com.revplay.revplay.mapper.UserLikeMapper;
import com.revplay.revplay.repository.SongRepository;
import com.revplay.revplay.repository.UserLikeRepository;
import com.revplay.revplay.repository.UserRepository;
import com.revplay.revplay.service.UserLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserLikeServiceImpl implements UserLikeService {

    private final UserLikeRepository likeRepository;
    private final UserRepository userRepository;
    private final SongRepository songRepository;

    @Override
    public UserLikeResponse likeSong(UserLikeCreateRequest request) {

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Song song = songRepository.findById(request.getSongId())
                .orElseThrow(() -> new RuntimeException("Song not found"));

        likeRepository.findByUserAndSong(user, song)
                .ifPresent(l -> {
                    throw new RuntimeException("Already liked this song");
                });

        UserLike userLike = UserLikeMapper.toEntity(user, song);
        likeRepository.save(userLike);

        return UserLikeMapper.toResponse(userLike);
    }

    @Override
    public void unlikeSong(Long userId, Long songId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new RuntimeException("Song not found"));

        likeRepository.deleteByUserAndSong(user, song);
    }

    @Override
    public List<UserLikeResponse> getUserLikes(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return likeRepository.findByUser(user)
                .stream()
                .map(UserLikeMapper::toResponse)
                .collect(Collectors.toList());
    }
}