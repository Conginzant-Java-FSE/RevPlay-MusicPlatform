package com.revplay.revplay.service;

import com.revplay.revplay.dto.request.UserLikeCreateRequest;
import com.revplay.revplay.dto.response.UserLikeResponse;

import java.util.List;

public interface UserLikeService {

    UserLikeResponse likeSong(UserLikeCreateRequest request);

    void unlikeSong(Long userId, Long songId);

    List<UserLikeResponse> getUserLikes(Long userId);
}