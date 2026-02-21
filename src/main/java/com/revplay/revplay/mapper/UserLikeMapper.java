package com.revplay.revplay.mapper;

import com.revplay.revplay.dto.response.UserLikeResponse;
import com.revplay.revplay.entity.Song;
import com.revplay.revplay.entity.User;
import com.revplay.revplay.entity.UserLike;

public class UserLikeMapper {

    private UserLikeMapper() {

    }


    public static UserLikeResponse toResponse(UserLike userLike) {

        return UserLikeResponse.builder()
                .id(userLike.getLikeId())
                .userId(userLike.getUser().getUserId())
                .songId(userLike.getSong().getSongId())
                .likedAt(userLike.getCreatedAt())
                .build();
    }


    public static UserLike toEntity(User user, Song song) {

        UserLike userLike = new UserLike();
        userLike.setUser(user);
        userLike.setSong(song);


        return userLike;
    }
}
